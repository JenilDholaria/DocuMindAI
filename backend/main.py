
import os
import uuid
from pathlib import Path
import time

import numpy as np
from dotenv import load_dotenv
from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from google import genai
from google.genai import types
from pydantic import BaseModel
from pypdf import PdfReader

load_dotenv(Path(__file__).with_name(".env"))

API_KEY = os.getenv("GEMINI_API_KEY", "").strip()
MODEL_NAME = os.getenv("GEMINI_MODEL", "gemini-2.5-flash")
EMBEDDING_MODEL = "gemini-embedding-001"

app = FastAPI(
    title="DocuMind AI Backend",
    description="Upload PDFs and ask questions grounded in their content",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# In-memory storage for uploaded documents.
# Documents are cleared when the server restarts.
documents = {}

CHUNK_SIZE = 1200
CHUNK_OVERLAP = 200
MAX_PDF_SIZE = 15 * 1024 * 1024


def get_client():
    if not API_KEY:
        raise HTTPException(
            status_code=500,
            detail="Gemini API key is missing. Configure GEMINI_API_KEY in backend/.env.",
        )
    return genai.Client(api_key=API_KEY)


def split_text(text: str):
    """Split text into overlapping character-based chunks."""
    chunks = []
    start = 0

    while start < len(text):
        end = min(start + CHUNK_SIZE, len(text))
        chunk = text[start:end].strip()

        if chunk:
            chunks.append(chunk)

        if end == len(text):
            break

        start = end - CHUNK_OVERLAP

    return chunks


def embed_texts(client, texts, task_type):
    """Create Gemini embeddings in small batches."""
    vectors = []

    for start in range(0, len(texts), 50):
        batch = texts[start:start + 50]

        response = client.models.embed_content(
            model=EMBEDDING_MODEL,
            contents=batch,
            config=types.EmbedContentConfig(
                task_type=task_type,
            ),
        )

        for item in response.embeddings:
            if item.values is None:
                raise HTTPException(
                    status_code=502,
                    detail="Gemini returned an empty embedding.",
                )
            vectors.append(np.array(item.values, dtype=np.float32))

    return vectors


@app.get("/")
def home():
    return {"message": "Welcome to DocuMind AI Backend"}


@app.get("/health")
def health_check():
    return {"status": "healthy"}


@app.post("/documents")
async def upload_document(file: UploadFile = File(...)):
    """Extract text, chunk it, and store embeddings for an uploaded PDF."""
    if not file.filename or Path(file.filename).suffix.lower() != ".pdf":
        raise HTTPException(
            status_code=400,
            detail="Please upload a PDF file.",
        )

    pdf_bytes = await file.read(MAX_PDF_SIZE + 1)
    await file.close()

    if len(pdf_bytes) > MAX_PDF_SIZE:
        raise HTTPException(
            status_code=413,
            detail="PDF is too large. Maximum size is 15 MB.",
        )

    try:
        from io import BytesIO

        reader = PdfReader(BytesIO(pdf_bytes))

        if reader.is_encrypted:
            raise HTTPException(
                status_code=400,
                detail="Password-protected PDFs are not supported.",
            )

        page_texts = []

        for page_number, page in enumerate(reader.pages, start=1):
            text = (page.extract_text() or "").strip()
            if text:
                page_texts.append((page_number, text))

    except HTTPException:
        raise
    except Exception:
        raise HTTPException(
            status_code=400,
            detail="Could not read this PDF. Please check that it is a valid PDF.",
        )

    if not page_texts:
        raise HTTPException(
            status_code=400,
            detail="No selectable text found. Scanned PDFs need OCR, which is not implemented yet.",
        )

    chunks = []

    for page_number, page_text in page_texts:
        for chunk in split_text(page_text):
            chunks.append({
                "page": page_number,
                "text": chunk,
            })

    client = get_client()

    try:
        vectors = embed_texts(
            client,
            [chunk["text"] for chunk in chunks],
            "RETRIEVAL_DOCUMENT",
        )
    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail=f"Could not create document embeddings: {str(exc)[:300]}",
        )

    document_id = str(uuid.uuid4())

    documents[document_id] = {
        "filename": Path(file.filename).name,
        "chunks": chunks,
        "vectors": vectors,
    }

    return {
        "document_id": document_id,
        "filename": Path(file.filename).name,
        "pages": len(reader.pages),
        "chunks": len(chunks),
        "message": "PDF uploaded and indexed successfully.",
    }


class ChatRequest(BaseModel):
    document_id: str
    question: str


@app.post("/chat")
def chat(request: ChatRequest):
    """Retrieve relevant document chunks and ask Gemini to answer from them."""
    question = request.question.strip()

    if not question:
        raise HTTPException(
            status_code=400,
            detail="Please enter a question.",
        )

    if len(question) > 2000:
        raise HTTPException(
            status_code=400,
            detail="Question is too long. Maximum length is 2000 characters.",
        )

    document = documents.get(request.document_id)

    if document is None:
        raise HTTPException(
            status_code=404,
            detail="Document not found. Please upload the PDF again.",
        )

    client = get_client()

    try:
        query_vectors = embed_texts(
            client,
            [question],
            "RETRIEVAL_QUERY",
        )
        query_vector = query_vectors[0]

        chunk_vectors = document["vectors"]
        scores = []

        for vector in chunk_vectors:
            denominator = (
                np.linalg.norm(query_vector) * np.linalg.norm(vector)
            )
            score = (
                float(np.dot(query_vector, vector) / denominator)
                if denominator > 0
                else 0.0
            )
            scores.append(score)

        best_indices = sorted(
            range(len(scores)),
            key=lambda index: scores[index],
            reverse=True,
        )[:5]

        selected_chunks = [
            document["chunks"][index] for index in best_indices
        ]

        context_parts = []

        for chunk in selected_chunks:
            context_parts.append(
                f"[Page {chunk['page']}]\n{chunk['text']}"
            )

        context = "\n\n".join(context_parts)

        prompt = (
            "Answer the user's question using only the document "
            "excerpts provided below. If the excerpts do not contain "
            "enough information, say that the answer is not available "
            "in the uploaded document. Do not invent facts. "
            "Refer to source page numbers when useful.\n\n"
            f"DOCUMENT EXCERPTS:\n{context}\n\n"
            f"QUESTION:\n{question}"
        )

        for attempt in range(3):
            try:
                response = client.models.generate_content(
                    model=MODEL_NAME,
                    contents=prompt,
                )
                break
            except Exception as exc:
                is_temporary_error = (
                    "503" in str(exc) or "UNAVAILABLE" in str(exc)
                )

                if not is_temporary_error or attempt == 2:
                    raise

                time.sleep(2 ** (attempt + 1))

        answer = (response.text or "").strip()

        if not answer:
            raise HTTPException(
                status_code=502,
                detail="Gemini returned an empty answer.",
            )

        sources = [
            {
                "page": chunk["page"],
                "excerpt": chunk["text"][:400],
            }
            for chunk in selected_chunks
        ]

        return {
            "document_id": request.document_id,
            "question": question,
            "answer": answer,
            "sources": sources,
        }

    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail=f"Could not generate an answer: {str(exc)[:300]}",
        )

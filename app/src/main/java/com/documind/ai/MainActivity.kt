
package com.documind.ai

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.documind.ai.ui.theme.DocuMindAITheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DocuMindAITheme {

                var documentId by remember {
                    mutableStateOf<String?>(null)
                }

                var documentName by remember {
                    mutableStateOf("Your PDF")
                }

                var isUploading by remember {
                    mutableStateOf(false)
                }

                var isSending by remember {
                    mutableStateOf(false)
                }

                var errorMessage by remember {
                    mutableStateOf<String?>(null)
                }

                val messages = remember {
                    mutableStateListOf(
                        ChatMessage(
                            text = "Hi! Ask me a question about your PDF.",
                            isUser = false
                        )
                    )
                }

                val scope = rememberCoroutineScope()

                // Opens the Android PDF/file picker.
                val pdfPicker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->

                    if (uri != null) {
                        scope.launch {
                            isUploading = true
                            errorMessage = null

                            try {
                                val result = withContext(Dispatchers.IO) {

                                    val bytes = contentResolver
                                        .openInputStream(uri)
                                        ?.use { inputStream ->
                                            inputStream.readBytes()
                                        }
                                        ?: error("Could not read the PDF")

                                    val filename = getPdfFilename(uri)

                                    ApiClient.uploadPdf(bytes, filename)
                                }

                                // Save the uploaded document details.
                                documentId = result.documentId
                                documentName = result.filename

                                // Reset the conversation for the new PDF.
                                messages.clear()
                                messages.add(
                                    ChatMessage(
                                        text = "PDF uploaded successfully! Ask me a question.",
                                        isUser = false
                                    )
                                )

                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Upload failed"
                            } finally {
                                isUploading = false
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    // Show the redesigned home screen until a PDF is uploaded.
                    if (documentId == null) {

                        HomeScreen(
                            isUploading = isUploading,
                            errorMessage = errorMessage,
                            onUploadClick = {
                                if (!isUploading) {
                                    pdfPicker.launch("application/pdf")
                                }
                            }
                        )

                    } else {

                        // Keep the existing chat screen and question-answer flow.
                        ChatScreen(
                            documentName = documentName,
                            messages = messages,
                            isLoading = isSending,

                            onBack = {
                                if (!isSending && !isUploading) {
                                    documentId = null
                                    documentName = "Your PDF"
                                    errorMessage = null
                                }
                            },

                            onSendQuestion = { question ->

                                val currentId = documentId

                                if (currentId != null && !isSending) {

                                    messages.add(
                                        ChatMessage(
                                            text = question,
                                            isUser = true
                                        )
                                    )

                                    isSending = true

                                    scope.launch {
                                        try {
                                            val result = withContext(Dispatchers.IO) {
                                                ApiClient.askQuestion(
                                                    documentId = currentId,
                                                    question = question
                                                )
                                            }

                                            // Ignore responses for a document
                                            // that is no longer the active one.
                                            if (documentId == currentId) {
                                                messages.add(
                                                    ChatMessage(
                                                        text = result.answer,
                                                        isUser = false,
                                                        sources = result.sources.map { source ->
                                                            SourceItem(
                                                                page = source.page,
                                                                excerpt = source.excerpt
                                                            )
                                                        }
                                                    )
                                                )
                                            }

                                        } catch (e: Exception) {
                                            if (documentId == currentId) {
                                                messages.add(
                                                    ChatMessage(
                                                        text = "Error: ${
                                                            e.message
                                                                ?: "Could not get an answer"
                                                        }",
                                                        isUser = false
                                                    )
                                                )
                                            }
                                        } finally {
                                            isSending = false
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Gets the original filename selected by the user.
    private fun getPdfFilename(uri: Uri): String {
        var filename = "document.pdf"

        contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        )?.use { cursor ->

            val index = cursor.getColumnIndex(
                OpenableColumns.DISPLAY_NAME
            )

            if (index >= 0 && cursor.moveToFirst()) {
                filename = cursor.getString(index)
            }
        }

        return filename
    }
}

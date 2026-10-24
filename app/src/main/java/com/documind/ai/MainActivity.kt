
package com.documind.ai

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.documind.ai.ui.theme.DocuMindAITheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DocuMindAITheme {
                var documentId by remember { mutableStateOf<String?>(null) }
                var documentName by remember { mutableStateOf("Your PDF") }
                var isUploading by remember { mutableStateOf(false) }
                var isSending by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                val messages = remember {
                    mutableStateListOf(
                        ChatMessage(
                            text = "Hi! Ask me a question about your PDF.",
                            isUser = false
                        )
                    )
                }

                val scope = rememberCoroutineScope()

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
                                        ?.use { it.readBytes() }
                                        ?: error("Could not read the PDF")

                                    val filename = getPdfFilename(uri)

                                    ApiClient.uploadPdf(bytes, filename)
                                }

                                documentId = result.documentId
                                documentName = result.filename

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
                    if (documentId == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "DocuMindAI",
                                style = MaterialTheme.typography.headlineLarge
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Upload a PDF to start asking questions")

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                enabled = !isUploading,
                                onClick = {
                                    pdfPicker.launch("application/pdf")
                                }
                            ) {
                                Text(
                                    if (isUploading) "Uploading..."
                                    else "Select PDF"
                                )
                            }

                            if (isUploading) {
                                Spacer(modifier = Modifier.height(16.dp))
                                CircularProgressIndicator()
                            }

                            errorMessage?.let { error ->
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        ChatScreen(
                            documentName = documentName,
                            messages = messages,
                            isLoading = isSending,
                            onBack = {
                                if (!isSending) {
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
                                                        text = "Error: ${e.message ?: "Could not get an answer"}",
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

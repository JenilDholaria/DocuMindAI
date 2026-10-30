
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
import java.util.UUID

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DocuMindAITheme {
                var documentId by remember { mutableStateOf<String?>(null) }
                var documentName by remember { mutableStateOf("Your PDF") }
                var activeChatId by remember { mutableStateOf<String?>(null) }

                var isUploading by remember { mutableStateOf(false) }
                var isSending by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                val messages = remember {
                    mutableStateListOf<ChatMessage>()
                }

                val recentChats = remember {
                    mutableStateListOf<SavedChat>().apply {
                        addAll(ChatHistoryStore.getAll(this@MainActivity))
                    }
                }

                val scope = rememberCoroutineScope()

                fun refreshHistory() {
                    recentChats.clear()
                    recentChats.addAll(
                        ChatHistoryStore.getAll(this@MainActivity)
                    )
                }

                fun saveActiveChat() {
                    val id = activeChatId ?: return
                    val docId = documentId ?: return

                    val title = messages.firstOrNull {
                        it.isUser
                    }?.text?.take(60) ?: documentName

                    val saved = SavedChat(
                        id = id,
                        title = title,
                        documentId = docId,
                        documentName = documentName,
                        messages = messages.map { message ->
                            SavedChatMessage(
                                text = message.text,
                                isUser = message.isUser,
                                sources = message.sources.map { source ->
                                    SavedChatSource(
                                        page = source.page,
                                        excerpt = source.excerpt
                                    )
                                }
                            )
                        },
                        updatedAt = System.currentTimeMillis()
                    )

                    ChatHistoryStore.save(this@MainActivity, saved)
                    refreshHistory()
                }

                fun openChat(chat: SavedChat) {
                    activeChatId = chat.id
                    documentId = chat.documentId
                    documentName = chat.documentName
                    errorMessage = null

                    messages.clear()
                    messages.addAll(
                        chat.messages.map { saved ->
                            ChatMessage(
                                text = saved.text,
                                isUser = saved.isUser,
                                sources = saved.sources.map { source ->
                                    SourceItem(
                                        page = source.page,
                                        excerpt = source.excerpt
                                    )
                                }
                            )
                        }
                    )
                }

                fun startNewChat() {
                    activeChatId = null
                    documentId = null
                    documentName = "Your PDF"
                    errorMessage = null
                    messages.clear()
                }

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
                                activeChatId = UUID.randomUUID().toString()

                                messages.clear()
                                messages.add(
                                    ChatMessage(
                                        text = "Your PDF has been uploaded successfully! I'm ready for your questions.",
                                        isUser = false
                                    )
                                )

                                saveActiveChat()
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
                        HomeScreen(
                            isUploading = isUploading,
                            errorMessage = errorMessage,
                            recentChats = recentChats,
                            onUploadClick = {
                                if (!isUploading) {
                                    pdfPicker.launch("application/pdf")
                                }
                            },
                            onNewChat = {
                                if (!isUploading && !isSending) {
                                    startNewChat()
                                    pdfPicker.launch("application/pdf")
                                }
                            },
                            onOpenChat = { chat ->
                                if (!isUploading && !isSending) {
                                    openChat(chat)
                                }
                            },
                            onDeleteChat = { chatId ->
                                ChatHistoryStore.delete(
                                    this@MainActivity,
                                    chatId
                                )
                                if (activeChatId == chatId) {
                                    startNewChat()
                                }
                                refreshHistory()
                            }
                        )
                    } else {
                        ChatScreen(
                            documentName = documentName,
                            messages = messages,
                            isLoading = isSending,
                            onBack = {
                                if (!isSending) {
                                    saveActiveChat()
                                    startNewChat()
                                }
                            },
                            onSendQuestion = { question ->
                                val currentId = documentId
                                val currentChatId = activeChatId

                                if (currentId != null &&
                                    currentChatId != null &&
                                    !isSending
                                ) {
                                    messages.add(
                                        ChatMessage(
                                            text = question,
                                            isUser = true
                                        )
                                    )
                                    saveActiveChat()
                                    isSending = true

                                    scope.launch {
                                        try {
                                            val result = withContext(
                                                Dispatchers.IO
                                            ) {
                                                ApiClient.askQuestion(
                                                    documentId = currentId,
                                                    question = question
                                                )
                                            }

                                            if (documentId == currentId &&
                                                activeChatId == currentChatId
                                            ) {
                                                messages.add(
                                                    ChatMessage(
                                                        text = result.answer,
                                                        isUser = false,
                                                        sources = result.sources.map {
                                                            SourceItem(
                                                                page = it.page,
                                                                excerpt = it.excerpt
                                                            )
                                                        }
                                                    )
                                                )
                                                saveActiveChat()
                                            }
                                        } catch (e: Exception) {
                                            if (documentId == currentId &&
                                                activeChatId == currentChatId
                                            ) {
                                                messages.add(
                                                    ChatMessage(
                                                        text = "Sorry, I couldn't answer that. ${e.message ?: "Please try again."}",
                                                        isUser = false
                                                    )
                                                )
                                                saveActiveChat()
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


package com.documind.ai

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ChatBackground = Color(0xFF0A0B11)
private val ChatPanel = Color(0xFF151722)
private val ChatPurple = Color(0xFF9670FF)
private val ChatText = Color(0xFFF5F3FF)
private val ChatMuted = Color(0xFFA6A9BC)
private val ChatBorder = Color(0xFF292D3C)

data class SourceItem(
    val page: Int,
    val excerpt: String
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val sources: List<SourceItem> = emptyList()
)

@Composable
fun ChatScreen(
    documentName: String,
    messages: List<ChatMessage>,
    isLoading: Boolean = false,
    onBack: () -> Unit = {},
    onSendQuestion: (String) -> Unit = {}
) {
    var question by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    // Speech recognition launcher.
    // Recognized words are placed in the question field.
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )
                ?.firstOrNull()
                ?.trim()

            if (!spokenText.isNullOrBlank()) {
                question = if (question.isBlank()) {
                    spokenText
                } else {
                    "${question.trimEnd()} $spokenText"
                }
            }
        }
    }

    // Start Android speech recognition.
    fun startVoiceInput() {
        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            // Indian English. Change to "hi-IN" for Hindi.
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "en-IN"
            )

            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Speak your question about the PDF"
            )
        }

        try {
            speechLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "Speech recognition is not available on this device.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Automatically scroll to the latest message.
    LaunchedEffect(messages.size, isLoading) {
        val count = messages.size + if (isLoading) 1 else 0

        if (count > 0) {
            listState.animateScrollToItem(count - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatBackground)
            .statusBarsPadding()
            .imePadding()
    ) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBack,
                shape = RoundedCornerShape(14.dp),
                color = ChatPanel,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    ChatBorder
                ),
                modifier = Modifier.size(42.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "‹",
                        color = ChatText,
                        fontSize = 30.sp
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFAA8BFF),
                                Color(0xFF6243D5)
                            )
                        ),
                        RoundedCornerShape(15.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "D",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.width(11.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "DocuMind AI",
                    color = ChatText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(3.dp))

                Text(
                    documentName,
                    color = ChatMuted,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        Color(0xFF15251E),
                        CircleShape
                    )
                    .border(
                        1.dp,
                        Color(0xFF28513D),
                        CircleShape
                    )
                    .padding(
                        horizontal = 9.dp,
                        vertical = 7.dp
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .background(
                                Color(0xFF58D99B),
                                CircleShape
                            )
                    )

                    Spacer(Modifier.width(5.dp))

                    Text(
                        "PDF",
                        color = Color(0xFF8BE8B6),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        HorizontalDivider(color = ChatBorder)

        // Document context strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
                .background(
                    Color(0xFF171526),
                    RoundedCornerShape(15.dp)
                )
                .border(
                    1.dp,
                    Color(0xFF302746),
                    RoundedCornerShape(15.dp)
                )
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .background(
                        Color(0xFF302448),
                        RoundedCornerShape(11.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "✦",
                    color = ChatPurple,
                    fontSize = 19.sp
                )
            }

            Spacer(Modifier.width(11.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Ask your document anything",
                    color = ChatText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(3.dp))

                Text(
                    "Answers based on your uploaded PDF",
                    color = ChatMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            itemsIndexed(messages) { _, message ->
                MessageBubble(message = message)
            }

            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(35.dp)
                                .background(
                                    Color(0xFF28213F),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "✦",
                                color = ChatPurple,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        Row(
                            modifier = Modifier
                                .background(
                                    ChatPanel,
                                    RoundedCornerShape(16.dp)
                                )
                                .border(
                                    1.dp,
                                    ChatBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(
                                    horizontal = 15.dp,
                                    vertical = 13.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = ChatPurple,
                                strokeWidth = 2.dp
                            )

                            Spacer(Modifier.width(10.dp))

                            Text(
                                "Thinking about your question...",
                                color = ChatMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Message composer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0F17))
                .border(
                    width = 1.dp,
                    color = ChatBorder
                )
                .padding(
                    horizontal = 15.dp,
                    vertical = 12.dp
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        ChatPanel,
                        RoundedCornerShape(20.dp)
                    )
                    .border(
                        1.dp,
                        Color(0xFF343348),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(
                        start = 16.dp,
                        end = 7.dp,
                        top = 7.dp,
                        bottom = 7.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Question input
                BasicTextField(
                    value = question,
                    onValueChange = { question = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    singleLine = true,
                    enabled = !isLoading,
                    textStyle = TextStyle(
                        color = ChatText,
                        fontSize = 15.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            val text = question.trim()

                            if (text.isNotEmpty() && !isLoading) {
                                question = ""
                                keyboard?.hide()
                                onSendQuestion(text)
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (question.isBlank()) {
                                Text(
                                    "Ask anything about your PDF...",
                                    color = ChatMuted,
                                    fontSize = 13.sp
                                )
                            }

                            innerTextField()
                        }
                    }
                )

                Spacer(Modifier.width(6.dp))

                // NEW: Microphone button
                Surface(
                    onClick = {
                        if (!isLoading) {
                            keyboard?.hide()
                            startVoiceInput()
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF302448),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFF51416F)
                    ),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎤",
                            fontSize = 19.sp
                        )
                    }
                }

                Spacer(Modifier.width(6.dp))

                // Send button
                Surface(
                    onClick = {
                        val text = question.trim()

                        if (text.isNotEmpty() && !isLoading) {
                            question = ""
                            keyboard?.hide()
                            onSendQuestion(text)
                        }
                    },
                    enabled = question.isNotBlank() && !isLoading,
                    shape = RoundedCornerShape(15.dp),
                    color = if (
                        question.isNotBlank() && !isLoading
                    ) {
                        ChatPurple
                    } else {
                        Color(0xFF393449)
                    },
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "➤",
                            color = Color.White,
                            fontSize = 21.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "AI ANSWERS  ·  DOCUMENT SOURCES",
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF777B91),
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) {
            Alignment.End
        } else {
            Alignment.Start
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!message.isUser) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            Color(0xFF28213F),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✦",
                        color = ChatPurple,
                        fontSize = 17.sp
                    )
                }

                Spacer(Modifier.width(8.dp))
            }

            Text(
                if (message.isUser) "YOU" else "DOCUMIND AI",
                color = ChatMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isUser) 18.dp else 5.dp,
                bottomEnd = if (message.isUser) 5.dp else 18.dp
            ),
            color = if (message.isUser) {
                Color(0xFF7052CF)
            } else {
                ChatPanel
            },
            border = if (message.isUser) {
                null
            } else {
                androidx.compose.foundation.BorderStroke(
                    1.dp,
                    ChatBorder
                )
            },
            modifier = Modifier.fillMaxWidth(
                if (message.isUser) 0.88f else 0.98f
            )
        ) {
            Text(
                text = cleanMarkdown(message.text),
                modifier = Modifier.padding(16.dp),
                color = ChatText,
                fontSize = 14.sp,
                lineHeight = 23.sp
            )
        }

        // Supporting PDF sources
        message.sources.forEachIndexed { index, source ->
            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.98f)
                    .background(
                        Color(0xFF111A18),
                        RoundedCornerShape(15.dp)
                    )
                    .border(
                        1.dp,
                        Color(0xFF294238),
                        RoundedCornerShape(15.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "▤",
                        color = Color(0xFF75DCA8),
                        fontSize = 17.sp
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        "SOURCE ${index + 1}",
                        color = Color(0xFF75DCA8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(Modifier.weight(1f))

                    Text(
                        "PAGE ${source.page}",
                        color = ChatMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(9.dp))

                Text(
                    cleanMarkdown(source.excerpt),
                    color = Color(0xFFD0D5E0),
                    fontSize = 12.sp,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

private fun cleanMarkdown(text: String): String {
    return text
        .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
        .replace(Regex("__(.*?)__"), "$1")
        .replace(Regex("`([^`]*)`"), "$1")
        .replace(Regex("(?m)^#{1,6}\\s*"), "")
        .replace(Regex("(?m)^\\s*[-*]\\s+"), "• ")
        .trim()
}

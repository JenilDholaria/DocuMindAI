
package com.documind.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HomeBackground = Color(0xFF090B12)
private val HomeCard = Color(0xFF141725)
private val HomePurple = Color(0xFF9670FF)
private val HomeText = Color(0xFFF4F2FF)
private val HomeMuted = Color(0xFFA2A6BB)
private val HomeBorder = Color(0xFF292D42)

@Composable
fun HomeScreen(
    isUploading: Boolean,
    errorMessage: String?,
    onUploadClick: () -> Unit,
    recentChats: List<SavedChat> = emptyList(),
    onNewChat: () -> Unit = {},
    onOpenChat: (SavedChat) -> Unit = {},
    onDeleteChat: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Top branding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 12.dp, bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFB39AFF),
                                Color(0xFF6945DE)
                            )
                        ),
                        RoundedCornerShape(15.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "D",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = "DocuMind",
                    color = HomeText,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "AI DOCUMENT ASSISTANT",
                    color = HomeMuted,
                    fontSize = 9.sp,
                    letterSpacing = 1.4.sp
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .background(
                        Color(0xFF17251F),
                        CircleShape
                    )
                    .border(
                        1.dp,
                        Color(0xFF28523E),
                        CircleShape
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(6.dp)
                        .background(
                            Color(0xFF50DDA0),
                            CircleShape
                        )
                )

                Spacer(Modifier.width(7.dp))

                Text(
                    text = if (isUploading) "WORKING" else "READY",
                    color = Color(0xFF75E8B0),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Hero section
        Text(
            text = "YOUR KNOWLEDGE, REIMAGINED",
            color = HomeMuted,
            fontSize = 10.sp,
            letterSpacing = 1.7.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Meet your\npersonal AI.",
            color = HomeText,
            fontSize = 43.sp,
            lineHeight = 49.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(13.dp))

        Text(
            text = "One document. Any question.\nAnswers grounded in your PDF.",
            color = HomeMuted,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(27.dp))

        // PDF upload card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF28203F),
                            Color(0xFF191827),
                            Color(0xFF141722)
                        )
                    ),
                    RoundedCornerShape(25.dp)
                )
                .border(
                    1.dp,
                    Color(0xFF57417F),
                    RoundedCornerShape(25.dp)
                )
                .padding(19.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(49.dp)
                            .background(
                                Color(0xFF3A2B62),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PDF",
                            color = Color(0xFFDCCEFF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(Modifier.width(13.dp))

                    Column {
                        Text(
                            text = "Start with a document",
                            color = HomeText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            text = "Your next insight starts here",
                            color = HomeMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(Modifier.height(21.dp))

                Button(
                    onClick = onUploadClick,
                    enabled = !isUploading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(53.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomePurple,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF51466F)
                    )
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )

                        Spacer(Modifier.width(10.dp))

                        Text("Analyzing your PDF...")
                    } else {
                        Text("＋", fontSize = 23.sp)

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "Upload your PDF",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isUploading) {
                    Spacer(Modifier.height(12.dp))

                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = HomePurple,
                        trackColor = Color(0xFF373047)
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = errorMessage,
                        color = Color(0xFFFF929D),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓",
                        color = Color(0xFF76DDAA),
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = "PDF DOCUMENTS",
                        color = HomeMuted,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )

                    Spacer(Modifier.width(12.dp))

                    Box(
                        Modifier
                            .size(3.dp)
                            .background(HomeMuted, CircleShape)
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = "AI-POWERED INSIGHTS",
                        color = HomeMuted,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Recent chats section
        Spacer(Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "YOUR WORKSPACE",
                    color = HomeMuted,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = "Recent chats",
                    color = HomeText,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = onNewChat,
                enabled = !isUploading
            ) {
                Text(
                    text = "+ New chat",
                    color = HomePurple,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(13.dp))

        // Show an empty state when no chats have been saved.
        if (recentChats.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        HomeCard,
                        RoundedCornerShape(17.dp)
                    )
                    .border(
                        1.dp,
                        HomeBorder,
                        RoundedCornerShape(17.dp)
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✦",
                    color = HomePurple,
                    fontSize = 28.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Your conversations will appear here",
                    color = HomeText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = "Upload a PDF and start asking questions.",
                    color = HomeMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Display the ten most recent saved chats.
            recentChats.take(10).forEach { chat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 9.dp)
                        .background(
                            HomeCard,
                            RoundedCornerShape(16.dp)
                        )
                        .border(
                            1.dp,
                            HomeBorder,
                            RoundedCornerShape(16.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tap a conversation to reopen it.
                    Surface(
                        onClick = {
                            onOpenChat(chat)
                        },
                        color = Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        Color(0xFF302448),
                                        RoundedCornerShape(13.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "▤",
                                    color = HomePurple,
                                    fontSize = 21.sp
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = chat.title,
                                    color = HomeText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = chat.documentName,
                                    color = HomeMuted,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )

                                Spacer(Modifier.height(3.dp))

                                Text(
                                    text = "${chat.messages.count { it.isUser }} questions",
                                    color = HomePurple,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Delete a saved conversation.
                    IconButton(
                        onClick = {
                            onDeleteChat(chat.id)
                        }
                    ) {
                        Text(
                            text = "×",
                            color = HomeMuted,
                            fontSize = 23.sp
                        )
                    }
                }
            }

            if (recentChats.size > 10) {
                Text(
                    text = "Showing the 10 most recent conversations",
                    color = HomeMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Team credits
        Spacer(Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "THE PEOPLE BEHIND IT",
                    color = HomeMuted,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = "Meet the team.",
                    color = HomeText,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        Color(0xFF211B35),
                        CircleShape
                    )
                    .border(
                        1.dp,
                        Color(0xFF453568),
                        CircleShape
                    )
                    .padding(
                        horizontal = 11.dp,
                        vertical = 7.dp
                    )
            ) {
                Text(
                    text = "OUR PROJECT",
                    color = Color(0xFFC6B1FF),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(15.dp))

        TeamMemberCard(
            initials = "JD",
            name = "Jenil Dholaria",
            studentId = "C123",
            accent = Color(0xFF9D7AFF)
        )

        Spacer(Modifier.height(10.dp))

        TeamMemberCard(
            initials = "JR",
            name = "Jay Rakholia",
            studentId = "C122",
            accent = Color(0xFF68C8E8)
        )

        Spacer(Modifier.height(10.dp))

        TeamMemberCard(
            initials = "UC",
            name = "Ujwala Chinta",
            studentId = "C116",
            accent = Color(0xFF66D6AE)
        )

        Spacer(Modifier.height(10.dp))

        TeamMemberCard(
            initials = "VS",
            name = "Vivaan Sanghvi",
            studentId = "C063",
            accent = Color(0xFFFFB86B)
        )

        Spacer(Modifier.height(22.dp))

        HorizontalDivider(color = HomeBorder)

        Spacer(Modifier.height(15.dp))

        Text(
            text = "DOCUMIND AI",
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFB7A0FF),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = "Built with curiosity. Powered by AI.",
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF777C91),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun TeamMemberCard(
    initials: String,
    name: String,
    studentId: String,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                HomeCard,
                RoundedCornerShape(17.dp)
            )
            .border(
                1.dp,
                HomeBorder,
                RoundedCornerShape(17.dp)
            )
            .padding(
                horizontal = 14.dp,
                vertical = 13.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.35f),
                            Color(0xFF242238)
                        )
                    ),
                    RoundedCornerShape(14.dp)
                )
                .border(
                    1.dp,
                    accent.copy(alpha = 0.5f),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(Modifier.width(13.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                color = HomeText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "DEVELOPMENT TEAM",
                color = HomeMuted,
                fontSize = 9.sp,
                letterSpacing = 0.8.sp
            )
        }

        Box(
            modifier = Modifier
                .background(
                    Color(0xFF211E31),
                    RoundedCornerShape(9.dp)
                )
                .padding(
                    horizontal = 9.dp,
                    vertical = 7.dp
                )
        ) {
            Text(
                text = studentId,
                color = accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


package com.documind.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.documind.ai.ui.theme.PremiumColors

@Composable
fun HomeScreen(
    isUploading: Boolean,
    errorMessage: String?,
    onUploadClick: () -> Unit
) {
    val colors = PremiumColors

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // BRAND HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(colors.Violet, colors.Indigo)
                            ),
                            RoundedCornerShape(15.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✦",
                        color = Color.White,
                        fontSize = 28.sp
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DocuMindAI",
                        color = colors.TextPrimary,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "YOUR PERSONAL KNOWLEDGE SPACE",
                        color = colors.TextSecondary,
                        fontSize = 9.sp,
                        letterSpacing = 1.3.sp
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = colors.SurfaceElevated
                ) {
                    Text(
                        text = "AI",
                        color = colors.VioletLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            horizontal = 13.dp,
                            vertical = 9.dp
                        )
                    )
                }
            }

            // HERO PANEL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF29204A),
                                Color(0xFF191D37),
                                Color(0xFF171927)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Surface(
                        color = Color(0x338B5CF6),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            text = "✦  INTELLIGENCE, REIMAGINED",
                            color = colors.VioletLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 9.dp
                            )
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Your documents.\nUnderstood.",
                        color = colors.TextPrimary,
                        fontSize = 33.sp,
                        lineHeight = 39.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Turn complex PDFs into clear answers. " +
                                "Upload a document and let your ideas come to life.",
                        color = Color(0xFFC0C2D7),
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = onUploadClick,
                        enabled = !isUploading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(17.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.Violet,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF51416F)
                        )
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Preparing your document...")
                        } else {
                            Text("＋", fontSize = 23.sp)
                            Spacer(Modifier.width(9.dp))
                            Text(
                                "Upload a PDF",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "PDF DOCUMENTS  •  AI-POWERED ANSWERS",
                        color = Color(0xFFB9B5D6),
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            // ERROR MESSAGE
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF351D29)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Upload failed\n$errorMessage",
                        color = colors.Error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // SECTION HEADING
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "Your AI workspace",
                    color = colors.TextPrimary,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Everything you need to understand a document.",
                    color = colors.TextSecondary,
                    fontSize = 13.sp
                )
            }

            // FEATURE CARDS
            FeatureCard(
                symbol = "⌕",
                title = "Ask anything",
                description = "Ask questions in natural language " +
                        "and explore your PDF conversationally.",
                accent = Color(0xFFAA91FF)
            )

            FeatureCard(
                symbol = "▤",
                title = "Understand faster",
                description = "Turn lengthy reading into focused " +
                        "answers and useful explanations.",
                accent = Color(0xFF7DD3FC)
            )

            FeatureCard(
                symbol = "◎",
                title = "Answers with context",
                description = "Review available source excerpts " +
                        "and page references alongside responses.",
                accent = Color(0xFF6EE7B7)
            )

            // PRIVACY / WORKFLOW FOOTER
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.Surface,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("✦", color = colors.VioletLight, fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "One document. One focused conversation.",
                            color = colors.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = "Upload your PDF to get started. " +
                                    "Answers depend on the document and " +
                                    "the capabilities of your connected AI service.",
                            color = colors.TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FeatureCard(
    symbol: String,
    title: String,
    description: String,
    accent: Color
) {
    val colors = PremiumColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.Surface
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = accent.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(15.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    color = accent,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.width(15.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    color = colors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    color = colors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

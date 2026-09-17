package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.AiChatViewModel
import com.example.CrmUiState
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicPrimary
import com.example.ui.theme.CosmicSecondary
import com.example.ui.theme.CosmicTertiary
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel,
    crmState: CrmUiState,
    totalSales: Double,
    onUpgradeRequested: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val aiUsageText by viewModel.aiUsageText.collectAsState()
    val showLimitAlert by viewModel.showLimitAlert.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    var textInput by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom when messages list changes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Suggestion chips list
    val suggestions = listOf(
        "Como aumentar meu ticket médio?",
        "Análise de estoque crítico",
        "Sugestões para fidelidade",
        "Idéias para novos Cupons"
    )

    var notifyMe by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = CosmicPrimary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = "Inteligência Artificial",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "DeepSeek v4 Flash",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = CosmicBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // Centered Image Logo (nova.png) breathing elegantly
                Image(
                    painter = painterResource(id = R.drawable.nova),
                    contentDescription = "Nova CRM Logo",
                    modifier = Modifier
                        .size(96.dp)
                        .scale(logoScale)
                        .padding(bottom = 12.dp)
                )

                // Glowing Badge with dynamic animated theme
                Surface(
                    color = CosmicSecondary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(CosmicSecondary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EM DESENVOLVIMENTO",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = CosmicSecondary,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                Text(
                    text = "Assistente Inteligente Nova CRM",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(
                    text = "O DeepSeek v4 Flash está sendo integrado na Nova para automatizar sua rotina de vendas e estoque, trazendo tomada de decisão baseada em IA de ponta direto para o seu bolso.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(bottom = 28.dp)
                )

                // Beautiful features card styled with high-contrast elements
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderDark.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 14.dp)) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CosmicPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PRÓXIMAS ATUALIZAÇÕES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CosmicPrimary,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Feature 1
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 12.dp)) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(6.dp)
                                    .background(CosmicSecondary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Otimização Autônoma de Estoque", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("Previsão automática de rupturas e sugestões de reabastecimento.", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        // Feature 2
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 12.dp)) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(6.dp)
                                    .background(CosmicSecondary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Previsões de Escala e Receita", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("Aprenda o melhor momento de lançar campanhas e cupons de fidelidade.", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        // Feature 3
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(6.dp)
                                    .background(CosmicSecondary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Atendimento com Agente Autónomo", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("Respostas polidas e precisas de suporte baseadas no DeepSeek v4.", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Interactive beta registration button
                Button(
                    onClick = {
                        notifyMe = !notifyMe
                        if (notifyMe) {
                            Toast.makeText(context, "Preferência salva! Você receberá acesso beta em breve.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (notifyMe) CosmicPrimary.copy(alpha = 0.15f) else CosmicPrimary
                    ),
                    border = if (notifyMe) androidx.compose.foundation.BorderStroke(1.dp, CosmicPrimary) else null,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("beta_notify_button")
                ) {
                    Text(
                        text = if (notifyMe) "✓ ACESSO BETA SOLICITADO" else "NOTIFICAR-ME QUANDO ESTIVER PRONTO",
                        fontWeight = FontWeight.Bold,
                        color = if (notifyMe) CosmicPrimary else Color.White,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }

    // Modal dialogue for rate limit exceeds
    if (showLimitAlert) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLimitAlert() },
            icon = { Icon(Icons.Default.Warning, contentDescription = "Limit Exceeded", tint = CosmicPrimary, modifier = Modifier.size(40.dp)) },
            title = { Text("Quotas de IA Atingida!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Text(
                    "Seu plano atual atingiu a cota diária de perguntas para o assistente virtual. Faça o upgrade por R$ 29/mês para liberar 45 perguntas/dia e expandir seu negócio!",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissLimitAlert()
                        onUpgradeRequested()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ElectricBolt, contentDescription = "Upgrade", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("UPGRADE PRO")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLimitAlert() }) {
                    Text("OK", color = CosmicSecondary)
                }
            },
            containerColor = CosmicBackground
        )
    }
}

@Composable
fun ChatBubbleItem(message: com.example.AiMessage) {
    val isUser = message.sender == "comerciante"
    val bubbleColor = if (isUser) CosmicPrimary else MaterialTheme.colorScheme.surface
    val textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
    val bubbleBorderModifier = if (!isUser) {
        Modifier.border(1.dp, BorderDark.copy(alpha = 0.5f), RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 2.dp,
            bottomEnd = 16.dp
        ))
    } else Modifier

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // AI avatar circle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(CosmicTertiary.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, CosmicTertiary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("AI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CosmicTertiary)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 16.dp
            ),
            modifier = Modifier
                .widthIn(max = 290.dp)
                .then(bubbleBorderModifier),
            shadowElevation = 1.dp
        ) {
            Box(modifier = Modifier.padding(14.dp)) {
                if (message.isThinking) {
                    ThinkingIndicator()
                } else {
                    // Custom Markdown renderer for **bold text**
                    Text(
                        text = parseMarkdown(message.text, !isUser),
                        fontSize = 13.sp,
                        color = textColor,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

@Composable
fun ThinkingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 600
                0.2f at 0
                1f at 200
                0.2f at 400
            },
            repeatMode = RepeatMode.Restart
        )
    )
    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 600
                0.2f at 100
                1f at 300
                0.2f at 500
            },
            repeatMode = RepeatMode.Restart
        )
    )
    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 600
                0.2f at 200
                1f at 400
                0.2f at 600
            },
            repeatMode = RepeatMode.Restart
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("Analisando dados", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.width(4.dp))
        Box(modifier = Modifier.size(6.dp * dot1Scale).background(CosmicPrimary, CircleShape))
        Box(modifier = Modifier.size(6.dp * dot2Scale).background(CosmicPrimary, CircleShape))
        Box(modifier = Modifier.size(6.dp * dot3Scale).background(CosmicPrimary, CircleShape))
    }
}

// Simple and bug-free Custom bold-pair parser for Markdown "**" indicators
fun parseMarkdown(text: String, isAiResponse: Boolean): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("**")
        for (i in parts.indices) {
            if (i % 2 == 1) {
                val boldColor = if (isAiResponse) CosmicPrimary else CosmicSecondary
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = boldColor)) {
                    append(parts[i])
                }
            } else {
                append(parts[i])
            }
        }
    }
}

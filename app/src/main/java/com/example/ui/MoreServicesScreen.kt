package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Client
import com.example.CrmUiState
import com.example.CrmViewModel
import com.example.R
import com.example.SupportChatViewModel
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicPrimary
import com.example.ui.theme.TextMuted
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreServicesScreen(
    crmState: CrmUiState,
    crmVm: CrmViewModel,
    supportChatVm: SupportChatViewModel,
    totalSales: Double,
    merchantId: String = "",
    onSignOutClick: () -> Unit
) {
    var activeModal by remember { mutableStateOf<ServiceModalType?>(null) }
    var showUpgradeDialog by remember { mutableStateOf(false) }
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val merchantProfile = crmState.merchantProfile

    val rawPlan = merchantProfile.subscription.plan.trim()
    val isProPlan = rawPlan.equals("pro", ignoreCase = true)
    val planDisplayName = when (rawPlan.lowercase()) {
        "pro" -> "Plano PRO"
        "enterprise" -> "Plano Enterprise"
        "starter" -> "Plano Starter"
        "free", "gratuito", "" -> "Plano Gratuito"
        else -> "Plano ${rawPlan.uppercase()}"
    }

    if (showSignOutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmDialog = false },
            title = {
                Text("Sair da Conta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text("Tem certeza que deseja encerrar sua sessão no aplicativo Nova CRM Mobile?", fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirmDialog = false
                        onSignOutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("SIM, SAIR", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmDialog = false }) {
                    Text("CANCELAR")
                }
            }
        )
    }

    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = CosmicPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Melhorar ou Alterar Plano", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Seu plano atual é $planDisplayName.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Para alterar, renovar ou contratar novos recursos para o seu plano, acesse o site oficial da plataforma:",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "novacrm.com.br",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = CosmicPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUpgradeDialog = false
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://novacrm.com.br"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Acesse novacrm.com.br no seu navegador", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary)
                ) {
                    Text("ACESSAR NOVACRM.COM.BR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpgradeDialog = false }) {
                    Text("FECHAR")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .size(34.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.nova),
                                contentDescription = "Nova CRM Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Mais Serviços & Módulos",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "Central de Ferramentas Nova CRM",
                                fontSize = 11.sp,
                                color = CosmicPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSignOutConfirmDialog = true },
                        modifier = Modifier.testTag("top_app_bar_logout")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sair da Conta",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = CosmicBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
        ) {
            // 1. Chat & Support Group
            item {
                SectionTitle("CHATS & ATENDIMENTO")
            }

            item {
                ServiceTileCard(
                    title = "Chat com Clientes (SAC)",
                    subtitle = "Conversas em tempo real com compradores e clientes da loja",
                    icon = Icons.Default.Chat,
                    iconBgColor = CosmicPrimary,
                    badgeText = "AO VIVO",
                    onClick = { activeModal = ServiceModalType.CUSTOMER_CHATS },
                    tag = "tile_customer_chats"
                )
            }

            item {
                ServiceTileCard(
                    title = "Atendimento e Suporte Nova CRM",
                    subtitle = "Canal direto de suporte em tempo real com nossa equipe",
                    icon = Icons.Default.SupportAgent,
                    iconBgColor = Color(0xFF10B981),
                    badgeText = "ONLINE",
                    onClick = { activeModal = ServiceModalType.SUPPORT_CHAT },
                    tag = "tile_support_chat"
                )
            }

            // 2. Sales & Store Management Group
            item {
                SectionTitle("GESTÃO DE VENDAS & LOJA")
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ServiceGridCard(
                        title = "Pedidos & Vendas",
                        infoText = "${crmState.orders.size} registrados",
                        icon = Icons.Default.ReceiptLong,
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f),
                        onClick = { activeModal = ServiceModalType.ORDERS },
                        tag = "grid_orders"
                    )

                    ServiceGridCard(
                        title = "Cupons de Desconto",
                        infoText = "Promoções ativas",
                        icon = Icons.Default.ConfirmationNumber,
                        color = Color(0xFFEC4899),
                        modifier = Modifier.weight(1f),
                        onClick = { activeModal = ServiceModalType.COUPONS },
                        tag = "grid_coupons"
                    )
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ServiceGridCard(
                        title = "Vitrine & Configurações",
                        infoText = "Dados da sua loja",
                        icon = Icons.Default.Storefront,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f),
                        onClick = { activeModal = ServiceModalType.STORE_SETTINGS },
                        tag = "grid_store_settings"
                    )

                    ServiceGridCard(
                        title = "Relatório PDF",
                        infoText = "Exportar métricas",
                        icon = Icons.Default.PictureAsPdf,
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            Toast.makeText(context, "Gerando resumo do relatório...", Toast.LENGTH_SHORT).show()
                            activeModal = ServiceModalType.REPORT
                        },
                        tag = "grid_report"
                    )
                }
            }

            // 3. Account & Subscription Section
            item {
                SectionTitle("MINHA CONTA & ASSINATURA")
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .clickable { activeModal = ServiceModalType.ACCOUNT }
                        .testTag("tile_account")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(CosmicPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Plano",
                                tint = CosmicPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Plano Identificado: $planDisplayName",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isProPlan) "Assinatura PRO ativa • Gerenciar em novacrm.com.br" else "Para alterar o plano, acesse novacrm.com.br",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Button(
                            onClick = { showUpgradeDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (isProPlan) "GERENCIAR" else "MELHORAR", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // Logout Section
            item {
                SectionTitle("SESSÃO & LOGOUT")
            }

            item {
                Button(
                    onClick = { showSignOutConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("more_sign_out_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Sair",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Sair da Conta Nova CRM", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }

            // 4. Footer branding with logo from `more.svg` (ic_nav_more)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.nova),
                            contentDescription = "Nova CRM Logo",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_nav_more),
                            contentDescription = "More Logo",
                            tint = CosmicPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nova CRM Mobile v2.4.0",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Text(
                        text = "Desenvolvido com Google AI Studio • Todos os direitos reservados",
                        fontSize = 10.sp,
                        color = TextMuted.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    // Modal Sheet or Full Screen overlay based on activeModal
    when (activeModal) {
        ServiceModalType.CUSTOMER_CHATS -> {
            FullModalDialog(onDismiss = { activeModal = null }) {
                CustomerChatsSubScreen(
                    crmState = crmState,
                    supportChatVm = supportChatVm,
                    merchantId = merchantId,
                    onClose = { activeModal = null }
                )
            }
        }
        ServiceModalType.SUPPORT_CHAT -> {
            FullModalDialog(onDismiss = { activeModal = null }) {
                SupportChatSubScreen(
                    supportChatVm = supportChatVm,
                    merchantProfile = merchantProfile,
                    onClose = { activeModal = null }
                )
            }
        }
        ServiceModalType.ORDERS -> {
            FullModalDialog(onDismiss = { activeModal = null }) {
                OrdersSubScreen(crmState = crmState, onClose = { activeModal = null })
            }
        }
        ServiceModalType.COUPONS -> {
            FullModalDialog(onDismiss = { activeModal = null }) {
                CouponsSubScreen(onClose = { activeModal = null })
            }
        }
        ServiceModalType.STORE_SETTINGS -> {
            FullModalDialog(onDismiss = { activeModal = null }) {
                StoreSettingsSubScreen(merchantProfile = merchantProfile, onClose = { activeModal = null })
            }
        }
        ServiceModalType.REPORT -> {
            FullModalDialog(onDismiss = { activeModal = null }) {
                ReportSubScreen(crmState = crmState, totalSales = totalSales, onClose = { activeModal = null })
            }
        }
        ServiceModalType.ACCOUNT -> {
            FullModalDialog(onDismiss = { activeModal = null }) {
                AccountSubScreen(
                    crmState = crmState,
                    onSignOutClick = onSignOutClick,
                    onClose = { activeModal = null }
                )
            }
        }
        null -> {}
    }
}

enum class ServiceModalType {
    CUSTOMER_CHATS,
    SUPPORT_CHAT,
    ORDERS,
    COUPONS,
    STORE_SETTINGS,
    REPORT,
    ACCOUNT
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        color = CosmicPrimary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun ServiceTileCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    badgeText: String? = null,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBgColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconBgColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (badgeText != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = iconBgColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = iconBgColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ServiceGridCard(
    title: String,
    infoText: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = infoText,
                fontSize = 11.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullModalDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("full_modal_overlay"),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

// ======================= Sub-screens for Modals =======================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportChatSubScreen(
    supportChatVm: SupportChatViewModel,
    merchantProfile: com.example.Merchant,
    onClose: () -> Unit
) {
    val messages by supportChatVm.messages.collectAsState()
    var textInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        supportChatVm.loadChat("system_merchant", "support")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = "Suporte", tint = Color(0xFF10B981))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Suporte & Atendimento", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Equipe Nova CRM Mobile", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Digite sua dúvida para o suporte...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("support_input"),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                supportChatVm.sendMessage(textInput.trim())
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .background(CosmicPrimary, CircleShape)
                            .testTag("send_support_msg")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Chat, contentDescription = "Chat", tint = TextMuted, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Bem-vindo ao Suporte da Nova CRM!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Envie sua mensagem acima e nossa equipe responderá em breve.", fontSize = 12.sp, color = TextMuted)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        val isMe = msg.sender == "merchant"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                color = if (isMe) CosmicPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersSubScreen(crmState: CrmUiState, onClose: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pedidos & Vendas (${crmState.orders.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }
            )
        }
    ) { padding ->
        if (crmState.orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum pedido registrado até o momento.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(crmState.orders) { order ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = order.customerEmail.ifBlank { "Cliente #" + order.id.take(6) },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Status: " + order.status.uppercase(),
                                    fontSize = 11.sp,
                                    color = if (order.status.lowercase() == "concluído") Color(0xFF10B981) else CosmicPrimary
                                )
                            }
                            Text(
                                text = "R$ " + String.format(Locale.getDefault(), "%,.2f", order.total),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = CosmicPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponsSubScreen(onClose: () -> Unit) {
    var couponCode by remember { mutableStateOf("") }
    val coupons = remember { mutableStateListOf("DESCONTO10", "PRIMEIRACOMPRA", "FRETEGRATIS") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cupons de Desconto", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Criar Novo Cupom", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = couponCode,
                        onValueChange = { couponCode = it.uppercase() },
                        label = { Text("Código do Cupom (ex: FESTA15)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (couponCode.isNotBlank()) {
                                coupons.add(couponCode.trim())
                                couponCode = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("ADICIONAR", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("Cupons Ativos da Loja:", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(coupons) { code ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ConfirmationNumber, contentDescription = "Cupom", tint = CosmicPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(code, fontWeight = FontWeight.Bold)
                            }
                            Text("10% OFF", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreSettingsSubScreen(merchantProfile: com.example.Merchant, onClose: () -> Unit) {
    val context = LocalContext.current
    val storeConfig = merchantProfile.storeConfig
    var storeName by remember { mutableStateOf(storeConfig["storeName"] ?: "Minha Loja Nova CRM") }
    var whatsapp by remember { mutableStateOf(storeConfig["whatsapp"] ?: "+55 11 99999-8888") }
    var category by remember { mutableStateOf(storeConfig["category"] ?: "Varejo & Serviços") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações da Loja", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("Nome da Loja / Empresa") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = whatsapp,
                onValueChange = { whatsapp = it },
                label = { Text("WhatsApp de Atendimento") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoria Principal") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Configurações da loja salvas com sucesso!", Toast.LENGTH_SHORT).show()
                    onClose()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SALVAR CONFIGURAÇÕES", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSubScreen(crmState: CrmUiState, totalSales: Double, onClose: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relatório & Resumo Executivo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Resumo Geral das Operações", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("• Faturamento Geral: R$ " + String.format(Locale.getDefault(), "%,.2f", totalSales))
                    Text("• Pedidos Registrados: ${crmState.orders.size}")
                    Text("• Clientes Cadastrados: ${crmState.clients.size}")
                    Text("• Itens em Estoque: ${crmState.products.size}")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSubScreen(
    crmState: CrmUiState,
    onSignOutClick: () -> Unit,
    onClose: () -> Unit
) {
    val merchant = crmState.merchantProfile
    val context = LocalContext.current
    var showUpgradeDialog by remember { mutableStateOf(false) }

    val rawPlan = merchant.subscription.plan.trim()
    val isProPlan = rawPlan.equals("pro", ignoreCase = true)
    val planDisplayName = when (rawPlan.lowercase()) {
        "pro" -> "Plano PRO"
        "enterprise" -> "Plano Enterprise"
        "starter" -> "Plano Starter"
        "free", "gratuito", "" -> "Plano Gratuito"
        else -> "Plano ${rawPlan.uppercase()}"
    }

    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = CosmicPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Melhorar ou Alterar Plano", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Seu plano atual é $planDisplayName.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Para alterar, renovar ou contratar novos recursos para o seu plano, acesse o site oficial:",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "novacrm.com.br",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = CosmicPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUpgradeDialog = false
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://novacrm.com.br"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Acesse novacrm.com.br", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary)
                ) {
                    Text("ACESSAR NOVACRM.COM.BR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpgradeDialog = false }) {
                    Text("FECHAR")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minha Conta & Assinatura", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = "Conta", tint = CosmicPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Perfil de Lojista Nova CRM", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Plano Identificado: $planDisplayName", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CosmicPrimary)
                    Text("Status da Conta: Ativa no Servidor", fontSize = 12.sp, color = TextMuted)
                }
            }

            Button(
                onClick = { showUpgradeDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isProPlan) "GERENCIAR PLANO EM NOVACRM.COM.BR" else "MELHORAR PLANO EM NOVACRM.COM.BR", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    onClose()
                    onSignOutClick()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("SAIR DA CONTA", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerChatsSubScreen(
    crmState: CrmUiState,
    supportChatVm: SupportChatViewModel,
    merchantId: String,
    onClose: () -> Unit
) {
    var selectedClient by remember { mutableStateOf<Client?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(selectedClient) {
        val client = selectedClient
        if (client != null) {
            supportChatVm.loadChat(merchantId, client.id)
        }
    }

    if (selectedClient != null) {
        RealtimeSupportChatPanel(
            client = selectedClient!!,
            viewModel = supportChatVm,
            onBackClick = {
                supportChatVm.setMerchantTyping(false)
                selectedClient = null
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Chat com Clientes (SAC)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("${crmState.clients.size} clientes no cadastro", fontSize = 11.sp, color = CosmicPrimary, fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar cliente para conversar...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("customer_chat_search"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                val filteredClients = crmState.clients.filter { client ->
                    searchQuery.isBlank() ||
                    client.name.contains(searchQuery, ignoreCase = true) ||
                    client.email.contains(searchQuery, ignoreCase = true) ||
                    client.phone.contains(searchQuery, ignoreCase = true)
                }

                if (filteredClients.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = TextMuted, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Nenhum cliente encontrado", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Cadastre clientes na aba 'Clientes' para iniciar atendimento ao cliente.", fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(horizontal = 24.dp))
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredClients) { client ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                    .clickable { selectedClient = client }
                                    .testTag("client_chat_item_${client.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(CosmicPrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = client.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp,
                                            color = CosmicPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = client.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = client.email.ifBlank { client.phone.ifBlank { "Cliente registrado" } },
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }

                                    Surface(
                                        color = CosmicPrimary,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Chat,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "ABRIR CHAT",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

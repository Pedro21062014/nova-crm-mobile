package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Client
import com.example.CrmUiState
import com.example.CrmViewModel
import com.example.Message
import com.example.R
import com.example.SupportChatViewModel
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicPrimary
import com.example.ui.theme.CosmicSecondary
import com.example.ui.theme.CosmicTertiary
import com.example.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    state: CrmUiState,
    crmViewModel: CrmViewModel,
    chatViewModel: SupportChatViewModel,
    merchantId: String
) {
    var activeChatClient by remember { mutableStateOf<Client?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Add Client form states
    var clientName by remember { mutableStateOf("") }
    var clientEmail by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(activeChatClient) {
        val client = activeChatClient
        if (client != null) {
            chatViewModel.loadChat(merchantId, client.id)
        }
    }

    if (activeChatClient != null) {
        // RENDER LIVE CONVERSATIONAL PANEL (RTDB)
        RealtimeSupportChatPanel(
            client = activeChatClient!!,
            viewModel = chatViewModel,
            onBackClick = {
                chatViewModel.setMerchantTyping(false) // clear typing before exit
                activeChatClient = null
            }
        )
    } else {
        // RENDER CLIENTS CRM DATABASE DIRECTORY
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
                                    "Central de Clientes",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    "Gestão de Contatos Nova CRM",
                                    fontSize = 11.sp,
                                    color = CosmicPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        clientName = ""
                        clientEmail = ""
                        clientPhone = ""
                        formError = null
                        showAddDialog = true
                    },
                    containerColor = CosmicPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("add_client_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Novo Cliente")
                }
            },
            containerColor = CosmicBackground
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Seus clientes integrados no CRM",
                    fontSize = 14.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                if (state.clients.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = "Sem clientes",
                                tint = TextMuted,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Nenhum cliente cadastrado ainda.",
                                fontSize = 14.sp,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("clients_list"),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(state.clients.reversed(), key = { it.id }) { item ->
                            ClientCardRow(
                                client = item,
                                onOpenChat = { activeChatClient = item }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue add Client
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = "Novo Cliente",
                        tint = CosmicPrimary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Adicionar Cliente", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.width(300.dp)
                ) {
                    if (formError != null) {
                        Text(
                            text = formError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Nome completo") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_client_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    OutlinedTextField(
                        value = clientEmail,
                        onValueChange = { clientEmail = it },
                        label = { Text("E-mail corporativo") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_client_email_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Telefone / WhatsApp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_client_phone_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (clientName.isBlank() || clientEmail.isBlank() || clientPhone.isBlank()) {
                            formError = "Preencha todos os campos obrigatórios."
                        } else if (!clientEmail.contains("@")) {
                            formError = "Insira um e-mail válido."
                        } else {
                            crmViewModel.addClient(
                                name = clientName.trim(),
                                email = clientEmail.trim(),
                                phone = clientPhone.trim()
                            ) { success ->
                                if (success) {
                                    showAddDialog = false
                                } else {
                                    formError = "Falha ao cadastrar cliente no Firestore."
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary),
                    modifier = Modifier.testTag("submit_client_button")
                ) {
                    Text("CADASTRAR", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("CANCELAR", color = CosmicPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun ClientCardRow(
    client: Client,
    onOpenChat: () -> Unit
) {
    val borderDarkColor = com.example.ui.theme.BorderDark
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderDarkColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .testTag("client_item_${client.id}"),
        onClick = onOpenChat
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = "Email", tint = TextMuted, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(client.email, fontSize = 11.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = "Telefone", tint = TextMuted, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(client.phone, fontSize = 11.sp, color = TextMuted)
                }
            }

            // Quick Chat shortcut
            IconButton(
                onClick = onOpenChat,
                modifier = Modifier
                    .size(42.dp)
                    .background(CosmicPrimary.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Chat de suporte",
                    tint = CosmicPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// LIVE SUPPORT CHAT VIEW WITH TYPING INDICES COMPILATION
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealtimeSupportChatPanel(
    client: Client,
    viewModel: SupportChatViewModel,
    onBackClick: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val typingStatus by viewModel.typingStatus.collectAsState()

    var chatTextInput by remember { mutableStateOf("") }

    val chatListState = rememberLazyListState()

    // Scroll to bottom when real-time messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            chatListState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = client.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        // Dynamic Typing status (RTDB status mapped)
                        AnimatedVisibility(
                            visible = typingStatus.customer,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Text(
                                text = "Digitando...",
                                fontSize = 11.sp,
                                color = CosmicSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.onBackground)
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
                .navigationBarsPadding()
        ) {
            // Live Messages Stream list view
            LazyColumn(
                state = chatListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    SupportChatBubble(message = msg)
                }

                // Customer is typing placeholder
                if (typingStatus.customer) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color.Gray.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("...", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp),
                                shadowElevation = 1.dp
                            ) {
                                Text(
                                    text = "Digitando...",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Chat input control panel
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatTextInput,
                        onValueChange = {
                            chatTextInput = it
                            // Triggers real-time typing indicator in RTDB!
                            viewModel.setMerchantTyping(it.isNotBlank())
                        },
                        placeholder = { Text("Responda ao cliente...", color = TextMuted, fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("support_chat_text_input"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CosmicBackground,
                            unfocusedContainerColor = CosmicBackground,
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (chatTextInput.isNotBlank()) {
                                val s = chatTextInput
                                chatTextInput = ""
                                viewModel.sendMessage(s)
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                Brush.linearGradient(listOf(CosmicPrimary, CosmicSecondary)),
                                shape = CircleShape
                            )
                            .testTag("support_send_button"),
                        enabled = chatTextInput.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SupportChatBubble(message: Message) {
    val isMerchant = message.sender.lowercase() == "merchant"
    val bubbleColor = if (isMerchant) CosmicPrimary else MaterialTheme.colorScheme.surface
    val textColor = if (isMerchant) Color.White else MaterialTheme.colorScheme.onSurface
    val borderDarkColor = com.example.ui.theme.BorderDark
    val bubbleBorderModifier = if (!isMerchant) {
        Modifier.border(1.dp, borderDarkColor.copy(alpha = 0.5f), RoundedCornerShape(
            topStart = 14.dp,
            topEnd = 14.dp,
            bottomStart = 2.dp,
            bottomEnd = 14.dp
        ))
    } else Modifier

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMerchant) Arrangement.End else Arrangement.Start
    ) {
        if (!isMerchant) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(CosmicSecondary.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, CosmicSecondary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("CL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CosmicSecondary)
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isMerchant) 14.dp else 2.dp,
                bottomEnd = if (isMerchant) 2.dp else 14.dp
            ),
            modifier = Modifier
                .widthIn(max = 240.dp)
                .then(bubbleBorderModifier),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = message.text,
                    fontSize = 12.sp,
                    color = textColor,
                    lineHeight = 16.sp
                )
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                    fontSize = 8.sp,
                    color = TextMuted,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .align(Alignment.End)
                )
            }
        }

        if (isMerchant) {
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

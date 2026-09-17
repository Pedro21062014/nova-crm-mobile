package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AuthState
import com.example.AuthViewModel
import com.example.R
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicPrimary
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private val TYPEWRITER_PHRASES = listOf(
    "Transforme conversas em vendas reais.",
    "Gestão ágil de clientes e estoque.",
    "Atendimento e SAC ao vivo em segundos.",
    "Controle financeiro na palma da mão.",
    "Seu ecossistema comercial inteligente."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: (String) -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(false) }

    // Typewriter state
    var typedText by remember { mutableStateOf("") }
    var showCursor by remember { mutableStateOf(true) }

    // Typewriter animation loop
    LaunchedEffect(Unit) {
        // Blinking cursor coroutine
        launch {
            while (isActive) {
                delay(450)
                showCursor = !showCursor
            }
        }

        // Typing and deleting loop
        var phraseIdx = 0
        while (isActive) {
            val currentPhrase = TYPEWRITER_PHRASES[phraseIdx]
            
            // Type characters one by one
            for (i in 1..currentPhrase.length) {
                typedText = currentPhrase.substring(0, i)
                delay(45)
            }
            
            // Pause at complete phrase
            delay(2400)
            
            // Delete characters one by one
            for (i in currentPhrase.length downTo 0) {
                typedText = currentPhrase.substring(0, i)
                delay(20)
            }
            
            delay(300)
            phraseIdx = (phraseIdx + 1) % TYPEWRITER_PHRASES.size
        }
    }

    // Navigation trigger onSuccess
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onAuthSuccess((authState as AuthState.Success).uid)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Minimalist Brand Emblem
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, BorderDark, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.nova),
                    contentDescription = "Nova CRM Logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Title
            Text(
                text = "Nova CRM",
                fontFamily = PlusJakartaSans,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                letterSpacing = (-0.5).sp
            )

            // Minimalist Typewriter container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(top = 8.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = typedText,
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (showCursor) "▌" else " ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                        color = CosmicPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Auth Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Minimal Tab Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 18.dp)
                    ) {
                        // Entrar Tab
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isSignUp = false },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Entrar",
                                fontFamily = PlusJakartaSans,
                                fontSize = 14.sp,
                                fontWeight = if (!isSignUp) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isSignUp) TextWhite else TextMuted,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(if (!isSignUp) CosmicPrimary else Color.Transparent)
                            )
                        }

                        // Cadastrar Tab
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isSignUp = true },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Criar conta",
                                fontFamily = PlusJakartaSans,
                                fontSize = 14.sp,
                                fontWeight = if (isSignUp) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSignUp) TextWhite else TextMuted,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(if (isSignUp) CosmicPrimary else Color.Transparent)
                            )
                        }
                    }

                    // Error Output
                    AnimatedVisibility(visible = authState is AuthState.Error) {
                        val msg = (authState as? AuthState.Error)?.message ?: ""
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = msg,
                                fontFamily = PlusJakartaSans,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = {
                            Text(
                                "E-mail",
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp
                            )
                        },
                        placeholder = {
                            Text(
                                "seu@email.com",
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp,
                                color = TextMuted.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedContainerColor = Color(0xFFFAFAFA),
                            unfocusedContainerColor = Color(0xFFFAFAFA),
                            focusedLabelColor = CosmicPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = {
                            Text(
                                "Senha",
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp
                            )
                        },
                        placeholder = {
                            Text(
                                "••••••••",
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp,
                                color = TextMuted.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Senha",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Ocultar senha" else "Ver senha",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    if (isSignUp) viewModel.signUp(email, password) else viewModel.login(email, password)
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedContainerColor = Color(0xFFFAFAFA),
                            unfocusedContainerColor = Color(0xFFFAFAFA),
                            focusedLabelColor = CosmicPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (isSignUp) {
                                viewModel.signUp(email, password)
                            } else {
                                viewModel.login(email, password)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CosmicPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_button"),
                        enabled = authState !is AuthState.Loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (isSignUp) "Cadastrar loja" else "Entrar no sistema",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Text(
                text = "Nova CRM • Plataforma de Gestão Comercial",
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp,
                color = TextMuted.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal
            )
        }
    }
}

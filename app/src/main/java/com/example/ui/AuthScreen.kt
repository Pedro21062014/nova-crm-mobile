package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.AuthState
import com.example.AuthViewModel
import com.example.R
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicPrimary
import com.example.ui.theme.CosmicSecondary
import com.example.ui.theme.CosmicTertiary
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private val TYPEWRITER_PHRASES = listOf(
    "Transforme conversas em vendas reais.",
    "Gestão ágil de clientes e estoque.",
    "Atendimento e SAC ao vivo em segundos.",
    "Controle financeiro na palma da mão.",
    "Seu ecossistema comercial inteligente."
)

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: (String) -> Unit
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val resetMessage by viewModel.resetMessage.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Typewriter state
    var typedText by remember { mutableStateOf("") }

    // Smooth cursor pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "CursorTransition")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CursorAlpha"
    )

    // Typewriter animation loop
    LaunchedEffect(Unit) {
        var phraseIdx = 0
        while (isActive) {
            val currentPhrase = TYPEWRITER_PHRASES[phraseIdx]
            for (i in 1..currentPhrase.length) {
                typedText = currentPhrase.substring(0, i)
                delay(42)
            }
            delay(2300)
            for (i in currentPhrase.length downTo 0) {
                typedText = currentPhrase.substring(0, i)
                delay(18)
            }
            delay(280)
            phraseIdx = (phraseIdx + 1) % TYPEWRITER_PHRASES.size
        }
    }

    // Navigation trigger onSuccess
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onAuthSuccess((authState as AuthState.Success).uid)
        }
    }

    val submitAction = {
        focusManager.clearFocus()
        if (isSignUp) {
            viewModel.signUp(email, password, confirmPassword)
        } else {
            viewModel.login(email, password)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBackground)
    ) {
        val isWideScreen = maxWidth >= 660.dp

        // Subtle atmospheric minimalist canvas
        MinimalistAtmosphereBackground()

        if (isWideScreen) {
            // Landscape / Tablet Split Editorial Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 40.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 420.dp)
                        .padding(end = 36.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    BrandHeader(
                        isSignUp = isSignUp,
                        typedText = typedText,
                        cursorAlpha = cursorAlpha,
                        centered = false
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    MinimalistHighlightsRow()
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 440.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    AuthFormCard(
                        isSignUp = isSignUp,
                        onModeChange = { signUp ->
                            if (isSignUp != signUp) {
                                isSignUp = signUp
                                viewModel.clearFeedback()
                            }
                        },
                        email = email,
                        onEmailChange = {
                            email = it
                            if (authState is AuthState.Error) viewModel.clearFeedback()
                        },
                        password = password,
                        onPasswordChange = {
                            password = it
                            if (authState is AuthState.Error) viewModel.clearFeedback()
                        },
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = {
                            confirmPassword = it
                            if (authState is AuthState.Error) viewModel.clearFeedback()
                        },
                        passwordVisible = passwordVisible,
                        onTogglePasswordVisible = { passwordVisible = !passwordVisible },
                        confirmPasswordVisible = confirmPasswordVisible,
                        onToggleConfirmPasswordVisible = { confirmPasswordVisible = !confirmPasswordVisible },
                        authState = authState,
                        resetMessage = resetMessage,
                        onForgotPasswordClick = { showResetDialog = true },
                        onSubmit = submitAction
                    )
                }
            }
        } else {
            // Portrait Mobile Centered Layout
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 430.dp)
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BrandHeader(
                        isSignUp = isSignUp,
                        typedText = typedText,
                        cursorAlpha = cursorAlpha,
                        centered = true
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    AuthFormCard(
                        isSignUp = isSignUp,
                        onModeChange = { signUp ->
                            if (isSignUp != signUp) {
                                isSignUp = signUp
                                viewModel.clearFeedback()
                            }
                        },
                        email = email,
                        onEmailChange = {
                            email = it
                            if (authState is AuthState.Error) viewModel.clearFeedback()
                        },
                        password = password,
                        onPasswordChange = {
                            password = it
                            if (authState is AuthState.Error) viewModel.clearFeedback()
                        },
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = {
                            confirmPassword = it
                            if (authState is AuthState.Error) viewModel.clearFeedback()
                        },
                        passwordVisible = passwordVisible,
                        onTogglePasswordVisible = { passwordVisible = !passwordVisible },
                        confirmPasswordVisible = confirmPasswordVisible,
                        onToggleConfirmPasswordVisible = { confirmPasswordVisible = !confirmPasswordVisible },
                        authState = authState,
                        resetMessage = resetMessage,
                        onForgotPasswordClick = { showResetDialog = true },
                        onSubmit = submitAction
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Secondary quick switch link
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                isSignUp = !isSignUp
                                viewModel.clearFeedback()
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isSignUp) "Já possui uma conta? " else "Novo por aqui? ",
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                        Text(
                            text = if (isSignUp) "Fazer login" else "Criar conta grátis",
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmicPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Nova CRM • Gestão Comercial Segura",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = TextMuted.copy(alpha = 0.65f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showResetDialog) {
        ForgotPasswordDialog(
            initialEmail = email,
            onDismiss = { showResetDialog = false },
            onConfirm = { targetEmail ->
                showResetDialog = false
                viewModel.sendPasswordReset(targetEmail)
            }
        )
    }
}

@Composable
private fun MinimalistAtmosphereBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Top-right soft indigo ambient glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    CosmicPrimary.copy(alpha = 0.085f),
                    CosmicPrimary.copy(alpha = 0.02f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.9f, size.height * 0.08f),
                radius = size.maxDimension * 0.55f
            ),
            radius = size.maxDimension * 0.55f,
            center = Offset(size.width * 0.9f, size.height * 0.08f)
        )

        // Bottom-left subtle sky glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    CosmicTertiary.copy(alpha = 0.07f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.1f, size.height * 0.92f),
                radius = size.maxDimension * 0.48f
            ),
            radius = size.maxDimension * 0.48f,
            center = Offset(size.width * 0.1f, size.height * 0.92f)
        )

        // Ultra-subtle architectural dot pattern in upper area
        val step = 28.dp.toPx()
        val maxY = size.height * 0.42f
        var y = step
        while (y < maxY) {
            var x = step
            val fadeFactor = 1f - (y / maxY)
            while (x < size.width) {
                drawCircle(
                    color = CosmicPrimary.copy(alpha = 0.045f * fadeFactor),
                    radius = 1.2.dp.toPx(),
                    center = Offset(x, y)
                )
                x += step
            }
            y += step
        }
    }
}

@Composable
private fun BrandHeader(
    isSignUp: Boolean,
    typedText: String,
    cursorAlpha: Float,
    centered: Boolean
) {
    val alignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
    val textAlign = if (centered) TextAlign.Center else TextAlign.Start

    Column(
        horizontalAlignment = alignment,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Brand Logo + Minimal Badge Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (centered) Arrangement.Center else Arrangement.Start
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, CosmicPrimary.copy(alpha = 0.12f)),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.nova),
                        contentDescription = "Nova CRM Logo",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subtle Eyebrow Pill
        Surface(
            color = Color.White.copy(alpha = 0.85f),
            shape = CircleShape,
            border = BorderStroke(1.dp, BorderDark)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(CosmicSecondary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NOVA CRM",
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    letterSpacing = 1.4.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Animated Headline based on Login vs Cadastro
        AnimatedContent(
            targetState = isSignUp,
            transitionSpec = {
                (fadeIn(tween(240)) + slideInVertically(tween(240)) { if (targetState) 14 else -14 })
                    .togetherWith(fadeOut(tween(180)) + slideOutVertically(tween(180)) { if (targetState) -14 else 14 })
            },
            label = "HeadlineTransition"
        ) { signUpMode ->
            Text(
                text = if (signUpMode) "Crie sua loja" else "Bem-vindo de volta",
                fontFamily = PlusJakartaSans,
                fontSize = 27.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                letterSpacing = (-0.7).sp,
                textAlign = textAlign
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Typewriter Effect with smooth fading cursor and fixed height to avoid layout jump
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 42.dp),
            contentAlignment = if (centered) Alignment.Center else Alignment.CenterStart
        ) {
            Text(
                text = buildAnnotatedString {
                    append(typedText)
                    withStyle(
                        SpanStyle(
                            color = CosmicPrimary.copy(alpha = cursorAlpha),
                            fontWeight = FontWeight.Light
                        )
                    ) {
                        append("|")
                    }
                },
                fontFamily = PlusJakartaSans,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted,
                textAlign = textAlign,
                modifier = Modifier.padding(horizontal = if (centered) 8.dp else 0.dp)
            )
        }
    }
}

@Composable
private fun MinimalistHighlightsRow() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val items = listOf(
            "Vendas, clientes e estoque integrados",
            "Atendimento SAC em tempo real",
            "Insights e métricas financeiras na nuvem"
        )
        items.forEach { label ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = CosmicSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AuthFormCard(
    isSignUp: Boolean,
    onModeChange: (Boolean) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisible: () -> Unit,
    confirmPasswordVisible: Boolean,
    onToggleConfirmPasswordVisible: () -> Unit,
    authState: AuthState,
    resetMessage: String?,
    onForgotPasswordClick: () -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, BorderDark.copy(alpha = 0.85f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = CosmicPrimary.copy(alpha = 0.08f),
                spotColor = CosmicPrimary.copy(alpha = 0.08f)
            )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Segmented Pill Switcher
            SlidingAuthModeToggle(
                isSignUp = isSignUp,
                onSelect = onModeChange
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Error Feedback Banner
            AnimatedVisibility(
                visible = authState is AuthState.Error,
                enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                val msg = (authState as? AuthState.Error)?.message ?: ""
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            fontFamily = PlusJakartaSans,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Password Reset Success Banner
            AnimatedVisibility(
                visible = !resetMessage.isNullOrBlank(),
                enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                Surface(
                    color = CosmicSecondary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = CosmicSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = resetMessage.orEmpty(),
                            fontFamily = PlusJakartaSans,
                            color = Color(0xFF065F46),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Email Field
            MinimalAuthTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "E-mail",
                placeholder = "voce@sualoja.com.br",
                leadingIcon = Icons.Outlined.Email,
                iconContentDescription = "Ícone de e-mail",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            MinimalAuthTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Senha",
                placeholder = if (isSignUp) "Mínimo 6 caracteres" else "Sua senha de acesso",
                leadingIcon = Icons.Outlined.Lock,
                iconContentDescription = "Ícone de senha",
                keyboardType = KeyboardType.Password,
                imeAction = if (isSignUp) ImeAction.Next else ImeAction.Done,
                onImeDone = { if (!isSignUp) onSubmit() },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisible) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar senha" else "Ver senha",
                            tint = TextMuted,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input")
            )

            // Login mode: Forgot password link
            AnimatedVisibility(
                visible = !isSignUp,
                enter = fadeIn(tween(180)) + expandVertically(tween(180)),
                exit = fadeOut(tween(120)) + shrinkVertically(tween(120))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(
                        onClick = onForgotPasswordClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("forgot_password_button")
                    ) {
                        Text(
                            text = "Esqueci minha senha",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CosmicPrimary
                        )
                    }
                }
            }

            // Sign-Up mode: Password strength bar + Confirm Password
            AnimatedVisibility(
                visible = isSignUp,
                enter = fadeIn(tween(240)) + expandVertically(tween(240)),
                exit = fadeOut(tween(180)) + shrinkVertically(tween(180))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (password.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        PasswordStrengthIndicator(password = password)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val passwordsMismatch = confirmPassword.isNotEmpty() && confirmPassword != password
                    MinimalAuthTextField(
                        value = confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = "Confirmar senha",
                        placeholder = "Repita a senha",
                        leadingIcon = Icons.Outlined.Lock,
                        iconContentDescription = "Confirmar senha",
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        onImeDone = onSubmit,
                        isError = passwordsMismatch,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = onToggleConfirmPasswordVisible) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (confirmPasswordVisible) "Ocultar confirmação de senha" else "Ver confirmação de senha",
                                    tint = TextMuted,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_password_input")
                    )

                    if (passwordsMismatch) {
                        Text(
                            text = "As senhas não coincidem",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            if (!isSignUp) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Primary Submit CTA
            val isLoading = authState is AuthState.Loading
            Button(
                onClick = onSubmit,
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = CosmicPrimary.copy(alpha = 0.5f),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                CosmicPrimary,
                                Color(0xFF4338CA)
                            )
                        )
                    )
                    .testTag("submit_button")
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.2.dp
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
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlidingAuthModeToggle(
    isSignUp: Boolean,
    onSelect: (Boolean) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF1F5F9))
            .border(1.dp, BorderDark.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        val tabWidth = maxWidth / 2
        val indicatorOffset by animateDpAsState(
            targetValue = if (isSignUp) tabWidth else 0.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "TogglePillOffset"
        )

        // Sliding white pill
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(10.dp),
            shadowElevation = 2.dp,
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
        ) {}

        Row(modifier = Modifier.fillMaxSize()) {
            val loginTextColor by animateColorAsState(
                targetValue = if (!isSignUp) TextWhite else TextMuted,
                animationSpec = tween(180),
                label = "LoginTabColor"
            )
            val signUpTextColor by animateColorAsState(
                targetValue = if (isSignUp) TextWhite else TextMuted,
                animationSpec = tween(180),
                label = "SignUpTabColor"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(false) }
                    .testTag("tab_login"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Entrar",
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    fontWeight = if (!isSignUp) FontWeight.Bold else FontWeight.Medium,
                    color = loginTextColor
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(true) }
                    .testTag("tab_signup"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Criar conta",
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    fontWeight = if (isSignUp) FontWeight.Bold else FontWeight.Medium,
                    color = signUpTextColor
                )
            }
        }
    }
}

@Composable
private fun MinimalAuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    iconContentDescription: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onImeDone: (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = TextMuted.copy(alpha = 0.5f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = iconContentDescription,
                tint = if (isError) MaterialTheme.colorScheme.error else TextMuted,
                modifier = Modifier.size(19.dp)
            )
        },
        trailingIcon = trailingIcon,
        isError = isError,
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { onImeDone?.invoke() }
        ),
        textStyle = LocalTextStyle.current.copy(
            fontFamily = PlusJakartaSans,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextWhite
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CosmicPrimary,
            unfocusedBorderColor = BorderDark,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFF8FAFC),
            focusedLabelColor = CosmicPrimary,
            unfocusedLabelColor = TextMuted,
            cursorColor = CosmicPrimary
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    )
}

@Composable
private fun PasswordStrengthIndicator(password: String) {
    val score = when {
        password.length < 6 -> 1
        password.length >= 8 && password.any { it.isDigit() } && password.any { it.isUpperCase() || !it.isLetterOrDigit() } -> 3
        else -> 2
    }

    val (activeColor, caption) = when (score) {
        1 -> Color(0xFFEF4444) to "Curta (mínimo 6 caracteres)"
        2 -> Color(0xFFF59E0B) to "Senha razoável"
        else -> CosmicSecondary to "Senha forte"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (step in 1..3) {
                val barColor by animateColorAsState(
                    targetValue = if (step <= score) activeColor else BorderDark,
                    animationSpec = tween(220),
                    label = "StrengthBar$step"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.5.dp)
                        .clip(CircleShape)
                        .background(barColor)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = caption,
            fontFamily = PlusJakartaSans,
            fontSize = 11.sp,
            color = activeColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ForgotPasswordDialog(
    initialEmail: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var resetEmail by remember { mutableStateOf(initialEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        icon = {
            Surface(
                shape = CircleShape,
                color = CosmicPrimary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.LockReset,
                        contentDescription = null,
                        tint = CosmicPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Recuperar senha",
                fontFamily = PlusJakartaSans,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Digite o e-mail da sua loja para enviarmos um link de redefinição de senha.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
                OutlinedTextField(
                    value = resetEmail,
                    onValueChange = { resetEmail = it },
                    label = { Text("E-mail", fontFamily = PlusJakartaSans, fontSize = 13.sp) },
                    placeholder = { Text("voce@sualoja.com.br", fontFamily = PlusJakartaSans, fontSize = 13.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedLabelColor = CosmicPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(resetEmail) },
                enabled = resetEmail.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary),
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text(
                    text = "Enviar link",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text(
                    text = "Cancelar",
                    fontFamily = PlusJakartaSans,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

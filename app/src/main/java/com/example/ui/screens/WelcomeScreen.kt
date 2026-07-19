package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.DecisaoLogo
import com.example.ui.theme.BrandPurpleGradientEnd
import com.example.ui.theme.BrandPurpleGradientStart
import kotlinx.coroutines.delay

enum class OnboardingStep {
    WELCOME,
    USERNAME,
    PASSWORD,
    PROFILE_SETUP,
    LOGIN_USERNAME,
    LOGIN_PASSWORD
}

enum class UsernameStatus {
    Idle,
    Checking,
    Available,
    Taken,
    Invalid
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onRegisterComplete: (username: String, name: String, password: String) -> Unit,
    onNavigateToMain: (username: String, password: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.WELCOME) }
    
    // User data collected
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var selectedAvatarIndex by remember { mutableStateOf(0) }

    // Login data
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Enforce pure white and light purple theme
    val bgBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFF9F8FE), Color(0xFFECE9FC))
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        // Draw elegant purple futuristic waves in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val waveColor = Color(0xFF6F3FF5).copy(alpha = 0.08f)

            for (i in 0..6) {
                val offset = i * 40f
                val path = Path().apply {
                    moveTo(0f, h * 0.15f + offset)
                    cubicTo(
                        w * 0.3f, h * 0.1f + offset,
                        w * 0.5f, h * 0.4f + offset,
                        w, h * 0.3f + offset
                    )
                    cubicTo(
                        w * 0.8f, h * 0.5f + offset,
                        w * 0.3f, h * 0.8f + offset,
                        w, h * 0.9f + offset
                    )
                }
                drawPath(
                    path = path,
                    color = waveColor,
                    style = Stroke(width = 2.5f)
                )
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (currentStep != OnboardingStep.WELCOME) {
                    CenterAlignedTopAppBar(
                        title = {
                            // Simple Step Indicator progress dots
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (currentStep == OnboardingStep.LOGIN_USERNAME || currentStep == OnboardingStep.LOGIN_PASSWORD) {
                                    val loginSteps = listOf(OnboardingStep.LOGIN_USERNAME, OnboardingStep.LOGIN_PASSWORD)
                                    loginSteps.forEach { step ->
                                        val isActive = step.ordinal <= currentStep.ordinal
                                        val isCurrent = step == currentStep
                                        Box(
                                            modifier = Modifier
                                                .size(width = if (isCurrent) 20.dp else 8.dp, height = 8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isActive) Color(0xFF6F3FF5) else Color(0xFFD0CCE0)
                                                )
                                        )
                                    }
                                } else {
                                    OnboardingStep.values().filter { 
                                        it != OnboardingStep.WELCOME && 
                                        it != OnboardingStep.LOGIN_USERNAME && 
                                        it != OnboardingStep.LOGIN_PASSWORD 
                                    }.forEach { step ->
                                        val isActive = step.ordinal <= currentStep.ordinal
                                        val isCurrent = step == currentStep
                                        Box(
                                            modifier = Modifier
                                                .size(width = if (isCurrent) 20.dp else 8.dp, height = 8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isActive) Color(0xFF6F3FF5) else Color(0xFFD0CCE0)
                                                )
                                        )
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    currentStep = when (currentStep) {
                                        OnboardingStep.WELCOME -> OnboardingStep.WELCOME
                                        OnboardingStep.USERNAME -> OnboardingStep.WELCOME
                                        OnboardingStep.PASSWORD -> OnboardingStep.USERNAME
                                        OnboardingStep.PROFILE_SETUP -> OnboardingStep.PASSWORD
                                        OnboardingStep.LOGIN_USERNAME -> OnboardingStep.WELCOME
                                        OnboardingStep.LOGIN_PASSWORD -> OnboardingStep.LOGIN_USERNAME
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = Color(0xFF140B33)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
        ) { paddingValues ->
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        slideInHorizontally { width -> width }.togetherWith(slideOutHorizontally { width -> -width })
                    } else {
                        slideInHorizontally { width -> -width }.togetherWith(slideOutHorizontally { width -> width })
                    }
                },
                label = "OnboardingTransition",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) { step ->
                when (step) {
                    OnboardingStep.WELCOME -> {
                        WelcomeMainContent(
                            onSignIn = { currentStep = OnboardingStep.LOGIN_USERNAME },
                            onSignUp = { currentStep = OnboardingStep.USERNAME }
                        )
                    }
                    OnboardingStep.USERNAME -> {
                        UsernameStepContent(
                            username = username,
                            onUsernameChange = { username = it },
                            onNext = { currentStep = OnboardingStep.PASSWORD }
                        )
                    }
                    OnboardingStep.PASSWORD -> {
                        PasswordStepContent(
                            password = password,
                            onPasswordChange = { password = it },
                            onNext = { currentStep = OnboardingStep.PROFILE_SETUP }
                        )
                    }
                    OnboardingStep.PROFILE_SETUP -> {
                        ProfileSetupStepContent(
                            displayName = displayName,
                            onDisplayNameChange = { displayName = it },
                            selectedAvatarIndex = selectedAvatarIndex,
                            onAvatarSelect = { selectedAvatarIndex = it },
                            onFinish = {
                                onRegisterComplete(username, displayName, password)
                            }
                        )
                    }
                    OnboardingStep.LOGIN_USERNAME -> {
                        LoginUsernameStepContent(
                            username = loginUsername,
                            onUsernameChange = { loginUsername = it },
                            onNext = { currentStep = OnboardingStep.LOGIN_PASSWORD }
                        )
                    }
                    OnboardingStep.LOGIN_PASSWORD -> {
                        LoginPasswordStepContent(
                            username = loginUsername,
                            password = loginPassword,
                            onPasswordChange = { loginPassword = it },
                            onNext = { onNavigateToMain(loginUsername, loginPassword) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeMainContent(
    onSignIn: () -> Unit,
    onSignUp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Central Branding Block
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            DecisaoLogo(size = 110.dp)
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF140B33),
                letterSpacing = (-0.5).sp
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = stringResource(R.string.subtitle),
                fontSize = 15.sp,
                color = Color(0xFF534C73),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }

        // Bottom Buttons Block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Entrar Button
            Button(
                onClick = onSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(BrandPurpleGradientStart, BrandPurpleGradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.btn_entrar),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Criar conta Button
            Button(
                onClick = onSignUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFDCD8F3),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_criar_conta),
                    color = Color(0xFF3F1BB0),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy/Terms Text
            Text(
                text = stringResource(R.string.terms_privacy),
                fontSize = 11.sp,
                color = Color(0xFF8681A3),
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun UsernameStepContent(
    username: String,
    onUsernameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    var checkingStatus by remember { mutableStateOf(UsernameStatus.Idle) }

    // Instagram style checking logic with LaunchedEffect
    LaunchedEffect(username) {
        val trimmed = username.trim()
        if (trimmed.isEmpty()) {
            checkingStatus = UsernameStatus.Idle
            return@LaunchedEffect
        }
        
        // Allowed characters: standard Instagram rules
        if (!trimmed.matches(Regex("^[a-zA-Z0-9_.]+$"))) {
            checkingStatus = UsernameStatus.Invalid
            return@LaunchedEffect
        }

        checkingStatus = UsernameStatus.Checking
        delay(500) // Simulates real-time verification network lookup

        val takenUsernames = listOf(
            "marinasouza", "anaclara", "gabrielf", "julianacosta",
            "beatrizlima", "lucasmartins", "timedecisoes", "carlosm"
        )

        checkingStatus = if (takenUsernames.contains(trimmed.lowercase())) {
            UsernameStatus.Taken
        } else {
            UsernameStatus.Available
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Escolha seu nome de usuário",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF140B33)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Adicione um nome de usuário único para que as pessoas possam te encontrar facilmente.",
                fontSize = 14.sp,
                color = Color(0xFF534C73),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Username input with instagram-style loading indicator or validation
            OutlinedTextField(
                value = username,
                onValueChange = { input ->
                    // Prevent spaces in usernames
                    if (!input.contains(" ")) {
                        onUsernameChange(input)
                    }
                },
                prefix = {
                    Text(
                        text = "@",
                        color = Color(0xFF6F3FF5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                trailingIcon = {
                    when (checkingStatus) {
                        UsernameStatus.Checking -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF6F3FF5)
                            )
                        }
                        UsernameStatus.Available -> {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Disponível",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                        UsernameStatus.Taken, UsernameStatus.Invalid -> {
                            Icon(
                                imageVector = Icons.Filled.Cancel,
                                contentDescription = "Indisponível",
                                tint = Color(0xFFE53935)
                            )
                        }
                        UsernameStatus.Idle -> {}
                    }
                },
                label = { Text("Nome de usuário") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6F3FF5),
                    focusedLabelColor = Color(0xFF6F3FF5),
                    unfocusedBorderColor = Color(0xFFD0CCE0)
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Text Feedback underneath
            when (checkingStatus) {
                UsernameStatus.Checking -> {
                    Text(
                        text = "Verificando disponibilidade...",
                        fontSize = 13.sp,
                        color = Color(0xFF8681A3)
                    )
                }
                UsernameStatus.Available -> {
                    Text(
                        text = "Nome de usuário disponível!",
                        fontSize = 13.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
                UsernameStatus.Taken -> {
                    Text(
                        text = "Este nome de usuário já está sendo usado.",
                        fontSize = 13.sp,
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Medium
                    )
                }
                UsernameStatus.Invalid -> {
                    Text(
                        text = "Apenas letras, números, sublinhados (_) ou pontos (.) são permitidos.",
                        fontSize = 13.sp,
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Medium
                    )
                }
                UsernameStatus.Idle -> {
                    Text(
                        text = "Digite seu usuário para verificar.",
                        fontSize = 13.sp,
                        color = Color(0xFF8681A3)
                    )
                }
            }
        }

        // Action Button
        Button(
            onClick = onNext,
            enabled = checkingStatus == UsernameStatus.Available,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6F3FF5),
                disabledContainerColor = Color(0xFFD0CCE0)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Avançar",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PasswordStepContent(
    password: String,
    onPasswordChange: (String) -> Unit,
    onNext: () -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    val isPasswordValid = password.length >= 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Crie uma senha",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF140B33)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sua senha deve ter pelo menos 6 caracteres para garantir a segurança da sua conta.",
                fontSize = 14.sp,
                color = Color(0xFF534C73),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Senha") },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Ocultar senha" else "Mostrar senha",
                            tint = Color(0xFF8681A3)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6F3FF5),
                    focusedLabelColor = Color(0xFF6F3FF5),
                    unfocusedBorderColor = Color(0xFFD0CCE0)
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password strength bars (simple visual design polish)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val strengthLevel = when {
                    password.isEmpty() -> 0
                    password.length < 4 -> 1
                    password.length < 6 -> 2
                    else -> 3
                }

                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < strengthLevel) {
                                    if (strengthLevel == 3) Color(0xFF6F3FF5) else Color(0xFFFFB74D)
                                } else {
                                    Color(0xFFEBE9F5)
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when {
                    password.isEmpty() -> "Digite sua nova senha."
                    password.length < 6 -> "Senha muito curta (mínimo 6 caracteres)."
                    else -> "Excelente! Senha segura."
                },
                fontSize = 13.sp,
                color = if (isPasswordValid) Color(0xFF4CAF50) else Color(0xFF534C73),
                fontWeight = if (isPasswordValid) FontWeight.Medium else FontWeight.Normal
            )
        }

        // Action Button
        Button(
            onClick = onNext,
            enabled = isPasswordValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6F3FF5),
                disabledContainerColor = Color(0xFFD0CCE0)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Avançar",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProfileSetupStepContent(
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    selectedAvatarIndex: Int,
    onAvatarSelect: (Int) -> Unit,
    onFinish: () -> Unit
) {
    val isFormValid = displayName.trim().isNotEmpty()

    // Predefined beautiful gradients for WhatsApp-style avatars selection
    val avatarGradients = listOf(
        listOf(Color(0xFF6F3FF5), Color(0xFF916AFF)),
        listOf(Color(0xFFFF5252), Color(0xFFFF7A00)),
        listOf(Color(0xFF00E676), Color(0xFF00B0FF)),
        listOf(Color(0xFFEC407A), Color(0xFFAB47BC))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Configurar Perfil",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF140B33),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Escolha um avatar de cor e insira seu nome de exibição (igualzinho ao WhatsApp).",
                fontSize = 14.sp,
                color = Color(0xFF534C73),
                lineHeight = 20.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // WhatsApp Style Profile Image Selection Circle with Camera Icon Overlay
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.size(110.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = avatarGradients[selectedAvatarIndex]
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = if (displayName.trim().isNotEmpty()) {
                        displayName.trim().take(2).uppercase()
                    } else {
                        "U"
                    }
                    Text(
                        text = initials,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                // Small circular green/purple camera icon overlay
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6F3FF5))
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Alterar Foto",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Predefined avatar theme selection pills (click to choose color palette)
            Text(
                text = "Toque para escolher uma paleta de cores:",
                fontSize = 12.sp,
                color = Color(0xFF8681A3),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                avatarGradients.forEachIndexed { index, colors ->
                    val isSelected = selectedAvatarIndex == index
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(colors))
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) Color(0xFF140B33) else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onAvatarSelect(index) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Display Name Input Text Box
            OutlinedTextField(
                value = displayName,
                onValueChange = onDisplayNameChange,
                label = { Text("Nome de exibição (ex: Marina Souza)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6F3FF5),
                    focusedLabelColor = Color(0xFF6F3FF5),
                    unfocusedBorderColor = Color(0xFFD0CCE0)
                ),
                shape = RoundedCornerShape(14.dp)
            )
        }

        // Complete Registration Button
        Button(
            onClick = onFinish,
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6F3FF5),
                disabledContainerColor = Color(0xFFD0CCE0)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Concluir",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LoginUsernameStepContent(
    username: String,
    onUsernameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val isUsernameValid = username.trim().length >= 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Entre na sua conta",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF140B33)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Digite seu nome de usuário cadastrado para prosseguir com o acesso.",
                fontSize = 14.sp,
                color = Color(0xFF534C73),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Username input
            OutlinedTextField(
                value = username,
                onValueChange = { input ->
                    // Prevent spaces in usernames
                    if (!input.contains(" ")) {
                        onUsernameChange(input)
                    }
                },
                prefix = {
                    Text(
                        text = "@",
                        color = Color(0xFF6F3FF5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                label = { Text("Nome de usuário") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_username_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6F3FF5),
                    focusedLabelColor = Color(0xFF6F3FF5),
                    unfocusedBorderColor = Color(0xFFD0CCE0)
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Digite pelo menos 3 caracteres.",
                fontSize = 13.sp,
                color = Color(0xFF8681A3)
            )
        }

        // Action Button
        Button(
            onClick = onNext,
            enabled = isUsernameValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("login_username_next_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6F3FF5),
                disabledContainerColor = Color(0xFFD0CCE0)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Avançar",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LoginPasswordStepContent(
    username: String,
    password: String,
    onPasswordChange: (String) -> Unit,
    onNext: () -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    val isPasswordValid = password.length >= 6
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Digite sua senha",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF140B33)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Olá, @$username! Insira a senha cadastrada para acessar as discussões.",
                fontSize = 14.sp,
                color = Color(0xFF534C73),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Senha") },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Ocultar senha" else "Mostrar senha",
                            tint = Color(0xFF8681A3)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6F3FF5),
                    focusedLabelColor = Color(0xFF6F3FF5),
                    unfocusedBorderColor = Color(0xFFD0CCE0)
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Forgot Password Button / Link
            TextButton(
                onClick = { showForgotPasswordDialog = true },
                modifier = Modifier
                    .align(Alignment.Start)
                    .testTag("forgot_password_btn")
            ) {
                Text(
                    text = "Esqueceu a senha?",
                    color = Color(0xFF6F3FF5),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Confirm Button
        Button(
            onClick = onNext,
            enabled = isPasswordValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("login_submit_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6F3FF5),
                disabledContainerColor = Color(0xFFD0CCE0)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Entrar",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LockReset,
                        contentDescription = "Recuperar",
                        tint = Color(0xFF6F3FF5)
                    )
                    Text(text = "Recuperar Senha")
                }
            },
            text = {
                Text(
                    text = "Como o cadastro é feito exclusivamente por usuário e senha (sem necessidade de e-mail), você pode redefinir sua senha diretamente com o administrador ou tentar novamente caso tenha lembrado.",
                    color = Color(0xFF534C73),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showForgotPasswordDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6F3FF5)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Entendi", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

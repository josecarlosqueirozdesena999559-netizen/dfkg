package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    seeOnline: Boolean,
    lastSeen: Boolean,
    delivered: Boolean,
    readReceipt: Boolean,
    typing: Boolean,
    recordingAudio: Boolean,
    blockedContacts: List<String>,
    onTogglePrivacy: (String) -> Unit,
    onUnblockContact: (String) -> Unit,
    onDeleteAccount: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubScreen by remember { mutableStateOf<String?>(null) } // "privacidade", "seguranca", "conta"
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Password state
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordSuccessMsg by remember { mutableStateOf<String?>(null) }
    var passwordErrorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (activeSubScreen) {
                            "privacidade" -> "Privacidade"
                            "seguranca" -> "Segurança"
                            "conta" -> "Gerenciar Conta"
                            else -> "Configurações"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (activeSubScreen != null) {
                                activeSubScreen = null
                                passwordSuccessMsg = null
                                passwordErrorMsg = null
                            } else {
                                onBackClick()
                            }
                        },
                        modifier = Modifier.testTag("settings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeSubScreen) {
                null -> {
                    // Main Settings Page
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            SettingsMenuCard(
                                icon = Icons.Filled.Lock,
                                title = "Privacidade",
                                description = "Online, visto por último, confirmações, contatos bloqueados",
                                onClick = { activeSubScreen = "privacidade" },
                                tag = "settings_privacy_item"
                            )
                        }
                        item {
                            SettingsMenuCard(
                                icon = Icons.Filled.Security,
                                title = "Segurança",
                                description = "Alterar senha de acesso, autenticação e proteção",
                                onClick = { activeSubScreen = "seguranca" },
                                tag = "settings_security_item"
                            )
                        }
                        item {
                            SettingsMenuCard(
                                icon = Icons.Filled.ManageAccounts,
                                title = "Conta",
                                description = "Excluir conta permanentemente e dados do perfil",
                                onClick = { activeSubScreen = "conta" },
                                tag = "settings_account_item"
                            )
                        }
                    }
                }
                "privacidade" -> {
                    // Privacy Sub Screen
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Quem pode interagir com você",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        item {
                            PrivacyToggleRow(
                                title = "Ver que você está online",
                                description = "Se desativado, ninguém poderá ver quando você estiver online e você também não verá o status de outras pessoas.",
                                checked = seeOnline,
                                onCheckedChange = { onTogglePrivacy("seeOnline") },
                                tag = "toggle_see_online"
                            )
                        }
                        item { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp)) }
                        item {
                            PrivacyToggleRow(
                                title = "Visto por último",
                                description = "Mostra o horário da sua última atividade nos chats.",
                                checked = lastSeen,
                                onCheckedChange = { onTogglePrivacy("lastSeen") },
                                tag = "toggle_last_seen"
                            )
                        }
                        item { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp)) }
                        item {
                            PrivacyToggleRow(
                                title = "Confirmar entregue",
                                description = "Mostra sinal de que a mensagem foi recebida pelo destinatário.",
                                checked = delivered,
                                onCheckedChange = { onTogglePrivacy("delivered") },
                                tag = "toggle_delivered"
                            )
                        }
                        item { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp)) }
                        item {
                            PrivacyToggleRow(
                                title = "Confirmar lido",
                                description = "Se desativado, você não enviará nem receberá confirmações de leitura (dois traços azuis).",
                                checked = readReceipt,
                                onCheckedChange = { onTogglePrivacy("readReceipt") },
                                tag = "toggle_read_receipt"
                            )
                        }
                        item { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp)) }
                        item {
                            PrivacyToggleRow(
                                title = "Indicador 'Digitando...'",
                                description = "Permite que outras pessoas vejam quando você está digitando uma mensagem.",
                                checked = typing,
                                onCheckedChange = { onTogglePrivacy("typing") },
                                tag = "toggle_typing"
                            )
                        }
                        item { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp)) }
                        item {
                            PrivacyToggleRow(
                                title = "Indicador 'Gravando áudio...'",
                                description = "Permite que os destinatários saibam quando você está gravando uma mensagem de voz.",
                                checked = recordingAudio,
                                onCheckedChange = { onTogglePrivacy("recordingAudio") },
                                tag = "toggle_recording_audio"
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Contatos Bloqueados",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        if (blockedContacts.isEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Você não tem contatos bloqueados.",
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(16.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(blockedContacts) { contact ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Block,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = contact,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        TextButton(
                                            onClick = { onUnblockContact(contact) },
                                            modifier = Modifier.testTag("unblock_${contact}")
                                        ) {
                                            Text(
                                                text = "Desbloquear",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
                "seguranca" -> {
                    // Security Sub Screen - Change Password
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(64.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        
                        Text(
                            text = "Trocar Senha",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        
                        Text(
                            text = "Para garantir a segurança dos seus dados na rede social Decisões, defina uma senha forte que você não use em outros sites.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Senha Atual") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("current_password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Nova Senha") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirmar Nova Senha") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (passwordSuccessMsg != null) {
                            Text(
                                text = passwordSuccessMsg!!,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        if (passwordErrorMsg != null) {
                            Text(
                                text = passwordErrorMsg!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                                    passwordSuccessMsg = null
                                    passwordErrorMsg = "Por favor, preencha todos os campos."
                                } else if (newPassword != confirmPassword) {
                                    passwordSuccessMsg = null
                                    passwordErrorMsg = "As senhas não coincidem."
                                } else {
                                    passwordErrorMsg = null
                                    passwordSuccessMsg = "Senha alterada com sucesso!"
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("update_password_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Salvar Nova Senha", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
                "conta" -> {
                    // Account Management Page
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteForever,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "Exclusão de Conta",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Atenção: Ao excluir permanentemente a sua conta, todos os seus dados pessoais, suas publicações, enquetes, votos e históricos de mensagens serão removidos imediatamente de nossos servidores e não poderão ser recuperados.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { showDeleteConfirmation = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("delete_account_permanent_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteForever,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Excluir Conta Permanentemente",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation AlertDialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirmar Exclusão", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza absoluta de que deseja excluir permanentemente a sua conta no Decisões? Esta ação é irreversível.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sim, Excluir", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun SettingsMenuCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun PrivacyToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag)
        )
    }
}

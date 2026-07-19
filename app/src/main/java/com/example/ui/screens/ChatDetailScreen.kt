package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ChatMessage
import com.example.model.ChatRoom
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.BrandPurpleGradientEnd
import com.example.ui.theme.BrandPurpleGradientStart
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatDetailScreen(
    room: ChatRoom,
    onSendMessage: (String) -> Unit,
    onSendImage: (String, Boolean) -> Unit,
    onDeleteMessage: (String, Boolean) -> Unit,
    onOpenSingleView: (String) -> Unit,
    onToggleMute: () -> Unit,
    onToggleBlock: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Dialog & UI controllers
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showUserProfileSheet by remember { mutableStateOf(false) }
    var showPhotoPickerDialog by remember { mutableStateOf(false) }
    var isSingleViewPhoto by remember { mutableStateOf(false) }
    var singleViewMessageToOpen by remember { mutableStateOf<ChatMessage?>(null) }
    var messageWithOptions by remember { mutableStateOf<ChatMessage?>(null) }

    // Auto scroll to bottom when room updates
    LaunchedEffect(room.messages.size) {
        if (room.messages.isNotEmpty()) {
            listState.animateScrollToItem(room.messages.size - 1)
        }
    }

    // 1. Photo Picker Dialog Simulation
    if (showPhotoPickerDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoPickerDialog = false },
            title = { 
                Text(
                    text = "Enviar Foto",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Escolha uma imagem para simular o envio:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Grid of 3 high quality mock images
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val mockImages = listOf(
                            "https://images.unsplash.com/photo-1541614101331-1a5a3a194e92?w=400&auto=format&fit=crop", // mobilidade/natureza
                            "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=400&auto=format&fit=crop", // montanha
                            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=400&auto=format&fit=crop"  // praia
                        )
                        val labels = listOf("Ciclo", "Montanha", "Praia")
                        
                        mockImages.forEachIndexed { idx, url ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(70.dp)
                                    .clickable {
                                        onSendImage(url, isSingleViewPhoto)
                                        showPhotoPickerDialog = false
                                        isSingleViewPhoto = false
                                        Toast.makeText(context, "Foto enviada!", Toast.LENGTH_SHORT).show()
                                    },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = labels[idx],
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Single-View Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable { isSingleViewPhoto = !isSingleViewPhoto }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = if (isSingleViewPhoto) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Visualização Única",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSingleViewPhoto) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Checkbox(
                            checked = isSingleViewPhoto,
                            onCheckedChange = { isSingleViewPhoto = it }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoPickerDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 2. Fullscreen Single-View Photo Viewer Dialog
    if (singleViewMessageToOpen != null) {
        val msg = singleViewMessageToOpen!!
        AlertDialog(
            onDismissRequest = {
                onOpenSingleView(msg.id)
                singleViewMessageToOpen = null
            },
            title = {
                Text(
                    text = "Foto de Visualização Única",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AsyncImage(
                            model = msg.imageUrl,
                            contentDescription = "Visualização Única",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Atenção: Esta imagem foi fechada e não poderá ser reaberta.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onOpenSingleView(msg.id)
                        singleViewMessageToOpen = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Entendi, Fechar")
                }
            }
        )
    }

    // 3. Message Options Dialog (Long Press)
    if (messageWithOptions != null) {
        val msg = messageWithOptions!!
        AlertDialog(
            onDismissRequest = { messageWithOptions = null },
            title = { Text("Opções de Mensagem") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "\"${if (msg.text.length > 30) msg.text.take(30) + "..." else msg.text}\"",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Option 1: Delete for Me
                    Button(
                        onClick = {
                            onDeleteMessage(msg.id, false)
                            messageWithOptions = null
                            Toast.makeText(context, "Apagada para mim", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Apagar para mim", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Option 2: Delete for Everyone (only if isFromMe)
                    if (msg.isFromMe) {
                        Button(
                            onClick = {
                                onDeleteMessage(msg.id, true)
                                messageWithOptions = null
                                Toast.makeText(context, "Apagada para todos", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Apagar para todos", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { messageWithOptions = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 4. User Profile Bottom Sheet / Modal Dialog
    if (showUserProfileSheet) {
        AlertDialog(
            onDismissRequest = { showUserProfileSheet = false },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatar(name = room.name, size = 80.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = room.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (room.name == "Time Decisões" || room.name == "Debates & Ideias") {
                            Spacer(modifier = Modifier.width(4.dp))
                            VerificationBadge(size = 16.dp)
                        }
                    }
                    Text(
                        text = room.username,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (room.name == "Time Decisões") 
                            "Canal de suporte e atualizações do aplicativo Decisões! 💜" 
                        else 
                            "Apaixonado por ideias, pessoas e mudanças. Vamos construir um futuro melhor juntos! ✨",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bio Counts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("12", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Postagens", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("1.2K", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Seguidores", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("421", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Seguindo", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Purple Gradient Follow Button
                    Button(
                        onClick = { 
                            Toast.makeText(context, "Seguindo com sucesso!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(BrandPurpleGradientStart, BrandPurpleGradientEnd)
                                    ),
                                    shape = RoundedCornerShape(22.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Seguir Perfil", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUserProfileSheet = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Custom Active Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Clicking Avatar opens User Profile
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { showUserProfileSheet = true }
            ) {
                UserAvatar(
                    name = room.name,
                    size = 40.dp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Clicking name/online indicators opens User Profile too
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showUserProfileSheet = true }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = room.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (room.isMuted) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.VolumeMute,
                            contentDescription = "Silenciado",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                
                Text(
                    text = if (room.isOnline) "Online" else room.lastSeen,
                    fontSize = 11.sp,
                    color = if (room.isOnline) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }

            // Removed telephone call button from here as requested.

            // Three dots options button
            Box {
                IconButton(onClick = { showOptionsMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Opções",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }

                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = if (room.isMuted) "Ativar Som" else "Silenciar",
                                fontSize = 14.sp
                            ) 
                        },
                        leadingIcon = { 
                            Icon(
                                imageVector = if (room.isMuted) Icons.Filled.VolumeUp else Icons.Filled.VolumeMute, 
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            ) 
                        },
                        onClick = {
                            onToggleMute()
                            showOptionsMenu = false
                            val txt = if (room.isMuted) "Notificações silenciadas" else "Som ativado"
                            Toast.makeText(context, txt, Toast.LENGTH_SHORT).show()
                        }
                    )

                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = if (room.isBlocked) "Desbloquear" else "Bloquear",
                                fontSize = 14.sp,
                                color = if (room.isBlocked) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.error
                            ) 
                        },
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Filled.Block, 
                                contentDescription = null,
                                tint = if (room.isBlocked) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            ) 
                        },
                        onClick = {
                            onToggleBlock()
                            showOptionsMenu = false
                            val txt = if (room.isBlocked) "Contato desbloqueado" else "Contato bloqueado"
                            Toast.makeText(context, txt, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

        // 2. Chat Messages Body (Bright/Purple Theme, no system dark overlay)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFFAF9FF)) // Force clean white/purple theme background
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(room.messages, key = { it.id }) { message ->
                    MessageBubbleItem(
                        message = message,
                        onOpenSingleView = { singleViewMessageToOpen = message },
                        onLongClick = { messageWithOptions = message }
                    )
                }
            }
        }

        // 3. Bottom Text Composer Row or Blocked Banner
        if (room.isBlocked) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f))
                    .clickable { onToggleBlock() }
                    .padding(vertical = 18.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Você bloqueou este contato. Toque para Desbloquear.",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo upload option button (camera icon)
                IconButton(
                    onClick = { showPhotoPickerDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = "Enviar foto",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Text Input Box with explicit high contrast text colors ("ver conteudo enquanto digita")
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { 
                        Text(
                            "Escreva uma mensagem...", 
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ) 
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(max = 120.dp)
                        .testTag("chat_input_text_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F1FB),
                        unfocusedContainerColor = Color(0xFFF3F1FB),
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    maxLines = 4,
                    singleLine = false
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Audio (microphone) option button
                IconButton(
                    onClick = {
                        Toast.makeText(context, "Gravação de áudio iniciada... 🎤", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Gravar áudio",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Round Sending Button with gradient
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText)
                            messageText = ""
                            coroutineScope.launch {
                                if (room.messages.isNotEmpty()) {
                                    listState.animateScrollToItem(room.messages.size)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(BrandPurpleGradientStart, BrandPurpleGradientEnd)
                            )
                        )
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Enviar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubbleItem(
    message: ChatMessage,
    onOpenSingleView: () -> Unit,
    onLongClick: () -> Unit
) {
    val isFromMe = message.isFromMe

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
        contentAlignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isFromMe) 18.dp else 4.dp,
                            bottomEnd = if (isFromMe) 4.dp else 18.dp
                        )
                    )
                    .background(
                        if (isFromMe) {
                            Brush.linearGradient(
                                colors = listOf(BrandPurpleGradientStart, BrandPurpleGradientEnd)
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFECEAF7), Color(0xFFECEAF7))
                            )
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    if (message.isDeleted) {
                        // Render Deleted message
                        Text(
                            text = message.text,
                            color = if (isFromMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else if (message.imageUrl != null) {
                        // Render photo message
                        if (message.isSingleView) {
                            // Single view photo rendering
                            if (message.isOpened) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "Visualizada",
                                        tint = if (isFromMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Foto visualizada",
                                        color = if (isFromMe) Color.White else MaterialTheme.colorScheme.onBackground,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isFromMe) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.05f))
                                        .clickable { onOpenSingleView() }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Visibility,
                                        contentDescription = "Visualização única",
                                        tint = if (isFromMe) Color.White else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Foto de visualização única",
                                        color = if (isFromMe) Color.White else MaterialTheme.colorScheme.primary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            // Standard photo rendering
                            Column {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    AsyncImage(
                                        model = message.imageUrl,
                                        contentDescription = "Imagem enviada",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                if (message.text.isNotBlank() && !message.text.startsWith("📷")) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = message.text,
                                        color = if (isFromMe) Color.White else MaterialTheme.colorScheme.onBackground,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    } else {
                        // Standard text rendering
                        Text(
                            text = message.text,
                            color = if (isFromMe) Color.White else MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Time and Status checks (lido, entregue)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = message.timestamp,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )

                if (isFromMe && !message.isDeleted) {
                    when (message.status) {
                        "LIDO" -> {
                            Icon(
                                imageVector = Icons.Filled.DoneAll,
                                contentDescription = "Lida",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        "ENTREGUE" -> {
                            Icon(
                                imageVector = Icons.Filled.DoneAll,
                                contentDescription = "Entregue",
                                tint = Color.LightGray,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = "Enviada",
                                tint = Color.LightGray,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

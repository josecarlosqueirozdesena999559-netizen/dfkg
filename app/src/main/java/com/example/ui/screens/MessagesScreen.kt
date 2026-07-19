package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatRoom
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerificationBadge

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessagesScreen(
    chatRooms: List<ChatRoom>,
    onChatRoomClick: (String) -> Unit,
    onBackToHome: () -> Unit,
    onDeleteChatRoom: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf(0) } // 0 = Todas, 1 = Não lidas
    var roomToDelete by remember { mutableStateOf<ChatRoom?>(null) }

    val filteredRooms = remember(chatRooms, searchQuery, selectedFilterTab) {
        chatRooms.filter { room ->
            // Search filter
            val matchesSearch = room.name.contains(searchQuery, ignoreCase = true) || 
                                room.lastMessage.contains(searchQuery, ignoreCase = true)
            
            // Tab filter
            val matchesTab = when (selectedFilterTab) {
                1 -> room.unreadCount > 0 // Unread
                else -> true // All
            }

            matchesSearch && matchesTab
        }
    }

    if (roomToDelete != null) {
        AlertDialog(
            onDismissRequest = { roomToDelete = null },
            title = { Text("Excluir Conversa") },
            text = { Text("Deseja realmente excluir a conversa com ${roomToDelete?.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        roomToDelete?.id?.let { onDeleteChatRoom(it) }
                        roomToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { roomToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 2. Search Box - Fixed "bug preto" by using theme-compliant surfaceVariant
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { 
                Text(
                    text = "Buscar mensagens",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 14.sp
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(52.dp)
                .testTag("chat_search_input"),
            shape = RoundedCornerShape(26.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Tab Selectors (Todas, Não lidas) - Removed "Grupos" Tab completely
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf("Todas", "Não lidas")
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedFilterTab == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            } else {
                                Color.Transparent
                            }
                        )
                        .clickable { selectedFilterTab = index }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("chat_tab_$index")
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Chat Rooms List
        if (filteredRooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.QuestionAnswer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nenhuma conversa encontrada",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredRooms, key = { it.id }) { room ->
                    ChatRoomItem(
                        room = room,
                        onClick = { onChatRoomClick(room.id) },
                        onLongClick = { roomToDelete = room }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                        modifier = Modifier.padding(start = 76.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatRoomItem(
    room: ChatRoom,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("chat_room_item_${room.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User/Group avatar
        UserAvatar(
            name = room.name,
            size = 52.dp,
            hasStoryBorder = room.unreadCount > 0,
            showOnlineStatus = room.unreadCount > 0 && !room.isGroup
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Middle Chat Details
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = room.name,
                    fontSize = 15.sp,
                    fontWeight = if (room.unreadCount > 0) FontWeight.ExtraBold else FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (room.name == "Time Decisões" || room.name == "Debates & Ideias" || room.name == "Beatriz Lima") {
                    Spacer(modifier = Modifier.width(4.dp))
                    VerificationBadge(size = 14.dp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = room.lastMessage,
                fontSize = 13.sp,
                color = if (room.unreadCount > 0) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                },
                fontWeight = if (room.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right timestamp & badge counts
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = room.timestamp,
                fontSize = 11.sp,
                color = if (room.unreadCount > 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                },
                fontWeight = if (room.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (room.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = room.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (room.name == "Time Decisões") {
                // Special 'S' verification badge circle matching Screen 4
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

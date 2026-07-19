package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.components.UserAvatar

object ProfileThemeHelper {
    fun getCoverBrush(coverName: String): Brush {
        val colors = when (coverName) {
            "purple" -> listOf(Color(0xFF8A2BE2), Color(0xFF1D0E3D))
            "blue" -> listOf(Color(0xFF0F52BA), Color(0xFF072146))
            "sunset" -> listOf(Color(0xFFFF5E62), Color(0xFF9E0059))
            "dark" -> listOf(Color(0xFF3A3A3A), Color(0xFF121212))
            "green" -> listOf(Color(0xFF11998e), Color(0xFF38ef7d))
            else -> listOf(Color(0xFF8A2BE2), Color(0xFF1D0E3D))
        }
        return Brush.verticalGradient(colors)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profile: UserProfile,
    onSave: (String, String, String, String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(profile.name) }
    var username by remember { mutableStateOf(profile.username.replace("@", "")) }
    var bio by remember { mutableStateOf(profile.bio) }
    var selectedAvatar by remember { mutableStateOf(profile.avatarUrl) }
    var selectedCover by remember { mutableStateOf(profile.coverUrl) }

    val presetAvatars = listOf("marina", "gabriel", "juliana", "lucas", "beatriz", "ana_clara")
    val presetCovers = listOf(
        "purple" to "Roxo Cosmic",
        "blue" to "Azul Royal",
        "sunset" to "Sunset",
        "dark" to "Grafite",
        "green" to "Esmeralda"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Editar Perfil",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (name.isNotBlank() && username.isNotBlank()) {
                                onSave(name, username, bio, selectedAvatar, selectedCover)
                            }
                        },
                        enabled = name.isNotBlank() && username.isNotBlank(),
                        modifier = Modifier.testTag("btn_save_profile")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Salvar",
                            tint = if (name.isNotBlank() && username.isNotBlank()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // 1. Interactive Header Preview
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    // Header Cover with chosen gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(brush = ProfileThemeHelper.getCoverBrush(selectedCover))
                    )

                    // Avatar overlapping the banner
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 24.dp)
                            .size(96.dp)
                    ) {
                        UserAvatar(
                            name = if (selectedAvatar == "marina") "Marina Souza" else selectedAvatar,
                            size = 96.dp,
                            modifier = Modifier
                                .border(
                                    width = 4.dp,
                                    color = MaterialTheme.colorScheme.background,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            // 2. Customizers for Avatar and Cover
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Foto de perfil",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal selection for avatars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        presetAvatars.forEach { avatarKey ->
                            val isSelected = selectedAvatar == avatarKey
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                                    .clickable { selectedAvatar = avatarKey }
                            ) {
                                UserAvatar(name = avatarKey, size = 46.dp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Estilo de capa",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row or Grid of covers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetCovers.forEach { (coverKey, label) ->
                            val isSelected = selectedCover == coverKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(brush = ProfileThemeHelper.getCoverBrush(coverKey))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedCover = coverKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. User Detail Fields
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome Completo") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_name"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Usuário (username)") },
                        prefix = { Text("@") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_username"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Frase que define / Biografia") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_bio"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4,
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank() && username.isNotBlank()) {
                                onSave(name, username, bio, selectedAvatar, selectedCover)
                            }
                        },
                        enabled = name.isNotBlank() && username.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_save_profile_footer"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Salvar Alterações",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

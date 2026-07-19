package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.window.Dialog
import com.example.model.FeedPost
import com.example.model.UserProfile
import com.example.ui.components.ReportDialog
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.BrandPurpleGradientEnd
import com.example.ui.theme.BrandPurpleGradientStart

@Composable
fun ProfileScreen(
    profile: UserProfile,
    profilePosts: List<FeedPost>,
    onLikePost: (String) -> Unit,
    onVotePoll: (String, String) -> Unit,
    onEditProfileClick: () -> Unit,
    onBackToHome: () -> Unit,
    onSavePost: (String) -> Unit = {},
    onReportPost: (String, String, String) -> Unit = { _, _, _ -> },
    onDeletePost: (String) -> Unit = { _ -> },
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var selectedProfileTab by remember { mutableStateOf(0) } // 0 = Enquetes, 1 = Pensamentos, 2 = Curtidas
    var postToReport by remember { mutableStateOf<FeedPost?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Scrollable content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Double Header Banner + Overlapping Avatar & Stats Side-by-side!
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(brush = ProfileThemeHelper.getCoverBrush(profile.coverUrl))
                    ) {
                        // Small Back navigation icon on top of banner
                        IconButton(
                            onClick = onBackToHome,
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = Color.White
                            )
                        }
                    }

                    // Avatar (shifted up) and Stats Row side-by-side!
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Overlapping Avatar with white border
                        Box(
                            modifier = Modifier
                                .offset(y = (-36).dp)
                                .size(90.dp)
                        ) {
                            UserAvatar(
                                name = profile.name,
                                size = 90.dp,
                                modifier = Modifier
                                    .border(
                                        width = 4.dp,
                                        color = MaterialTheme.colorScheme.background,
                                        shape = CircleShape
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Stats on the right side of the avatar (subido!)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 20.dp), // Compensate for avatar offset shift
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileStatItem(count = profile.publicationsCount.toString(), label = "Publicações")
                            ProfileStatItem(count = profile.followersCount, label = "Seguidores")
                            ProfileStatItem(count = profile.followingCount.toString(), label = "Seguindo")
                        }
                    }
                }
            }

            // 3. Names & Verification
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = profile.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (profile.isVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            VerificationBadge(size = 18.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = profile.username,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bio Text
                    Text(
                        text = profile.bio,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Edit Profile Button
                    Button(
                        onClick = onEditProfileClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_edit_profile"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Editar perfil",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 4. Custom Profile Tabs Selector
            item {
                Spacer(modifier = Modifier.height(14.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val profileTabs = listOf("Enquetes", "Pensamentos", "Curtidas")
                    profileTabs.forEachIndexed { index, tabName ->
                        val isSelected = selectedProfileTab == index
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedProfileTab = index }
                                .padding(vertical = 10.dp)
                                .testTag("profile_tab_$index")
                        ) {
                            Text(
                                text = tabName,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Blue Pill Indicator under active tab
                            Box(
                                modifier = Modifier
                                    .height(3.dp)
                                    .width(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    )
                            )
                        }
                    }
                }
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 5. Profile Tab Sub-Views Content
            when (selectedProfileTab) {
                0 -> {
                    // Enquetes tab (Poll posts authored by user)
                    if (profilePosts.isEmpty()) {
                        item {
                            EmptyStatePlaceholder("Nenhuma enquete criada ainda.")
                        }
                    } else {
                        items(profilePosts, key = { it.id }) { post ->
                            FeedPostCard(
                                post = post,
                                onLike = { onLikePost(post.id) },
                                onVote = { optId -> onVotePoll(post.id, optId) },
                                onSave = { onSavePost(post.id) },
                                onReport = { postToReport = post },
                                onDelete = { onDeletePost(post.id) }
                            )
                        }
                    }
                }
                1 -> {
                    // Pensamentos tab (Text ideas/quotes)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ThoughtCard(
                                text = "A verdadeira participação democrática começa no diálogo local, nas pequenas decisões cotidianas de nosso bairro. 🌸",
                                date = "2 dias atrás"
                            )
                            ThoughtCard(
                                text = "Se quisermos transformar as cidades, precisamos primeiro dar ouvido e voz aos cidadãos.",
                                date = "5 dias atrás"
                            )
                        }
                    }
                }
                2 -> {
                    // Curtidas tab (Simple simulated liked posts)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ThoughtCard(
                                text = "Gostou do depoimento de Carlos Mendes: 'O construído nas pequenas decisões que tomamos todos os dias...'",
                                date = "Ontem",
                                isLikedPost = true
                            )
                        }
                    }
                }
            }
        }

        postToReport?.let { post ->
            ReportDialog(
                onDismiss = { postToReport = null },
                onSubmit = { reason, details ->
                    onReportPost(post.id, reason, details)
                }
            )
        }
    }
}

@Composable
private fun ProfileStatItem(count: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyStatePlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ThoughtCard(
    text: String,
    date: String,
    isLikedPost: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        shape = RoundedCornerShape(16.dp),
        border = CardStrokeHelper.getBorderStroke(isDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLikedPost) Color(0xFFFF4081) else MaterialTheme.colorScheme.primary
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isLikedPost) "Curtida" else "Pensamento",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLikedPost) Color(0xFFFF4081) else MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = date,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}



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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.PersonRemoveAlt1
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FeedPost
import com.example.model.UserProfile
import com.example.ui.components.ReportDialog
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.BrandPurpleGradientEnd
import com.example.ui.theme.BrandPurpleGradientStart

@Composable
fun UserProfileScreen(
    profile: UserProfile,
    allPosts: List<FeedPost>,
    onLikePost: (String) -> Unit,
    onVotePoll: (String, String) -> Unit,
    onBackClick: () -> Unit,
    onToggleFollow: () -> Unit,
    onSendMessage: () -> Unit,
    onPostClick: (FeedPost) -> Unit,
    onSavePost: (String) -> Unit = {},
    onReportPost: (String, String, String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var postToReport by remember { mutableStateOf<FeedPost?>(null) }
    
    // Filter posts authored by this user
    val userFeedPosts = remember(allPosts, profile.username) {
        allPosts.filter { it.authorUsername == profile.username }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 30.dp)
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
                        // Back Navigation Button
                        IconButton(
                            onClick = onBackClick,
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
                            ProfileStatItem(count = userFeedPosts.size.toString(), label = "Publicações")
                            ProfileStatItem(count = profile.followersCount, label = "Seguidores")
                            ProfileStatItem(count = profile.followingCount.toString(), label = "Seguindo")
                        }
                    }
                }
            }

            // 3. Names, Verification & Bio
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

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action Buttons Row: Follow and Message
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Follow button
                        Button(
                            onClick = onToggleFollow,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("user_follow_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (profile.isFollowing) {
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (profile.isFollowing) Icons.Filled.PersonRemoveAlt1 else Icons.Filled.PersonAddAlt1,
                                    contentDescription = null,
                                    tint = if (profile.isFollowing) MaterialTheme.colorScheme.primary else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (profile.isFollowing) "Seguindo" else "Seguir",
                                    color = if (profile.isFollowing) MaterialTheme.colorScheme.primary else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Message button
                        Button(
                            onClick = onSendMessage,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("user_message_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Forum,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mensagem",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Section divider
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Feed de Publicações",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 4. Feed Posts List
            if (userFeedPosts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma publicação no feed ainda.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                items(userFeedPosts, key = { it.id }) { post ->
                    FeedPostCard(
                        post = post,
                        onLike = { onLikePost(post.id) },
                        onVote = { optId -> onVotePoll(post.id, optId) },
                        onPostClick = { onPostClick(post) },
                        onUserClick = { /* Already on profile */ },
                        onToggleFollow = {},
                        onSave = { onSavePost(post.id) },
                        onReport = { postToReport = post }
                    )
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
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

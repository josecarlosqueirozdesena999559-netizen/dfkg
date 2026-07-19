package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.model.FeedPost
import com.example.model.Story
import com.example.model.UserProfile
import com.example.ui.components.ReportDialog
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.AccentPink
import com.example.ui.theme.BrandPurpleGradientEnd
import com.example.ui.theme.BrandPurpleGradientStart

val CATEGORIES = listOf(
    "Todos" to "✨",
    "Moda" to "👗",
    "Vida" to "🌱",
    "Viagens" to "✈️",
    "Hobbies" to "🎨",
    "Amor" to "💖",
    "Lojas" to "🛍️",
    "Comprinhas" to "🛒",
    "Sites" to "🌐",
    "Tecnologia" to "💻",
    "Educação" to "🎓",
    "Política" to "🗳️",
    "Realities" to "📺",
    "Proteção Animal" to "🐾",
    "Mulheres" to "👩",
    "Homens" to "👨",
    "Alimentação" to "🍕",
    "Locais" to "📍",
    "Meio Ambiente" to "🌳",
    "Saúde e Bem Estar" to "🧘‍♀️",
    "Outros" to "💡"
)

@Composable
fun FeedScreen(
    stories: List<Story>,
    posts: List<FeedPost>,
    onLikePost: (String) -> Unit,
    onVotePoll: (String, String) -> Unit,
    onCreatePoll: (String, String, String) -> Unit,
    onAvatarClick: () -> Unit,
    onPostClick: (FeedPost) -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onToggleFollow: (String) -> Unit = {},
    onSavePost: (String) -> Unit = {},
    onReportPost: (String, String, String) -> Unit = { _, _, _ -> },
    onDeletePost: (String) -> Unit = { _ -> },
    onCreateCustomPost: (String, Boolean, List<com.example.model.PollOption>, String, String?) -> Unit = { _, _, _, _, _ -> },
    currentUserUsername: String = "@marinasouza",
    modifier: Modifier = Modifier
) {
    var showCreatePollDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var postToReport by remember { mutableStateOf<FeedPost?>(null) }

    val filteredPosts = remember(posts, selectedCategory) {
        if (selectedCategory == "Todos") {
            posts
        } else {
            posts.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 2. Horizontal Swipeable Category stories (round format)
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(CATEGORIES) { (catName, emoji) ->
                        val isSelected = selectedCategory == catName
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    selectedCategory = if (selectedCategory == catName) "Todos" else catName
                                }
                                .testTag("category_story_$catName")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) {
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFF6F3FF5), Color(0xFFE040FB))
                                                )
                                            } else {
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.surfaceVariant,
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                )
                                            }
                                        )
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF6F3FF5) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                            shape = CircleShape
                                        )
                                        .padding(if (isSelected) 2.dp else 0.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Color.White else MaterialTheme.colorScheme.surface
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = emoji,
                                            fontSize = 28.sp
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Text(
                                text = catName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    Color(0xFF6F3FF5)
                                } else {
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(66.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 3. Feed Posts
            if (filteredPosts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp, bottom = 48.dp, start = 32.dp, end = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("💡", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nenhuma publicação nesta categoria ainda.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Seja o primeiro a publicar nesta categoria clicando no botão '+' no menu inferior!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                itemsIndexed(filteredPosts, key = { _, post -> post.id }) { index, post ->
                    FeedPostCard(
                        post = post,
                        onLike = { onLikePost(post.id) },
                        onVote = { optId -> onVotePoll(post.id, optId) },
                        onPostClick = { onPostClick(post) },
                        onUserClick = onUserClick,
                        onToggleFollow = onToggleFollow,
                        onSave = { onSavePost(post.id) },
                        onReport = { postToReport = post },
                        onDelete = { onDeletePost(post.id) },
                        currentUserUsername = currentUserUsername
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (index == 0) {
                        RelevantProfilesSection()
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // New Poll Dialog
    if (showCreatePollDialog) {
        CreatePollDialog(
            onDismiss = { showCreatePollDialog = false },
            onCreate = { question, opt1, opt2 ->
                onCreatePoll(question, opt1, opt2)
                showCreatePollDialog = false
            },
            onCreateCustom = { content, isPoll, options, category, imageUrl ->
                onCreateCustomPost(content, isPoll, options, category, imageUrl)
                showCreatePollDialog = false
            }
        )
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

@Composable
fun StoriesSection(
    stories: List<Story>,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(stories) { story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    if (story.isCurrentUser) onAvatarClick()
                }
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    UserAvatar(
                        name = story.name,
                        size = 62.dp,
                        hasStoryBorder = story.hasUnread
                    )
                    
                    if (story.isCurrentUser) {
                        // Blue '+' icon overlay for current user story
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.background)
                                .padding(1.5.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = story.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(66.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // "+99" Badge Circle at the end of stories
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+99",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "+99",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun RelevantProfilesSection(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(20.dp),
        border = CardStrokeHelper.getBorderStroke(isSystemInDarkTheme())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Perfis Relevantes",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Ver tudo",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val profiles = listOf(
                Pair("Gabriel Ferreira", "@gabrielf"),
                Pair("Juliana Costa", "@julianacosta"),
                Pair("Beatriz Lima", "@beatrizlima"),
                Pair("Lucas Martins", "@lucasmartins")
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(profiles) { profile ->
                    var isFollowing by remember { mutableStateOf(false) }
                    
                    Column(
                        modifier = Modifier
                            .width(130.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UserAvatar(name = profile.first, size = 48.dp)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = profile.first.split(" ").first(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            VerificationBadge(size = 12.dp)
                        }
                        
                        Text(
                            text = profile.second,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { isFollowing = !isFollowing },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                contentColor = if (isFollowing) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    Color.White
                                }
                            ),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isFollowing) "Seguindo" else "Seguir",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedPostCard(
    post: FeedPost,
    onLike: () -> Unit,
    onVote: (String) -> Unit,
    onPostClick: () -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onToggleFollow: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onReport: () -> Unit = {},
    onDelete: () -> Unit = {},
    isDetailMode: Boolean = false,
    currentUserUsername: String = "@marinasouza",
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    
    val cardModifier = if (isDetailMode) {
        modifier.fillMaxWidth()
    } else {
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onPostClick() }
    }
    
    val cardColors = CardDefaults.cardColors(
        containerColor = if (isDetailMode) Color.Transparent else MaterialTheme.colorScheme.surface
    )
    
    val cardElevation = CardDefaults.cardElevation(
        defaultElevation = if (isDetailMode) 0.dp else 1.dp
    )
    
    val borderStroke = if (isDetailMode) null else CardStrokeHelper.getBorderStroke(isDark)
    
    Card(
        modifier = cardModifier.testTag("feed_post_${post.id}"),
        colors = cardColors,
        elevation = cardElevation,
        shape = if (isDetailMode) RoundedCornerShape(0.dp) else RoundedCornerShape(20.dp),
        border = borderStroke
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isDetailMode) 20.dp else 16.dp)
        ) {
            // 1. Author Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    name = post.authorName, 
                    size = 44.dp,
                    modifier = Modifier.clickable { onUserClick(post.authorUsername) }
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onUserClick(post.authorUsername) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (post.authorUsername.equals(currentUserUsername, ignoreCase = true) || post.authorUsername == "@marinasouza" || post.authorName == "Ana Clara") {
                            Spacer(modifier = Modifier.width(4.dp))
                            VerificationBadge(size = 14.dp)
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorUsername,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = post.timeAgo,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                var showMenu by remember { mutableStateOf(false) }


                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("feed_post_menu_btn_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = "Mais opções",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        val isOwner = post.authorUsername.equals(currentUserUsername, ignoreCase = true) || post.authorUsername == "@marinasouza"
                        if (isOwner) {
                            DropdownMenuItem(
                                text = { Text("Excluir publicação") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                modifier = Modifier.testTag("feed_post_menu_delete_${post.id}")
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Denunciar Publicação") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Flag,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onReport()
                                },
                                modifier = Modifier.testTag("feed_post_menu_report_${post.id}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Tag (Enquete or Depoimento)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (post.tag == "Enquete") {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        } else {
                            Color(0xFFE0F7FA).copy(alpha = if (isDark) 0.15f else 0.8f)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = post.tag,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (post.tag == "Enquete") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        if (isDark) Color(0xFF80DEEA) else Color(0xFF006064)
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Question / Content Text
            Text(
                text = post.content,
                fontSize = if (post.tag == "Pensamento") 18.sp else 16.sp,
                fontWeight = if (post.tag == "Pensamento") FontWeight.Medium else FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 24.sp
            )

            if (post.imageUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Poll Options or Text Area
            if (post.isPoll) {
                PollOptionsLayout(post = post, onVote = onVote)
            } else {
                // Non-poll decorative divider or line
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Votes Metadata Count
            if (post.isPoll) {
                Text(
                    text = "${String.format("%,d", post.totalVotes).replace(",", ".")} votos",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Interaction Action Row (Instagram Style!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button & Count Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLike() }
                        .padding(end = 12.dp)
                ) {
                    IconButton(
                        onClick = onLike,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("like_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (post.hasLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Curtir",
                            tint = if (post.hasLiked) AccentPink else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "${post.likes}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Comment Button & Count Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onPostClick() }
                        .padding(end = 12.dp)
                ) {
                    IconButton(
                        onClick = onPostClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("comment_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Comment,
                            contentDescription = "Comentar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "${post.comments}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Save Button on the right
                IconButton(
                    onClick = onSave,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("save_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Salvar",
                        tint = if (post.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Likes row right below the buttons (subido, compact!)
            var showLikesDialog by remember { mutableStateOf(false) }
            LikedByRow(
                users = post.likedByUsers,
                likesCount = post.likes,
                onLikedByClick = { showLikesDialog = true },
                modifier = Modifier.padding(start = 4.dp, top = 0.dp)
            )

            if (showLikesDialog) {
                LikesListDialog(
                    users = post.likedByUsers,
                    onDismiss = { showLikesDialog = false },
                    onUserClick = onUserClick,
                    onToggleFollow = onToggleFollow
                )
            }

            // 7. Recent Comments Preview
            
        }
    }
}

@Composable
fun PollOptionsLayout(
    post: FeedPost,
    onVote: (String) -> Unit
) {
    val total = post.totalVotes.coerceAtLeast(1)
    val hasVoted = post.userSelectedOptionId != null

    // Determine if any options have images
    val hasImages = post.pollOptions.any { it.imageUrl != null }
    val useHorizontalLayout = hasImages

    if (useHorizontalLayout) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            post.pollOptions.forEachIndexed { index, option ->
                val percentage = ((option.votes.toFloat() / total.toFloat()) * 100).toInt()
                val isSelected = post.userSelectedOptionId == option.id
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(170.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onVote(option.id) }
                ) {
                    if (option.imageUrl != null) {
                        AsyncImage(
                            model = option.imageUrl,
                            contentDescription = option.text,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Black.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.75f))
                                    )
                                )
                        )
                    } else {
                        AbstractOptionBackground(index = index)
                    }

                    // Percentage overlay filled from the bottom!
                    val animatedProgress by animateFloatAsState(
                        targetValue = if (hasVoted) (percentage.toFloat() / 100f) else 0f,
                        animationSpec = tween(durationMillis = 800)
                    )

                    // Animated progress container drawing from the bottom upwards!
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(animatedProgress)
                            .align(Alignment.BottomCenter)
                            .background(
                                if (isSelected) {
                                    if (option.imageUrl != null) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                } else {
                                    if (option.imageUrl != null) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
                                }
                            )
                    )

                    // Information details
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top row with option text and a selection check indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = option.text,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (option.imageUrl != null) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Selecionado",
                                    tint = if (option.imageUrl != null) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Bottom row displaying percentage value if voted (removed "Votar" button completely)
                        if (hasVoted) {
                            Text(
                                text = "$percentage%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (option.imageUrl != null) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Vertical Layout for Poll Options (A, B, C...)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            post.pollOptions.forEachIndexed { index, option ->
                val percentage = ((option.votes.toFloat() / total.toFloat()) * 100).toInt()
                val isSelected = post.userSelectedOptionId == option.id
                val letter = ('A' + index).toChar().toString()

                val animatedProgress by animateFloatAsState(
                    targetValue = if (hasVoted) (percentage.toFloat() / 100f) else 0f,
                    animationSpec = tween(durationMillis = 800)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onVote(option.id) }
                ) {
                    // Custom drawn horizontal progress fill
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.06f)
                                }
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Letter Badge (A, B, C...)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letter,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = option.text,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        if (hasVoted) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Votado",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "$percentage%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AbstractOptionBackground(index: Int) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
    ) {
        val w = size.width
        val h = size.height

        if (index == 0) {
            // Draw a cute abstract bicycle road theme matching "Mais ciclovias"
            // Let's paint simple clean abstract lines and circles resembling wheels and roads
            val strokeColor = Color(0xFF6F3FF5).copy(alpha = 0.06f)
            drawCircle(
                color = strokeColor,
                radius = 35f,
                center = Offset(w * 0.3f, h * 0.7f),
                style = Stroke(width = 6f)
            )
            drawCircle(
                color = strokeColor,
                radius = 35f,
                center = Offset(w * 0.7f, h * 0.7f),
                style = Stroke(width = 6f)
            )
            drawLine(
                color = strokeColor,
                start = Offset(0f, h * 0.78f),
                end = Offset(w, h * 0.78f),
                strokeWidth = 6f
            )
            val path = Path().apply {
                moveTo(w * 0.3f, h * 0.7f)
                lineTo(w * 0.5f, h * 0.4f)
                lineTo(w * 0.7f, h * 0.7f)
                moveTo(w * 0.5f, h * 0.4f)
                lineTo(w * 0.45f, h * 0.32f)
                lineTo(w * 0.38f, h * 0.32f)
            }
            drawPath(path = path, color = strokeColor, style = Stroke(width = 5f))
        } else {
            // Draw a cute abstract tree theme matching "Mais áreas verdes"
            // Beautiful rounded green-blue cloud/tree bubbles and stems
            val treeColor = Color(0xFF4CAF50).copy(alpha = 0.06f)
            drawCircle(
                color = treeColor,
                radius = 42f,
                center = Offset(w * 0.5f, h * 0.42f)
            )
            drawCircle(
                color = treeColor,
                radius = 30f,
                center = Offset(w * 0.36f, h * 0.52f)
            )
            drawCircle(
                color = treeColor,
                radius = 30f,
                center = Offset(w * 0.64f, h * 0.52f)
            )
            drawLine(
                color = treeColor,
                start = Offset(w * 0.5f, h * 0.42f),
                end = Offset(w * 0.5f, h * 0.9f),
                strokeWidth = 8f
            )
        }
    }
}

@Composable
fun InteractionButton(
    icon: ImageVector,
    text: String,
    tint: Color,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(6.dp))
        
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = tint
        )
    }
}

@Composable
fun CreatePollDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit,
    onCreateCustom: (String, Boolean, List<com.example.model.PollOption>, String, String?) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Enquete, 1 = Pensamento
    var selectedCategory by remember { mutableStateOf("Outros") }
    
    // Enquete state
    var question by remember { mutableStateOf("") }
    var option1 by remember { mutableStateOf("") }
    var option2 by remember { mutableStateOf("") }
    var option1Image by remember { mutableStateOf<String?>(null) }
    var option2Image by remember { mutableStateOf<String?>(null) }
    
    // Pensamento state
    var thoughtText by remember { mutableStateOf("") }
    var thoughtImage by remember { mutableStateOf<String?>(null) }
    
    var showImageSelectorForOption by remember { mutableStateOf<Int?>(null) } // null, 1, 2, 3 (3 for thought image)
    
    // Beautiful default mock images the user can select
    val mockImages = listOf(
        "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=400&auto=format&fit=crop", // Mountains
        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=400&auto=format&fit=crop", // Beach
        "https://images.unsplash.com/photo-1541614101331-1a5a3a194e92?w=600&auto=format&fit=crop", // Bicycle
        "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400&auto=format&fit=crop", // Tech
        "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=400&auto=format&fit=crop"  // Education
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Text(
                    text = "Criar Nova Publicação",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tabs: Enquete vs Pensamentos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🗳️ Enquete",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💭 Pensamento",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Categories Horizontal Scroll "arrastando pro lado"
                Text(
                    text = "Selecione uma Categoria:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Filter out "Todos" from create post categories
                    items(CATEGORIES.filter { it.first != "Todos" }) { (catName, emoji) ->
                        val isCatSelected = selectedCategory == catName
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isCatSelected) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isCatSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedCategory = catName }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$emoji $catName",
                                fontSize = 12.sp,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCatSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Fields based on selectedTab
                if (selectedTab == 0) {
                    // Enquete form
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        label = { Text("Pergunta da Enquete") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Option 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = option1,
                            onValueChange = { option1 = it },
                            label = { Text("Opção 1") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        // Image select icon "na frente um icone de imagem caso tenha"
                        IconButton(
                            onClick = { showImageSelectorForOption = 1 },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (option1Image != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                        ) {
                            if (option1Image != null) {
                                AsyncImage(
                                    model = option1Image,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.AddPhotoAlternate,
                                    contentDescription = "Adicionar imagem",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Option 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = option2,
                            onValueChange = { option2 = it },
                            label = { Text("Opção 2") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        // Image select icon "na frente um icone de imagem caso tenha"
                        IconButton(
                            onClick = { showImageSelectorForOption = 2 },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (option2Image != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                        ) {
                            if (option2Image != null) {
                                AsyncImage(
                                    model = option2Image,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.AddPhotoAlternate,
                                    contentDescription = "Adicionar imagem",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                } else {
                    // Pensamento form
                    OutlinedTextField(
                        value = thoughtText,
                        onValueChange = { thoughtText = it },
                        label = { Text("O que você está pensando?") },
                        placeholder = { Text("Escreva aqui seu pensamento...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Attaching optional image to thought
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .clickable { showImageSelectorForOption = 3 }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (thoughtImage != null) "Imagem adicionada!" else "Anexar imagem (opcional)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        if (thoughtImage != null) {
                            AsyncImage(
                                model = thoughtImage,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (selectedTab == 0) {
                                // Enquete
                                if (question.isNotBlank() && option1.isNotBlank() && option2.isNotBlank()) {
                                    val options = listOf(
                                        com.example.model.PollOption("opt-new-1", option1, imageUrl = option1Image, votes = 0),
                                        com.example.model.PollOption("opt-new-2", option2, imageUrl = option2Image, votes = 0)
                                    )
                                    onCreateCustom(question, true, options, selectedCategory, null)
                                }
                            } else {
                                // Pensamento
                                if (thoughtText.isNotBlank()) {
                                    onCreateCustom(thoughtText, false, emptyList(), selectedCategory, thoughtImage)
                                }
                            }
                        },
                        enabled = if (selectedTab == 0) {
                            question.isNotBlank() && option1.isNotBlank() && option2.isNotBlank()
                        } else {
                            thoughtText.isNotBlank()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Publicar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    
    // Quick modal selector for Mock Images
    if (showImageSelectorForOption != null) {
        Dialog(onDismissRequest = { showImageSelectorForOption = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Escolha uma Imagem",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Options
                        mockImages.forEachIndexed { idx, url ->
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        when (showImageSelectorForOption) {
                                            1 -> option1Image = url
                                            2 -> option2Image = url
                                            3 -> thoughtImage = url
                                        }
                                        showImageSelectorForOption = null
                                    }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(onClick = { showImageSelectorForOption = null }) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

object CardStrokeHelper {
    @Composable
    fun getBorderStroke(isDark: Boolean): BorderStroke? {
        return if (isDark) {
            BorderStroke(1.dp, Color(0xFF1E1636))
        } else {
            BorderStroke(1.dp, Color(0xFFEBE9F5))
        }
    }
}

@Composable
fun LikedByRow(
    users: List<UserProfile>,
    likesCount: Int,
    onLikedByClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onLikedByClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (users.isNotEmpty()) {
            Box(
                modifier = Modifier.padding(end = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Show up to 3 overlapping avatars
                val displayUsers = users.take(3)
                displayUsers.forEachIndexed { index, user ->
                    Box(
                        modifier = Modifier
                            .padding(start = (index * 12).dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(1.dp)
                            .clip(CircleShape)
                    ) {
                        UserAvatar(name = user.name, size = 18.dp)
                    }
                }
            }
            Spacer(modifier = Modifier.width((if (users.size > 1) (users.size * 3) else 4).dp))
        }
        
        val text = if (likesCount <= 0) {
            "Nenhuma curtida"
        } else if (likesCount == 1) {
            if (users.isNotEmpty()) "Curtido por ${users[0].name}" else "1 curtida"
        } else {
            if (users.isNotEmpty()) "Curtido por ${users[0].name} e mais ${likesCount - 1} pessoas" else "$likesCount curtidas"
        }
        
        Text(
            text = text,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LikesListDialog(
    users: List<UserProfile>,
    onDismiss: () -> Unit,
    onUserClick: (String) -> Unit,
    onToggleFollow: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(min = 380.dp, max = 550.dp)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Curtidas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Fechar")
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                if (users.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma curtida ainda.",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .heightIn(min = 280.dp, max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(users) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        onDismiss()
                                        onUserClick(user.username) 
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(name = user.name, size = 36.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = user.username,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                                
                                if (user.username != "@marinasouza") {
                                    Button(
                                        onClick = { onToggleFollow(user.username) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (user.isFollowing) {
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            }
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = if (user.isFollowing) "Seguindo" else "Seguir",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (user.isFollowing) MaterialTheme.colorScheme.primary else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditPostDialog(
    initialContent: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var content by remember { mutableStateOf(initialContent) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Publicação") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("edit_post_input"),
                    label = { Text("Conteúdo") },
                    maxLines = 6
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        onSubmit(content)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("edit_post_submit_btn")
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


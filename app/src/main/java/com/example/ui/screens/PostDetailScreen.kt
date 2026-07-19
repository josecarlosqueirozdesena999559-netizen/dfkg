package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Comment
import com.example.model.FeedPost
import androidx.compose.material.icons.outlined.Flag
import com.example.ui.components.ReportDialog
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.AccentPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    post: FeedPost,
    comments: List<Comment>,
    onLikePost: () -> Unit,
    onVotePoll: (String) -> Unit,
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit,
    onAddComment: (String) -> Unit,
    onLikeComment: (String) -> Unit,
    onSavePost: () -> Unit = {},
    onReportPost: (String, String, String) -> Unit = { _, _, _ -> },
    onReportComment: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onDeletePost: (String) -> Unit = { _ -> },
    modifier: Modifier = Modifier
) {
    var commentText by remember { mutableStateOf("") }
    var reportTargetPost by remember { mutableStateOf<FeedPost?>(null) }
    var reportTargetComment by remember { mutableStateOf<Comment?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Publicação",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Comment input bar
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Adicione um comentário...", fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input_field"),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onAddComment(commentText)
                                commentText = ""
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("submit_comment_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Enviar comentário",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 1. Post Content
            item {
                FeedPostCard(
                    post = post,
                    onLike = onLikePost,
                    onVote = onVotePoll,
                    onPostClick = {}, // Inside detail, no-op or disable card click
                    onUserClick = onUserClick,
                    onToggleFollow = {}, // Filled if needed
                    onSave = onSavePost,
                    onReport = { reportTargetPost = post },
                    onDelete = { onDeletePost(post.id) },
                    isDetailMode = true,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Comentários (${comments.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 2. Comments List
            if (comments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Seja o primeiro a comentar!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                items(comments) { comment ->
                    CommentItem(
                        comment = comment,
                        onUserClick = onUserClick,
                        onLikeClick = { onLikeComment(comment.id) },
                        onReportClick = { reportTargetComment = comment },
                        onReplyClick = { commentText = "${comment.authorUsername} " }
                    )
                }
            }
        }
    }

    reportTargetPost?.let { p ->
        ReportDialog(
            onDismiss = { reportTargetPost = null },
            onSubmit = { reason, details ->
                onReportPost(p.id, reason, details)
            }
        )
    }

    reportTargetComment?.let { c ->
        ReportDialog(
            onDismiss = { reportTargetComment = null },
            onSubmit = { reason, details ->
                onReportComment(c.id, post.id, reason, details)
            }
        )
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    onUserClick: (String) -> Unit,
    onLikeClick: () -> Unit,
    onReportClick: () -> Unit = {},
    onReplyClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        UserAvatar(
            name = comment.authorName,
            size = 36.dp,
            modifier = Modifier.clickable { onUserClick(comment.authorUsername) }
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onUserClick(comment.authorUsername) }
            ) {
                Text(
                    text = comment.authorName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (comment.authorUsername == "@anaclara") {
                    Spacer(modifier = Modifier.width(4.dp))
                    VerificationBadge(size = 12.dp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = comment.authorUsername,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = comment.timestamp,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = comment.text,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Responder",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier
                    .clickable { onReplyClick() }
                    .testTag("reply_comment_btn_${comment.id}")
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Comment Action buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onLikeClick() }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (comment.hasLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Curtir comentário",
                    tint = if (comment.hasLiked) AccentPink else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                if (comment.likes > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = comment.likes.toString(),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            IconButton(
                onClick = onReportClick,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("report_comment_btn_${comment.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = "Denunciar comentário",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

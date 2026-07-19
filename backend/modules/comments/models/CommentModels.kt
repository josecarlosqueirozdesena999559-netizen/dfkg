package com.decisoes.modules.comments.models

import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatarUrl: String? = null,
    val parentCommentId: String? = null,
    val content: String,
    val likesCount: Long = 0,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateCommentRequest(
    val content: String,
    val parentCommentId: String? = null
)

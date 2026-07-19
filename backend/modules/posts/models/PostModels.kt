package com.decisoes.modules.posts.models

import kotlinx.serialization.Serializable

@Serializable
data class PollOptionDto(
    val id: String,
    val pollId: String,
    val text: String,
    val imageUrl: String? = null,
    val votesCount: Long = 0,
    val displayOrder: Int
)

@Serializable
data class PollDto(
    val id: String,
    val postId: String,
    val question: String,
    val totalVotes: Long = 0,
    val expiresAt: String? = null,
    val options: List<PollOptionDto> = emptyList()
)

@Serializable
data class PostDto(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatarUrl: String? = null,
    val type: String, // TEXT, IMAGE, POLL, QUESTION
    val content: String? = null,
    val visibility: String,
    val status: String,
    val likesCount: Long = 0,
    val commentsCount: Long = 0,
    val sharesCount: Long = 0,
    val viewsCount: Long = 0,
    val hasLiked: Boolean = false,
    val poll: PollDto? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreatePollOptionRequest(
    val text: String,
    val imageUrl: String? = null,
    val displayOrder: Int
)

@Serializable
data class CreatePollRequest(
    val question: String,
    val expiresAt: String? = null,
    val options: List<CreatePollOptionRequest>
)

@Serializable
data class CreatePostRequest(
    val type: String, // TEXT, IMAGE, POLL, QUESTION
    val content: String? = null,
    val visibility: String = "PUBLIC",
    val poll: CreatePollRequest? = null
)

@Serializable
data class VoteRequest(
    val optionId: String
)

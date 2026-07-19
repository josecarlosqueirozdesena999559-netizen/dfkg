package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PollOptionDto(
    @Json(name = "id") val id: String,
    @Json(name = "pollId") val pollId: String,
    @Json(name = "text") val text: String,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "votesCount") val votesCount: Long = 0,
    @Json(name = "displayOrder") val displayOrder: Int
)

@JsonClass(generateAdapter = true)
data class PollDto(
    @Json(name = "id") val id: String,
    @Json(name = "postId") val postId: String,
    @Json(name = "question") val question: String,
    @Json(name = "totalVotes") val totalVotes: Long = 0,
    @Json(name = "expiresAt") val expiresAt: String? = null,
    @Json(name = "options") val options: List<PollOptionDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PostDto(
    @Json(name = "id") val id: String,
    @Json(name = "authorId") val authorId: String,
    @Json(name = "authorName") val authorName: String,
    @Json(name = "authorUsername") val authorUsername: String,
    @Json(name = "authorAvatarUrl") val authorAvatarUrl: String? = null,
    @Json(name = "type") val type: String, // TEXT, IMAGE, POLL, QUESTION
    @Json(name = "content") val content: String? = null,
    @Json(name = "visibility") val visibility: String,
    @Json(name = "status") val status: String,
    @Json(name = "likesCount") val likesCount: Long = 0,
    @Json(name = "commentsCount") val commentsCount: Long = 0,
    @Json(name = "sharesCount") val sharesCount: Long = 0,
    @Json(name = "viewsCount") val viewsCount: Long = 0,
    @Json(name = "hasLiked") val hasLiked: Boolean = false,
    @Json(name = "poll") val poll: PollDto? = null,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class CreatePollOptionRequest(
    @Json(name = "text") val text: String,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "displayOrder") val displayOrder: Int
)

@JsonClass(generateAdapter = true)
data class CreatePollRequest(
    @Json(name = "question") val question: String,
    @Json(name = "expiresAt") val expiresAt: String? = null,
    @Json(name = "options") val options: List<CreatePollOptionRequest>
)

@JsonClass(generateAdapter = true)
data class CreatePostRequest(
    @Json(name = "type") val type: String, // TEXT, IMAGE, POLL, QUESTION
    @Json(name = "content") val content: String? = null,
    @Json(name = "visibility") val visibility: String = "PUBLIC",
    @Json(name = "poll") val poll: CreatePollRequest? = null
)

@JsonClass(generateAdapter = true)
data class VoteRequest(
    @Json(name = "optionId") val optionId: String
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id") val id: String,
    @Json(name = "firebaseUid") val firebaseUid: String,
    @Json(name = "email") val email: String,
    @Json(name = "username") val username: String,
    @Json(name = "status") val status: String,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String,
    @Json(name = "lastLoginAt") val lastLoginAt: String? = null
)

@JsonClass(generateAdapter = true)
data class UserProfileDto(
    @Json(name = "userId") val userId: String,
    @Json(name = "displayName") val displayName: String,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "profileImageUrl") val profileImageUrl: String? = null,
    @Json(name = "coverImageUrl") val coverImageUrl: String? = null,
    @Json(name = "followersCount") val followersCount: Long = 0,
    @Json(name = "followingCount") val followingCount: Long = 0,
    @Json(name = "postsCount") val postsCount: Long = 0,
    @Json(name = "verified") val verified: Boolean = false,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class MeProfileDto(
    @Json(name = "user") val user: UserDto,
    @Json(name = "profile") val profile: UserProfileDto
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @Json(name = "displayName") val displayName: String? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "profileImageUrl") val profileImageUrl: String? = null,
    @Json(name = "coverImageUrl") val coverImageUrl: String? = null
)



@JsonClass(generateAdapter = true)
data class CommentDto(
    @Json(name = "id") val id: String,
    @Json(name = "postId") val postId: String,
    @Json(name = "authorId") val authorId: String,
    @Json(name = "authorName") val authorName: String,
    @Json(name = "authorUsername") val authorUsername: String,
    @Json(name = "authorAvatarUrl") val authorAvatarUrl: String? = null,
    @Json(name = "parentCommentId") val parentCommentId: String? = null,
    @Json(name = "content") val content: String,
    @Json(name = "likesCount") val likesCount: Long = 0,
    @Json(name = "status") val status: String,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class CreateCommentRequest(
    @Json(name = "content") val content: String,
    @Json(name = "parentCommentId") val parentCommentId: String? = null
)

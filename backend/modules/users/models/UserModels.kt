package com.decisoes.modules.users.models

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val firebaseUid: String,
    val email: String,
    val username: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val lastLoginAt: String? = null
)

@Serializable
data class UserProfileDto(
    val userId: String,
    val displayName: String,
    val bio: String? = null,
    val profileImageUrl: String? = null,
    val coverImageUrl: String? = null,
    val followersCount: Long = 0,
    val followingCount: Long = 0,
    val postsCount: Long = 0,
    val verified: Boolean = false,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class MeProfileDto(
    val user: UserDto,
    val profile: UserProfileDto
)

@Serializable
data class UpdateProfileRequest(
    val displayName: String? = null,
    val bio: String? = null,
    val profileImageUrl: String? = null,
    val coverImageUrl: String? = null
)

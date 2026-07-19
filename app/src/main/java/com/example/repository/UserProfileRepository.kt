package com.example.repository

import com.example.model.UserProfile
import com.example.network.ApiService
import com.example.network.UpdateProfileRequest
import com.example.network.MeProfileDto
import com.example.network.UserProfileDto

interface UserProfileRepository {
    suspend fun getMe(): UserProfile
    suspend fun getUserProfile(username: String): UserProfile
    suspend fun updateProfile(name: String, bio: String, avatarUrl: String, coverUrl: String): UserProfile
}

class ApiUserProfileRepository(private val apiService: ApiService) : UserProfileRepository {

    override suspend fun getMe(): UserProfile {
        val dto = apiService.getMe()
        return mapToDomain(dto)
    }

    override suspend fun getUserProfile(username: String): UserProfile {
        val cleanUsername = username.replace("@", "").trim()
        val dto = apiService.getUserProfile(cleanUsername)
        return mapToDomain(dto)
    }

    override suspend fun updateProfile(
        name: String,
        bio: String,
        avatarUrl: String,
        coverUrl: String
    ): UserProfile {
        val request = UpdateProfileRequest(
            displayName = name,
            bio = bio,
            profileImageUrl = avatarUrl,
            coverImageUrl = coverUrl
        )
        apiService.updateProfile(request)
        // Retrieve fresh full profile to ensure all counts and fields are completely accurate
        return getMe()
    }

    private fun mapToDomain(dto: MeProfileDto): UserProfile {
        val originalUsername = dto.user.username
        val formattedUsername = if (originalUsername.startsWith("@")) originalUsername else "@$originalUsername"
        
        return UserProfile(
            id = dto.profile.userId,
            name = dto.profile.displayName,
            username = formattedUsername,
            avatarUrl = dto.profile.profileImageUrl ?: "marina",
            bio = dto.profile.bio ?: "",
            publicationsCount = dto.profile.postsCount.toInt(),
            followersCount = formatCount(dto.profile.followersCount),
            followingCount = dto.profile.followingCount.toInt(),
            isVerified = dto.profile.verified,
            isFollowing = false, // Set dynamically if needed
            coverUrl = dto.profile.coverImageUrl ?: "purple"
        )
    }

    private fun formatCount(count: Long): String {
        return if (count >= 1000) {
            val k = count / 1000.0
            String.format("%.1fK", k).replace(",0", "")
        } else {
            count.toString()
        }
    }
}

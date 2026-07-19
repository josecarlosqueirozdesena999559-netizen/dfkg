package com.example.usecase

import com.example.model.UserProfile
import com.example.repository.UserProfileRepository

class UpdateProfileUseCase(private val repository: UserProfileRepository) {
    suspend fun execute(name: String, bio: String, avatarUrl: String, coverUrl: String): UserProfile {
        return repository.updateProfile(name, bio, avatarUrl, coverUrl)
    }
}

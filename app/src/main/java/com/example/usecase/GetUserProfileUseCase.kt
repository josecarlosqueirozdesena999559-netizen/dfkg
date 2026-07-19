package com.example.usecase

import com.example.model.UserProfile
import com.example.repository.UserProfileRepository

class GetUserProfileUseCase(private val repository: UserProfileRepository) {
    suspend fun execute(username: String): UserProfile {
        return repository.getUserProfile(username)
    }
}

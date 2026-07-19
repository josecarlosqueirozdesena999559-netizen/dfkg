package com.example.usecase

import com.example.model.UserProfile
import com.example.repository.UserProfileRepository

class GetMeUseCase(private val repository: UserProfileRepository) {
    suspend fun execute(): UserProfile {
        return repository.getMe()
    }
}

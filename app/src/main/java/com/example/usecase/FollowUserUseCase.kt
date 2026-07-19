package com.example.usecase

import com.example.repository.FollowRepository

class FollowUserUseCase(
    private val repository: FollowRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        return repository.followUser(userId)
    }
}

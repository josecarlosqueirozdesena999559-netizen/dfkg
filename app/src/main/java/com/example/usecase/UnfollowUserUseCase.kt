package com.example.usecase

import com.example.repository.FollowRepository

class UnfollowUserUseCase(
    private val repository: FollowRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        return repository.unfollowUser(userId)
    }
}

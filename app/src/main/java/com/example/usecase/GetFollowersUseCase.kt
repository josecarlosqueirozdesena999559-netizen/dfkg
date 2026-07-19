package com.example.usecase

import com.example.network.UserProfileDto
import com.example.repository.FollowRepository

class GetFollowersUseCase(
    private val repository: FollowRepository
) {
    suspend operator fun invoke(userId: String): Result<List<UserProfileDto>> {
        return repository.getFollowers(userId)
    }
}

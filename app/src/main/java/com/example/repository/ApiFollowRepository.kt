package com.example.repository

import com.example.network.ApiService
import com.example.network.UserProfileDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiFollowRepository(
    private val apiService: ApiService
) : FollowRepository {
    override suspend fun followUser(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.followUser(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unfollowUser(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.unfollowUser(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFollowers(userId: String): Result<List<UserProfileDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFollowers(userId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFollowing(userId: String): Result<List<UserProfileDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFollowing(userId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

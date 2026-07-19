package com.example.repository

import com.example.network.UserProfileDto

interface FollowRepository {
    suspend fun followUser(userId: String): Result<Unit>
    suspend fun unfollowUser(userId: String): Result<Unit>
    suspend fun getFollowers(userId: String): Result<List<UserProfileDto>>
    suspend fun getFollowing(userId: String): Result<List<UserProfileDto>>
}

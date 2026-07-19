content = """package com.decisoes.modules.follows.services

import com.decisoes.modules.follows.repositories.FollowRepository
import com.decisoes.modules.users.models.UserProfileDto
import java.util.UUID

class FollowService(private val followRepository: FollowRepository) {
    suspend fun followUser(followerId: UUID, followingId: UUID): Boolean {
        require(followerId != followingId) { "You cannot follow yourself" }
        return followRepository.follow(followerId, followingId)
    }

    suspend fun unfollowUser(followerId: UUID, followingId: UUID): Boolean {
        return followRepository.unfollow(followerId, followingId)
    }

    suspend fun getFollowers(userId: UUID): List<UserProfileDto> {
        return followRepository.getFollowers(userId)
    }

    suspend fun getFollowing(userId: UUID): List<UserProfileDto> {
        return followRepository.getFollowing(userId)
    }
}
"""

with open('backend/modules/follows/services/FollowService.kt', 'w') as f:
    f.write(content)

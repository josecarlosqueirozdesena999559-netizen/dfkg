package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostRemoteDataSource(private val apiService: ApiService) {
    suspend fun getFeed(limit: Int, cursor: String?): List<PostDto> {
        return apiService.getFeed(limit, cursor)
    }

    suspend fun createPost(request: CreatePostRequest): PostDto {
        return apiService.createPost(request)
    }

    suspend fun likePost(postId: String): Any {
        return apiService.likePost(postId)
    }

    suspend fun unlikePost(postId: String): Any {
        return apiService.unlikePost(postId)
    }

    suspend fun vote(pollId: String, optionId: String): Any {
        return apiService.vote(pollId, VoteRequest(optionId))
    }

    suspend fun deletePost(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.deletePost(postId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

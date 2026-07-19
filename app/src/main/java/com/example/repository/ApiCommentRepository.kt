package com.example.repository

import com.example.network.ApiService
import com.example.network.CommentDto
import com.example.network.CreateCommentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiCommentRepository(
    private val apiService: ApiService
) : CommentRepository {
    override suspend fun getCommentsForPost(postId: String): Result<List<CommentDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getComments(postId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addComment(
        postId: String,
        content: String,
        parentCommentId: String?
    ): Result<CommentDto> = withContext(Dispatchers.IO) {
        try {
            val request = CreateCommentRequest(content = content, parentCommentId = parentCommentId)
            val response = apiService.addComment(postId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

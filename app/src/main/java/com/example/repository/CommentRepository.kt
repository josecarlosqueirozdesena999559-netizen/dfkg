package com.example.repository

import com.example.network.CommentDto

interface CommentRepository {
    suspend fun getCommentsForPost(postId: String): Result<List<CommentDto>>
    suspend fun addComment(postId: String, content: String, parentCommentId: String? = null): Result<CommentDto>
}

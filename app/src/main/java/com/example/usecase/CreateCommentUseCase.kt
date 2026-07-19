package com.example.usecase

import com.example.network.CommentDto
import com.example.repository.CommentRepository

class CreateCommentUseCase(
    private val repository: CommentRepository
) {
    suspend operator fun invoke(postId: String, content: String, parentCommentId: String? = null): Result<CommentDto> {
        return repository.addComment(postId, content, parentCommentId)
    }
}

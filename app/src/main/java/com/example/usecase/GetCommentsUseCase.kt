package com.example.usecase

import com.example.network.CommentDto
import com.example.repository.CommentRepository

class GetCommentsUseCase(
    private val repository: CommentRepository
) {
    suspend operator fun invoke(postId: String): Result<List<CommentDto>> {
        return repository.getCommentsForPost(postId)
    }
}

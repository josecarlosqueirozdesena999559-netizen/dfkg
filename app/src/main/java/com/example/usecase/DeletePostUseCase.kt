package com.example.usecase

import com.example.repository.PostRepository

class DeletePostUseCase(
    private val repository: PostRepository
) {
    suspend operator fun invoke(postId: String): Result<Unit> {
        return repository.deletePost(postId)
    }
}

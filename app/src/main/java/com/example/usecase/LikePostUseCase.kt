package com.example.usecase

import com.example.repository.PostRepository

class LikePostUseCase(private val postRepository: PostRepository) {
    suspend fun execute(postId: String, shouldLike: Boolean): Result<Unit> {
        return if (shouldLike) {
            postRepository.likePost(postId)
        } else {
            postRepository.unlikePost(postId)
        }
    }
}

package com.example.usecase

import com.example.repository.PostRepository

class VoteUseCase(private val postRepository: PostRepository) {
    suspend fun execute(pollId: String, optionId: String): Result<Unit> {
        return postRepository.vote(pollId, optionId)
    }
}

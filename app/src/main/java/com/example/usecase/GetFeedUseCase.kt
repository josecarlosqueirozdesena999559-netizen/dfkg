package com.example.usecase

import com.example.model.FeedPost
import com.example.repository.PostRepository

class GetFeedUseCase(private val postRepository: PostRepository) {
    suspend fun execute(limit: Int, cursor: String?): List<FeedPost> {
        return postRepository.getFeed(limit, cursor)
    }
}

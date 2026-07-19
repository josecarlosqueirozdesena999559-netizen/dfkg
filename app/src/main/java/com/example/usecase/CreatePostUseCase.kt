package com.example.usecase

import com.example.model.FeedPost
import com.example.model.PollOption
import com.example.repository.PostRepository

class CreatePostUseCase(private val postRepository: PostRepository) {
    suspend fun execute(
        content: String,
        isPoll: Boolean,
        options: List<PollOption>,
        category: String,
        imageUrl: String? = null
    ): FeedPost {
        return postRepository.createPost(content, isPoll, options, category, imageUrl)
    }
}

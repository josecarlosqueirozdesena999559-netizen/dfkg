package com.example.repository

import com.example.model.FeedPost
import com.example.model.PollOption

interface PostRepository {
    suspend fun getFeed(limit: Int, cursor: String?): List<FeedPost>
    suspend fun createPost(content: String, isPoll: Boolean, options: List<PollOption>, category: String, imageUrl: String?): FeedPost
    suspend fun likePost(postId: String): Result<Unit>
    suspend fun unlikePost(postId: String): Result<Unit>
    suspend fun vote(pollId: String, optionId: String): Result<Unit>
    suspend fun deletePost(postId: String): Result<Unit>
}

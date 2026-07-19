content = """package com.example.repository

import com.example.model.FeedPost
import com.example.model.PollOption
import com.example.network.CreatePollOptionRequest
import com.example.network.CreatePollRequest
import com.example.network.CreatePostRequest
import com.example.network.PostRemoteDataSource

class ApiPostRepository(
    private val dataSource: PostRemoteDataSource
) : PostRepository {

    override suspend fun getFeed(limit: Int, cursor: String?): List<FeedPost> {
        val result = dataSource.getFeed(limit, cursor)
        if (result.isSuccess) {
            val dtoList = result.getOrNull() ?: emptyList()
            return dtoList.map { dto ->
                FeedPost(
                    id = dto.id,
                    authorName = dto.authorName,
                    authorUsername = dto.authorUsername,
                    authorAvatar = dto.authorAvatarUrl ?: "purple",
                    timeAgo = "Recent", // In a real app, parse dto.createdAt to relative time
                    tag = dto.type,
                    content = dto.content ?: "",
                    isPoll = dto.type == "POLL",
                    pollOptions = dto.poll?.options?.map { optDto ->
                        PollOption(
                            id = optDto.id,
                            text = optDto.text,
                            imageUrl = optDto.imageUrl,
                            votes = optDto.votesCount.toInt()
                        )
                    } ?: emptyList(),
                    totalVotes = dto.poll?.totalVotes?.toInt() ?: 0,
                    likes = dto.likesCount.toInt(),
                    comments = dto.commentsCount.toInt(),
                    shares = dto.sharesCount.toInt(),
                    hasLiked = dto.hasLiked,
                    category = "API",
                    imageUrl = null // If API supports post images, map it here
                )
            }
        }
        return emptyList()
    }

    override suspend fun createPost(
        content: String,
        isPoll: Boolean,
        options: List<PollOption>,
        category: String,
        imageUrl: String?
    ): FeedPost {
        val request = CreatePostRequest(
            type = if (isPoll) "POLL" else "TEXT",
            content = content,
            poll = if (isPoll) {
                CreatePollRequest(
                    question = content,
                    options = options.mapIndexed { index, opt ->
                        CreatePollOptionRequest(
                            text = opt.text,
                            displayOrder = index
                        )
                    }
                )
            } else null
        )

        val result = dataSource.createPost(request)
        val dto = result.getOrNull() ?: throw Exception("Failed to create post")
        
        return FeedPost(
            id = dto.id,
            authorName = dto.authorName,
            authorUsername = dto.authorUsername,
            authorAvatar = dto.authorAvatarUrl ?: "purple",
            timeAgo = "Agora",
            tag = dto.type,
            content = dto.content ?: "",
            isPoll = dto.type == "POLL",
            pollOptions = dto.poll?.options?.map { optDto ->
                PollOption(
                    id = optDto.id,
                    text = optDto.text,
                    votes = optDto.votesCount.toInt()
                )
            } ?: emptyList(),
            totalVotes = 0,
            likes = 0,
            comments = 0,
            shares = 0,
            hasLiked = false,
            category = category
        )
    }

    override suspend fun likePost(postId: String): Boolean {
        val result = dataSource.likePost(postId)
        return result.isSuccess
    }

    override suspend fun unlikePost(postId: String): Boolean {
        val result = dataSource.unlikePost(postId)
        return result.isSuccess
    }

    override suspend fun vote(pollId: String, optionId: String): Boolean {
        val result = dataSource.vote(pollId, optionId)
        return result.isSuccess
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        return dataSource.deletePost(postId)
    }
}
"""

with open('app/src/main/java/com/example/repository/ApiPostRepository.kt', 'w') as f:
    f.write(content)

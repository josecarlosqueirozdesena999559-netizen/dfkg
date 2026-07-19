content1 = """package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostRemoteDataSource(private val apiService: ApiService) {
    suspend fun getFeed(limit: Int, cursor: String?): List<PostDto> {
        return apiService.getFeed(limit, cursor)
    }

    suspend fun createPost(request: CreatePostRequest): PostDto {
        return apiService.createPost(request)
    }

    suspend fun likePost(postId: String): Any {
        return apiService.likePost(postId)
    }

    suspend fun unlikePost(postId: String): Any {
        return apiService.unlikePost(postId)
    }

    suspend fun vote(pollId: String, optionId: String): Any {
        return apiService.vote(pollId, VoteRequest(optionId))
    }

    suspend fun deletePost(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.deletePost(postId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
"""

with open('app/src/main/java/com/example/network/PostRemoteDataSource.kt', 'w') as f:
    f.write(content1)

content2 = """package com.example.repository

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
        return try {
            val dtoList = dataSource.getFeed(limit, cursor)
            dtoList.map { dto ->
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
                    imageUrl = null
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
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

        val dto = dataSource.createPost(request)
        
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
        return try {
            dataSource.likePost(postId)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun unlikePost(postId: String): Boolean {
        return try {
            dataSource.unlikePost(postId)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun vote(pollId: String, optionId: String): Boolean {
        return try {
            dataSource.vote(pollId, optionId)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        return dataSource.deletePost(postId)
    }
}
"""

with open('app/src/main/java/com/example/repository/ApiPostRepository.kt', 'w') as f:
    f.write(content2)


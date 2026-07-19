package com.decisoes.modules.posts.services

import com.decisoes.modules.posts.models.PostDto
import com.decisoes.modules.posts.models.CreatePostRequest
import com.decisoes.modules.posts.repositories.PostRepository
import java.util.UUID
import java.time.Instant

class PostService(private val postRepository: PostRepository) {

    suspend fun getFeed(limit: Int, cursor: Instant?, viewerId: UUID? = null): List<PostDto> {
        return postRepository.findFeed(limit, cursor, viewerId)
    }

    suspend fun getPost(postId: UUID, viewerId: UUID? = null): PostDto? {
        return postRepository.findById(postId, viewerId)
    }

    suspend fun createPost(authorId: UUID, req: CreatePostRequest): PostDto {
        val postId = UUID.randomUUID()
        val type = req.type.uppercase().trim()
        val content = req.content

        require(type in listOf("TEXT", "IMAGE", "POLL", "QUESTION")) {
            "Invalid post type: must be TEXT, IMAGE, POLL, or QUESTION"
        }

        if (type == "POLL") {
            val pollReq = req.poll ?: throw IllegalArgumentException("Poll details are required for POLL type posts")
            require(pollReq.options.size >= 2) { "A poll must have at least two options" }
            
            // Create post first
            postRepository.createPost(postId, authorId, type, content, req.visibility)

            // Create poll
            val pollId = UUID.randomUUID()
            val expiresAt = pollReq.expiresAt?.let { Instant.parse(it) }
            postRepository.createPoll(pollId, postId, pollReq.question, expiresAt)

            // Create poll options
            pollReq.options.forEach { opt ->
                val optId = UUID.randomUUID()
                postRepository.createPollOption(optId, pollId, opt.text, opt.imageUrl, opt.displayOrder)
            }

            return postRepository.findById(postId, authorId)!!
        } else {
            return postRepository.createPost(postId, authorId, type, content, req.visibility)
        }
    }

    suspend fun deletePost(postId: UUID, authorId: UUID): Boolean {
        return postRepository.deletePost(postId, authorId)
    }

    suspend fun likePost(postId: UUID, userId: UUID): Boolean {
        return postRepository.addLike(postId, userId)
    }

    suspend fun unlikePost(postId: UUID, userId: UUID): Boolean {
        return postRepository.removeLike(postId, userId)
    }

    suspend fun vote(pollId: UUID, optionId: UUID, userId: UUID): Boolean {
        val poll = postRepository.findPollById(pollId) ?: throw IllegalArgumentException("Poll not found")
        
        // Validate expiry
        if (poll.expiresAt != null) {
            val expiry = Instant.parse(poll.expiresAt)
            if (Instant.now().isAfter(expiry)) {
                throw IllegalArgumentException("This poll has already expired")
            }
        }

        // Validate option belongs to poll
        val validOption = poll.options.any { it.id == optionId.toString() }
        if (!validOption) {
            throw IllegalArgumentException("Invalid option for this poll")
        }

        return postRepository.registerVote(UUID.randomUUID(), pollId, optionId, userId)
    }
}

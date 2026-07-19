package com.decisoes.modules.comments.services

import com.decisoes.modules.comments.models.CommentDto
import com.decisoes.modules.comments.models.CreateCommentRequest
import com.decisoes.modules.comments.repositories.CommentRepository
import java.util.UUID

class CommentService(private val commentRepository: CommentRepository) {

    suspend fun getCommentsForPost(postId: UUID): List<CommentDto> {
        return commentRepository.findByPostId(postId)
    }

    suspend fun addComment(postId: UUID, authorId: UUID, req: CreateCommentRequest): CommentDto {
        val parentId = req.parentCommentId?.let { UUID.fromString(it) }
        
        // Validate parent comment if specified
        if (parentId != null) {
            val parent = commentRepository.findById(parentId) ?: throw IllegalArgumentException("Parent comment not found")
            require(parent.postId == postId.toString()) { "Parent comment does not belong to this post" }
        }

        return commentRepository.createComment(
            id = UUID.randomUUID(),
            postId = postId,
            authorId = authorId,
            parentCommentId = parentId,
            content = req.content
        )
    }
}

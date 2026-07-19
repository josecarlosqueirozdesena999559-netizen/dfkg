package com.decisoes.modules.comments.repositories

import com.decisoes.modules.comments.models.CommentDto
import com.decisoes.shared.database.*
import com.decisoes.shared.database.DatabaseConnector.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class CommentRepository {

    private fun rowToCommentDto(row: ResultRow): CommentDto = CommentDto(
        id = row[CommentsTable.id].toString(),
        postId = row[CommentsTable.postId].toString(),
        authorId = row[CommentsTable.authorId].toString(),
        authorName = row[UserProfilesTable.displayName],
        authorUsername = "@${row[UsersTable.username]}",
        authorAvatarUrl = row[UserProfilesTable.profileImageUrl],
        parentCommentId = row[CommentsTable.parentCommentId]?.toString(),
        content = row[CommentsTable.content],
        likesCount = row[CommentsTable.likesCount],
        status = row[CommentsTable.status],
        createdAt = row[CommentsTable.createdAt].toString(),
        updatedAt = row[CommentsTable.updatedAt].toString()
    )

    private fun findByIdInternal(commentId: UUID): CommentDto? {
        return (CommentsTable innerJoin UsersTable innerJoin UserProfilesTable)
            .selectAll()
            .where { CommentsTable.id eq commentId }
            .map { rowToCommentDto(it) }
            .singleOrNull()
    }

    suspend fun findById(commentId: UUID): CommentDto? = dbQuery {
        findByIdInternal(commentId)
    }

    suspend fun findByPostId(postId: UUID): List<CommentDto> = dbQuery {
        (CommentsTable innerJoin UsersTable innerJoin UserProfilesTable)
            .selectAll()
            .where { CommentsTable.postId eq postId }
            .orderBy(CommentsTable.createdAt to SortOrder.ASC)
            .map { rowToCommentDto(it) }
    }

    suspend fun createComment(
        id: UUID,
        postId: UUID,
        authorId: UUID,
        parentCommentId: UUID?,
        content: String
    ): CommentDto = dbQuery {
        val inserted = CommentsTable.insert {
            it[CommentsTable.id] = id
            it[CommentsTable.postId] = postId
            it[CommentsTable.authorId] = authorId
            it[CommentsTable.parentCommentId] = parentCommentId
            it[CommentsTable.content] = content
        }

        require(inserted.insertedCount == 1) {
            "Failed to insert comment."
        }

        // Increment comments_count in PostsTable
        val updated = PostsTable.update({ PostsTable.id eq postId }) {
            with(SqlExpressionBuilder) {
                it[PostsTable.commentsCount] = PostsTable.commentsCount + 1
            }
        }

        require(updated == 1) {
            "Post not found while updating comments count."
        }

        findByIdInternal(id)
            ?: throw IllegalStateException("Comment inserted but could not be loaded.")
    }
}

package com.decisoes.modules.posts.repositories

import com.decisoes.modules.posts.models.PostDto
import com.decisoes.modules.posts.models.PollDto
import com.decisoes.modules.posts.models.PollOptionDto
import com.decisoes.shared.database.*
import com.decisoes.shared.database.DatabaseConnector.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import java.util.UUID
import java.time.Instant

class PostRepository {

    suspend fun findFeed(limit: Int, cursor: Instant?, viewerId: UUID? = null): List<PostDto> = dbQuery {
        var condition: Op<Boolean> = PostsTable.status eq "ACTIVE"
        if (cursor != null) {
            condition = condition and (PostsTable.createdAt less cursor)
        }
        
        val postRows = (PostsTable innerJoin UsersTable innerJoin UserProfilesTable)
            .selectAll()
            .where(condition)
            .orderBy(PostsTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .toList()

        if (postRows.isEmpty()) return@dbQuery emptyList()

        val postIds: List<UUID> = postRows.map { it[PostsTable.id] }
        
        val likedPostIds = if (viewerId != null) {
            PostLikesTable.selectAll()
                .where { (PostLikesTable.postId inList postIds) and (PostLikesTable.userId eq viewerId) }
                .map { it[PostLikesTable.postId] }
                .toSet()
        } else emptySet()

        val pollPostIds: List<UUID> = postRows.filter { it[PostsTable.type] == "POLL" }.map { it[PostsTable.id] }
        
        val pollsByPostId = if (pollPostIds.isNotEmpty()) {
            val polls = PollsTable.selectAll()
                .where { PollsTable.postId inList pollPostIds }
                .toList()
                
            val pollIds: List<UUID> = polls.map { it[PollsTable.id] }
            val optionsByPollId = if (pollIds.isNotEmpty()) {
                PollOptionsTable.selectAll()
                    .where { PollOptionsTable.pollId inList pollIds }
                    .orderBy(PollOptionsTable.displayOrder to SortOrder.ASC)
                    .groupBy { it[PollOptionsTable.pollId] }
            } else emptyMap()

            polls.associateBy({ it[PollsTable.postId] }) { pollRow ->
                val pId = pollRow[PollsTable.id]
                val options = optionsByPollId[pId]?.map { optRow ->
                    PollOptionDto(
                        id = optRow[PollOptionsTable.id].toString(),
                        pollId = pId.toString(),
                        text = optRow[PollOptionsTable.text],
                        imageUrl = optRow[PollOptionsTable.imageUrl],
                        votesCount = optRow[PollOptionsTable.votesCount],
                        displayOrder = optRow[PollOptionsTable.displayOrder]
                    )
                } ?: emptyList()
                
                PollDto(
                    id = pId.toString(),
                    postId = pollRow[PollsTable.postId].toString(),
                    question = pollRow[PollsTable.question],
                    totalVotes = pollRow[PollsTable.totalVotes],
                    expiresAt = pollRow[PollsTable.expiresAt]?.toString(),
                    options = options
                )
            }
        } else emptyMap()

        postRows.map { postRow ->
            val postId = postRow[PostsTable.id]
            PostDto(
                id = postId.toString(),
                authorId = postRow[PostsTable.authorId].toString(),
                authorName = postRow[UserProfilesTable.displayName],
                authorUsername = "@" + postRow[UsersTable.username],
                authorAvatarUrl = postRow[UserProfilesTable.profileImageUrl],
                type = postRow[PostsTable.type],
                content = postRow[PostsTable.content],
                visibility = postRow[PostsTable.visibility],
                status = postRow[PostsTable.status],
                likesCount = postRow[PostsTable.likesCount],
                commentsCount = postRow[PostsTable.commentsCount],
                sharesCount = postRow[PostsTable.sharesCount],
                viewsCount = postRow[PostsTable.viewsCount],
                hasLiked = likedPostIds.contains(postId),
                poll = pollsByPostId[postId],
                createdAt = postRow[PostsTable.createdAt].toString(),
                updatedAt = postRow[PostsTable.updatedAt].toString()
            )
        }
    }

    suspend fun findById(postId: UUID, viewerId: UUID? = null): PostDto? = dbQuery {
        val postRow = (PostsTable innerJoin UsersTable innerJoin UserProfilesTable)
            .selectAll()
            .where { PostsTable.id eq postId }
            .singleOrNull() ?: return@dbQuery null

        val hasLiked = if (viewerId != null) {
            PostLikesTable.selectAll()
                .where { (PostLikesTable.postId eq postId) and (PostLikesTable.userId eq viewerId) }
                .count() > 0
        } else false

        val pollDto = if (postRow[PostsTable.type] == "POLL") {
            val pollRow = PollsTable.selectAll()
                .where { PollsTable.postId eq postId }
                .singleOrNull()

            if (pollRow != null) {
                val pollId = pollRow[PollsTable.id]
                val options = PollOptionsTable.selectAll()
                    .where { PollOptionsTable.pollId eq pollId }
                    .orderBy(PollOptionsTable.displayOrder to SortOrder.ASC)
                    .map { optRow ->
                        PollOptionDto(
                            id = optRow[PollOptionsTable.id].toString(),
                            pollId = pollId.toString(),
                            text = optRow[PollOptionsTable.text],
                            imageUrl = optRow[PollOptionsTable.imageUrl],
                            votesCount = optRow[PollOptionsTable.votesCount],
                            displayOrder = optRow[PollOptionsTable.displayOrder]
                        )
                    }

                PollDto(
                    id = pollId.toString(),
                    postId = postId.toString(),
                    question = pollRow[PollsTable.question],
                    totalVotes = pollRow[PollsTable.totalVotes],
                    expiresAt = pollRow[PollsTable.expiresAt]?.toString(),
                    options = options
                )
            } else null
        } else null

        PostDto(
            id = postRow[PostsTable.id].toString(),
            authorId = postRow[PostsTable.authorId].toString(),
            authorName = postRow[UserProfilesTable.displayName],
            authorUsername = "@${postRow[UsersTable.username]}",
            authorAvatarUrl = postRow[UserProfilesTable.profileImageUrl],
            type = postRow[PostsTable.type],
            content = postRow[PostsTable.content],
            visibility = postRow[PostsTable.visibility],
            status = postRow[PostsTable.status],
            likesCount = postRow[PostsTable.likesCount],
            commentsCount = postRow[PostsTable.commentsCount],
            sharesCount = postRow[PostsTable.sharesCount],
            viewsCount = postRow[PostsTable.viewsCount],
            hasLiked = hasLiked,
            poll = pollDto,
            createdAt = postRow[PostsTable.createdAt].toString(),
            updatedAt = postRow[PostsTable.updatedAt].toString()
        )
    }

    suspend fun createPost(
        id: UUID,
        authorId: UUID,
        type: String,
        content: String?,
        visibility: String
    ): PostDto = dbQuery {
        PostsTable.insert {
            it[PostsTable.id] = id
            it[PostsTable.authorId] = authorId
            it[PostsTable.type] = type
            it[PostsTable.content] = content
            it[PostsTable.visibility] = visibility
        }
        
        // Update user_profiles posts_count
        UserProfilesTable.update({ UserProfilesTable.userId eq authorId }) {
            with(SqlExpressionBuilder) {
                it[UserProfilesTable.postsCount] = UserProfilesTable.postsCount + 1
            }
        }
        
        // Call select to build standard DTO
        findById(id, authorId)!!
    }

    suspend fun deletePost(id: UUID, authorId: UUID): Boolean = dbQuery {
        val deleted = PostsTable.deleteWhere { (PostsTable.id eq id) and (PostsTable.authorId eq authorId) } > 0
        if (deleted) {
            UserProfilesTable.update({ UserProfilesTable.userId eq authorId }) {
                with(SqlExpressionBuilder) {
                    it[UserProfilesTable.postsCount] = Case()
                        .When(UserProfilesTable.postsCount greater 0L, UserProfilesTable.postsCount - 1)
                        .Else(longParam(0L))
                }
            }
        }
        deleted
    }

    suspend fun addLike(postId: UUID, userId: UUID): Boolean = dbQuery {
        val alreadyLiked = PostLikesTable.selectAll()
            .where { (PostLikesTable.postId eq postId) and (PostLikesTable.userId eq userId) }
            .count() > 0

        if (alreadyLiked) return@dbQuery false

        PostLikesTable.insert {
            it[PostLikesTable.postId] = postId
            it[PostLikesTable.userId] = userId
        }

        PostsTable.update({ PostsTable.id eq postId }) {
            with(SqlExpressionBuilder) {
                it[PostsTable.likesCount] = PostsTable.likesCount + 1
            }
        }
        true
    }

    suspend fun removeLike(postId: UUID, userId: UUID): Boolean = dbQuery {
        val deleted = PostLikesTable.deleteWhere { (PostLikesTable.postId eq postId) and (PostLikesTable.userId eq userId) } > 0

        if (deleted) {
            PostsTable.update({ PostsTable.id eq postId }) {
                with(SqlExpressionBuilder) {
                    it[PostsTable.likesCount] = Case()
                        .When(PostsTable.likesCount greater 0L, PostsTable.likesCount - 1)
                        .Else(longParam(0L))
                }
            }
        }
        deleted
    }

    suspend fun createPoll(
        id: UUID,
        postId: UUID,
        question: String,
        expiresAt: Instant?
    ): Unit = dbQuery {
        PollsTable.insert {
            it[PollsTable.id] = id
            it[PollsTable.postId] = postId
            it[PollsTable.question] = question
            it[PollsTable.expiresAt] = expiresAt
        }
    }

    suspend fun createPollOption(
        id: UUID,
        pollId: UUID,
        text: String,
        imageUrl: String?,
        displayOrder: Int
    ): Unit = dbQuery {
        PollOptionsTable.insert {
            it[PollOptionsTable.id] = id
            it[PollOptionsTable.pollId] = pollId
            it[PollOptionsTable.text] = text
            it[PollOptionsTable.imageUrl] = imageUrl
            it[PollOptionsTable.displayOrder] = displayOrder
        }
    }

    suspend fun registerVote(
        voteId: UUID,
        pollId: UUID,
        optionId: UUID,
        userId: UUID
    ): Boolean = dbQuery {
        val insertedCount = VotesTable.insertIgnore {
            it[VotesTable.id] = voteId
            it[VotesTable.pollId] = pollId
            it[VotesTable.optionId] = optionId
            it[VotesTable.userId] = userId
        }.insertedCount

        if (insertedCount == 0) return@dbQuery false

        PollOptionsTable.update({ PollOptionsTable.id eq optionId }) {
            with(SqlExpressionBuilder) {
                it[PollOptionsTable.votesCount] = PollOptionsTable.votesCount + 1
            }
        }

        PollsTable.update({ PollsTable.id eq pollId }) {
            with(SqlExpressionBuilder) {
                it[PollsTable.totalVotes] = PollsTable.totalVotes + 1
            }
        }
        true
    }

    suspend fun findPollById(pollId: UUID): PollDto? = dbQuery {
        val pollRow = PollsTable.selectAll()
            .where { PollsTable.id eq pollId }
            .singleOrNull() ?: return@dbQuery null

        val options = PollOptionsTable.selectAll()
            .where { PollOptionsTable.pollId eq pollId }
            .orderBy(PollOptionsTable.displayOrder to SortOrder.ASC)
            .map { optRow ->
                PollOptionDto(
                    id = optRow[PollOptionsTable.id].toString(),
                    pollId = pollId.toString(),
                    text = optRow[PollOptionsTable.text],
                    imageUrl = optRow[PollOptionsTable.imageUrl],
                    votesCount = optRow[PollOptionsTable.votesCount],
                    displayOrder = optRow[PollOptionsTable.displayOrder]
                )
            }

        PollDto(
            id = pollId.toString(),
            postId = pollRow[PollsTable.postId].toString(),
            question = pollRow[PollsTable.question],
            totalVotes = pollRow[PollsTable.totalVotes],
            expiresAt = pollRow[PollsTable.expiresAt]?.toString(),
            options = options
        )
    }
}

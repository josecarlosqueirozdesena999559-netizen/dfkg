package com.decisoes.shared.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import java.time.Instant

object UsersTable : Table("users") {
    val id = uuid("id")
    val firebaseUid = varchar("firebase_uid", 128).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val username = varchar("username", 30).uniqueIndex()
    val status = varchar("status", 30).default("ACTIVE")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
    val lastLoginAt = timestamp("last_login_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

object UserProfilesTable : Table("user_profiles") {
    val userId = uuid("user_id").references(UsersTable.id)
    val displayName = varchar("display_name", 100)
    val bio = varchar("bio", 500).nullable()
    val profileImageUrl = text("profile_image_url").nullable()
    val coverImageUrl = text("cover_image_url").nullable()
    val followersCount = long("followers_count").default(0L)
    val followingCount = long("following_count").default(0L)
    val postsCount = long("posts_count").default(0L)
    val verified = bool("verified").default(false)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(userId)
}

object PostsTable : Table("posts") {
    val id = uuid("id")
    val authorId = uuid("author_id").references(UsersTable.id)
    val type = varchar("type", 30) // TEXT, IMAGE, POLL, QUESTION
    val content = text("content").nullable()
    val visibility = varchar("visibility", 30).default("PUBLIC")
    val status = varchar("status", 30).default("ACTIVE")
    val likesCount = long("likes_count").default(0L)
    val commentsCount = long("comments_count").default(0L)
    val sharesCount = long("shares_count").default(0L)
    val viewsCount = long("views_count").default(0L)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, authorId)
        index(false, createdAt)
    }
}

object PollsTable : Table("polls") {
    val id = uuid("id")
    val postId = uuid("post_id").references(PostsTable.id).uniqueIndex()
    val question = text("question")
    val totalVotes = long("total_votes").default(0L)
    val expiresAt = timestamp("expires_at").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}

object PollOptionsTable : Table("poll_options") {
    val id = uuid("id")
    val pollId = uuid("poll_id").references(PollsTable.id)
    val text = varchar("text", 300)
    val imageUrl = text("image_url").nullable()
    val votesCount = long("votes_count").default(0L)
    val displayOrder = integer("display_order")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, pollId)
    }
}

object VotesTable : Table("votes") {
    val id = uuid("id")
    val pollId = uuid("poll_id").references(PollsTable.id)
    val optionId = uuid("option_id").references(PollOptionsTable.id)
    val userId = uuid("user_id").references(UsersTable.id)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex("uid_poll_unique", userId, pollId)
        index(false, optionId)
        index(false, userId)
    }

    override val primaryKey = PrimaryKey(id)
}

object PostLikesTable : Table("post_likes") {
    val postId = uuid("post_id").references(PostsTable.id)
    val userId = uuid("user_id").references(UsersTable.id)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(postId, userId)

    init {
        index(false, postId)
        index(false, userId)
    }
}

object CommentsTable : Table("comments") {
    val id = uuid("id")
    val postId = uuid("post_id").references(PostsTable.id)
    val authorId = uuid("author_id").references(UsersTable.id)
    val parentCommentId = uuid("parent_comment_id").references(id).nullable()
    val content = text("content")
    val likesCount = long("likes_count").default(0L)
    val status = varchar("status", 30).default("ACTIVE")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, postId)
        index(false, authorId)
    }
}

object FollowsTable : Table("follows") {
    val followerId = uuid("follower_id").references(UsersTable.id)
    val followingId = uuid("following_id").references(UsersTable.id)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(followerId, followingId)

    init {
        index(false, followerId)
        index(false, followingId)
    }
}

content = """package com.decisoes.modules.follows.repositories

import com.decisoes.shared.database.*
import com.decisoes.shared.database.DatabaseConnector.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import java.util.UUID
import com.decisoes.modules.users.models.UserProfileDto

class FollowRepository {
    suspend fun follow(followerId: UUID, followingId: UUID): Boolean = dbQuery {
        if (followerId == followingId) return@dbQuery false

        val alreadyFollowing = FollowsTable.selectAll()
            .where { (FollowsTable.followerId eq followerId) and (FollowsTable.followingId eq followingId) }
            .count() > 0

        if (alreadyFollowing) return@dbQuery false

        FollowsTable.insert {
            it[FollowsTable.followerId] = followerId
            it[FollowsTable.followingId] = followingId
        }

        // Update counts
        UserProfilesTable.update({ UserProfilesTable.userId eq followerId }) {
            with(SqlExpressionBuilder) {
                it[UserProfilesTable.followingCount] = UserProfilesTable.followingCount + 1
            }
        }
        UserProfilesTable.update({ UserProfilesTable.userId eq followingId }) {
            with(SqlExpressionBuilder) {
                it[UserProfilesTable.followersCount] = UserProfilesTable.followersCount + 1
            }
        }
        true
    }

    suspend fun unfollow(followerId: UUID, followingId: UUID): Boolean = dbQuery {
        val deleted = FollowsTable.deleteWhere { (FollowsTable.followerId eq followerId) and (FollowsTable.followingId eq followingId) } > 0
        if (deleted) {
            // Update counts
            UserProfilesTable.update({ UserProfilesTable.userId eq followerId }) {
                with(SqlExpressionBuilder) {
                    it[UserProfilesTable.followingCount] = Case()
                        .When(UserProfilesTable.followingCount greater 0L, UserProfilesTable.followingCount - 1)
                        .Else(longParam(0L))
                }
            }
            UserProfilesTable.update({ UserProfilesTable.userId eq followingId }) {
                with(SqlExpressionBuilder) {
                    it[UserProfilesTable.followersCount] = Case()
                        .When(UserProfilesTable.followersCount greater 0L, UserProfilesTable.followersCount - 1)
                        .Else(longParam(0L))
                }
            }
        }
        deleted
    }

    suspend fun getFollowers(userId: UUID): List<UserProfileDto> = dbQuery {
        (FollowsTable innerJoin UserProfilesTable on (FollowsTable.followerId eq UserProfilesTable.userId))
            .selectAll()
            .where { FollowsTable.followingId eq userId }
            .orderBy(FollowsTable.createdAt to SortOrder.DESC)
            .map { row ->
                UserProfileDto(
                    userId = row[UserProfilesTable.userId].toString(),
                    displayName = row[UserProfilesTable.displayName],
                    bio = row[UserProfilesTable.bio],
                    profileImageUrl = row[UserProfilesTable.profileImageUrl],
                    coverImageUrl = row[UserProfilesTable.coverImageUrl],
                    followersCount = row[UserProfilesTable.followersCount],
                    followingCount = row[UserProfilesTable.followingCount],
                    postsCount = row[UserProfilesTable.postsCount],
                    verified = row[UserProfilesTable.verified],
                    createdAt = row[UserProfilesTable.createdAt].toString(),
                    updatedAt = row[UserProfilesTable.updatedAt].toString()
                )
            }
    }

    suspend fun getFollowing(userId: UUID): List<UserProfileDto> = dbQuery {
        (FollowsTable innerJoin UserProfilesTable on (FollowsTable.followingId eq UserProfilesTable.userId))
            .selectAll()
            .where { FollowsTable.followerId eq userId }
            .orderBy(FollowsTable.createdAt to SortOrder.DESC)
            .map { row ->
                UserProfileDto(
                    userId = row[UserProfilesTable.userId].toString(),
                    displayName = row[UserProfilesTable.displayName],
                    bio = row[UserProfilesTable.bio],
                    profileImageUrl = row[UserProfilesTable.profileImageUrl],
                    coverImageUrl = row[UserProfilesTable.coverImageUrl],
                    followersCount = row[UserProfilesTable.followersCount],
                    followingCount = row[UserProfilesTable.followingCount],
                    postsCount = row[UserProfilesTable.postsCount],
                    verified = row[UserProfilesTable.verified],
                    createdAt = row[UserProfilesTable.createdAt].toString(),
                    updatedAt = row[UserProfilesTable.updatedAt].toString()
                )
            }
    }
}
"""

with open('backend/modules/follows/repositories/FollowRepository.kt', 'w') as f:
    f.write(content)


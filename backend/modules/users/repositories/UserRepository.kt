package com.decisoes.modules.users.repositories

import com.decisoes.modules.users.models.UserDto
import com.decisoes.modules.users.models.UserProfileDto
import com.decisoes.shared.database.DatabaseConnector.dbQuery
import com.decisoes.shared.database.UsersTable
import com.decisoes.shared.database.UserProfilesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID
import java.time.Instant

class UserRepository {

    private fun ResultRow.toUserDto(): UserDto = UserDto(
        id = this[UsersTable.id].toString(),
        firebaseUid = this[UsersTable.firebaseUid],
        email = this[UsersTable.email],
        username = this[UsersTable.username],
        status = this[UsersTable.status],
        createdAt = this[UsersTable.createdAt].toString(),
        updatedAt = this[UsersTable.updatedAt].toString(),
        lastLoginAt = this[UsersTable.lastLoginAt]?.toString()
    )

    private fun ResultRow.toUserProfileDto(): UserProfileDto = UserProfileDto(
        userId = this[UserProfilesTable.userId].toString(),
        displayName = this[UserProfilesTable.displayName],
        bio = this[UserProfilesTable.bio],
        profileImageUrl = this[UserProfilesTable.profileImageUrl],
        coverImageUrl = this[UserProfilesTable.coverImageUrl],
        followersCount = this[UserProfilesTable.followersCount],
        followingCount = this[UserProfilesTable.followingCount],
        postsCount = this[UserProfilesTable.postsCount],
        verified = this[UserProfilesTable.verified],
        createdAt = this[UserProfilesTable.createdAt].toString(),
        updatedAt = this[UserProfilesTable.updatedAt].toString()
    )

    suspend fun findByFirebaseUid(firebaseUid: String): UserDto? = dbQuery {
        UsersTable.selectAll().where { UsersTable.firebaseUid eq firebaseUid }
            .map { it.toUserDto() }
            .singleOrNull()
    }

    suspend fun findById(id: UUID): UserDto? = dbQuery {
        UsersTable.selectAll().where { UsersTable.id eq id }
            .map { it.toUserDto() }
            .singleOrNull()
    }

    suspend fun findByUsername(username: String): UserDto? = dbQuery {
        val cleanUsername = username.removePrefix("@").lowercase().trim()
        UsersTable.selectAll().where { UsersTable.username eq cleanUsername }
            .map { it.toUserDto() }
            .singleOrNull()
    }

    suspend fun findProfileByUserId(userId: UUID): UserProfileDto? = dbQuery {
        UserProfilesTable.selectAll().where { UserProfilesTable.userId eq userId }
            .map { it.toUserProfileDto() }
            .singleOrNull()
    }

    suspend fun createUser(id: UUID, firebaseUid: String, email: String, username: String): UserDto = dbQuery {
        UsersTable.insert {
            it[UsersTable.id] = id
            it[UsersTable.firebaseUid] = firebaseUid
            it[UsersTable.email] = email
            it[UsersTable.username] = username.lowercase().trim()
            it[UsersTable.status] = "ACTIVE"
        }
        UsersTable.selectAll().where { UsersTable.id eq id }.map { it.toUserDto() }.single()
    }

    suspend fun createUserProfile(userId: UUID, displayName: String): UserProfileDto = dbQuery {
        UserProfilesTable.insert {
            it[UserProfilesTable.userId] = userId
            it[UserProfilesTable.displayName] = displayName
            it[UserProfilesTable.bio] = null
            it[UserProfilesTable.profileImageUrl] = null
            it[UserProfilesTable.coverImageUrl] = null
        }
        UserProfilesTable.selectAll().where { UserProfilesTable.userId eq userId }.map { it.toUserProfileDto() }.single()
    }

    suspend fun updateUserProfile(
        userId: UUID,
        displayName: String?,
        bio: String?,
        profileImageUrl: String?,
        coverImageUrl: String?
    ): UserProfileDto = dbQuery {
        UserProfilesTable.update({ UserProfilesTable.userId eq userId }) {
            if (displayName != null) it[UserProfilesTable.displayName] = displayName
            if (bio != null) it[UserProfilesTable.bio] = bio
            if (profileImageUrl != null) it[UserProfilesTable.profileImageUrl] = profileImageUrl
            if (coverImageUrl != null) it[UserProfilesTable.coverImageUrl] = coverImageUrl
            it[UserProfilesTable.updatedAt] = Instant.now()
        }
        UserProfilesTable.selectAll().where { UserProfilesTable.userId eq userId }.map { it.toUserProfileDto() }.single()
    }

    suspend fun updateLastLogin(userId: UUID) = dbQuery {
        UsersTable.update({ UsersTable.id eq userId }) {
            it[UsersTable.lastLoginAt] = Instant.now()
        }
    }
}

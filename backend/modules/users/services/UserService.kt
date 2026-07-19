package com.decisoes.modules.users.services

import com.decisoes.modules.users.models.UserDto
import com.decisoes.modules.users.models.UserProfileDto
import com.decisoes.modules.users.models.MeProfileDto
import com.decisoes.modules.users.models.UpdateProfileRequest
import com.decisoes.modules.users.repositories.UserRepository
import com.decisoes.shared.security.FirebaseDecodedToken
import com.decisoes.shared.security.FirebaseUserPrincipal
import java.util.UUID
import org.slf4j.LoggerFactory

class UserService(private val userRepository: UserRepository) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    suspend fun getOrCreateUserFromFirebase(token: FirebaseDecodedToken): FirebaseUserPrincipal? {
        val existingUser = userRepository.findByFirebaseUid(token.uid)
        if (existingUser != null) {
            if (existingUser.status != "ACTIVE") {
                logger.warn("User with firebase_uid: {} has status: {} and is blocked from login.", token.uid, existingUser.status)
                return null
            }
            val userId = UUID.fromString(existingUser.id)
            userRepository.updateLastLogin(userId)
            return FirebaseUserPrincipal(
                id = userId,
                firebaseUid = existingUser.firebaseUid,
                email = existingUser.email,
                username = existingUser.username
            )
        }

        // Create new user flow
        val userId = UUID.randomUUID()
        
        // Generate unique username
        var username = token.email.substringBefore("@").lowercase().trim()
        if (username.isEmpty() || username.length < 3) {
            username = "user_" + token.uid.take(6)
        }
        
        // Remove characters that aren't letters, digits, or underscore
        username = username.filter { it.isLetterOrDigit() || it == '_' }
        
        // Check collision and adjust if necessary
        var finalUsername = username
        var attempt = 1
        while (userRepository.findByUsername(finalUsername) != null) {
            finalUsername = "${username}_${attempt++}"
        }

        val displayName = token.name.ifBlank { finalUsername.replaceFirstChar { it.uppercase() } }

        logger.info("Registering first-time Firebase user: UID={}, Email={}, Assigned Username={}, DisplayName={}", token.uid, token.email, finalUsername, displayName)

        val newUser = userRepository.createUser(userId, token.uid, token.email, finalUsername)
        userRepository.createUserProfile(userId, displayName)

        return FirebaseUserPrincipal(
            id = userId,
            firebaseUid = newUser.firebaseUid,
            email = newUser.email,
            username = newUser.username
        )
    }

    suspend fun getProfileByUsername(username: String): MeProfileDto? {
        val user = userRepository.findByUsername(username) ?: return null
        val profile = userRepository.findProfileByUserId(UUID.fromString(user.id)) ?: return null
        return MeProfileDto(user, profile)
    }

    suspend fun getProfileByUserId(userId: UUID): MeProfileDto? {
        val user = userRepository.findById(userId) ?: return null
        val profile = userRepository.findProfileByUserId(userId) ?: return null
        return MeProfileDto(user, profile)
    }

    suspend fun updateProfile(userId: UUID, req: UpdateProfileRequest): UserProfileDto {
        return userRepository.updateUserProfile(
            userId = userId,
            displayName = req.displayName,
            bio = req.bio,
            profileImageUrl = req.profileImageUrl,
            coverImageUrl = req.coverImageUrl
        )
    }
}

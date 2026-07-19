package com.example.repository

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    val currentUserEmail: String?
    val currentUserId: String?
    val isUserLoggedIn: Boolean

    suspend fun login(email: String, password: String): Result<String>
    suspend fun register(email: String, password: String, displayName: String, username: String): Result<String>
    suspend fun logout()
    suspend fun getCurrentIdToken(forceRefresh: Boolean = false): String?
}

/**
 * Real implementation of AuthRepository using Firebase Authentication SDK.
 * It is fully decoupled from the UI and contains checks to gracefully handle
 * cases where google-services.json has not been configured yet (i.e. avoids crashing).
 */
class FirebaseAuthRepositoryImpl : AuthRepository {
    private val TAG = "FirebaseAuthRepository"

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth is not available (google-services.json might be missing or invalid): ${e.message}")
            null
        }

    override val currentUserEmail: String?
        get() = auth?.currentUser?.email

    override val currentUserId: String?
        get() = auth?.currentUser?.uid

    override val isUserLoggedIn: Boolean
        get() = auth?.currentUser != null

    override suspend fun login(email: String, password: String): Result<String> {
        val firebaseAuth = auth ?: return Result.failure(
            IllegalStateException("Firebase Authentication is not configured. Please add the google-services.json file.")
        )
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user.uid)
            } else {
                Result.failure(Exception("Login succeeded but User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
        username: String
    ): Result<String> {
        val firebaseAuth = auth ?: return Result.failure(
            IllegalStateException("Firebase Authentication is not configured. Please add the google-services.json file.")
        )
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // We can update the profile displayName if desired, but we return the UID
                try {
                    val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                        this.displayName = displayName
                    }
                    user.updateProfile(profileUpdates).await()
                } catch (pe: Exception) {
                    Log.w(TAG, "Could not update user profile displayName: ${pe.message}")
                }
                Result.success(user.uid)
            } else {
                Result.failure(Exception("Registration succeeded but User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out from Firebase: ${e.message}")
        }
    }

    override suspend fun getCurrentIdToken(forceRefresh: Boolean): String? {
        val firebaseAuth = auth ?: return null
        return try {
            val user = firebaseAuth.currentUser ?: return null
            val tokenResult = user.getIdToken(forceRefresh).await()
            tokenResult.token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to obtain Firebase ID Token: ${e.message}")
            null
        }
    }
}

/**
 * Dev/Mock implementation of AuthRepository.
 * Continues to support USE_DEV_AUTH=true flow.
 */
class DevAuthRepositoryImpl : AuthRepository {
    private var mockEmail: String? = "marinasouza@example.com"
    private var mockUid: String? = "dev_uid_marina"
    private var isLoggedInState: Boolean = true

    override val currentUserEmail: String?
        get() = mockEmail

    override val currentUserId: String?
        get() = mockUid

    override val isUserLoggedIn: Boolean
        get() = isLoggedInState

    override suspend fun login(email: String, password: String): Result<String> {
        mockEmail = email
        mockUid = "dev_uid_${email.substringBefore("@")}"
        isLoggedInState = true
        return Result.success(mockUid!!)
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
        username: String
    ): Result<String> {
        mockEmail = email
        mockUid = "dev_uid_${username.replace("@", "")}"
        isLoggedInState = true
        return Result.success(mockUid!!)
    }

    override suspend fun logout() {
        mockEmail = null
        mockUid = null
        isLoggedInState = false
    }

    override suspend fun getCurrentIdToken(forceRefresh: Boolean): String? {
        return mockEmail?.substringBefore("@")?.let { "dev_token_$it" }
    }
}

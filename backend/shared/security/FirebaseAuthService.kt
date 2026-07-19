package com.decisoes.shared.security

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.File
import org.slf4j.LoggerFactory

object FirebaseAuthService {
    private val logger = LoggerFactory.getLogger(FirebaseAuthService::class.java)
    private var isInitialized = false

    fun initialize() {
        if (isInitialized) return

        try {
            val firebaseCredentialsPath = System.getenv("FIREBASE_CREDENTIALS_PATH")
            val firebaseCredentialsJson = System.getenv("FIREBASE_CREDENTIALS_JSON")

            val optionsBuilder = FirebaseOptions.builder()
            var hasCredentials = false

            when {
                !firebaseCredentialsJson.isNullOrBlank() -> {
                    logger.info("Initializing Firebase Admin SDK with JSON from environment variable")
                    optionsBuilder.setCredentials(GoogleCredentials.fromStream(ByteArrayInputStream(firebaseCredentialsJson.toByteArray())))
                    hasCredentials = true
                }
                !firebaseCredentialsPath.isNullOrBlank() && File(firebaseCredentialsPath).exists() -> {
                    logger.info("Initializing Firebase Admin SDK with credentials file from path: {}", firebaseCredentialsPath)
                    optionsBuilder.setCredentials(GoogleCredentials.fromStream(FileInputStream(firebaseCredentialsPath)))
                    hasCredentials = true
                }
                else -> {
                    logger.warn("Firebase credentials not found in env (FIREBASE_CREDENTIALS_JSON or FIREBASE_CREDENTIALS_PATH). Trying Application Default Credentials.")
                    try {
                        optionsBuilder.setCredentials(GoogleCredentials.getApplicationDefault())
                        hasCredentials = true
                    } catch (e: Exception) {
                        logger.error("Could not load Application Default Credentials. Firebase token validation will fail unless running with dev_token_ bypass mode.", e)
                    }
                }
            }

            if (hasCredentials) {
                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(optionsBuilder.build())
                }
                isInitialized = true
                logger.info("Firebase Admin SDK successfully initialized")
            } else {
                logger.warn("Firebase Admin SDK was NOT initialized because no valid credentials configuration was provided.")
            }
        } catch (e: Exception) {
            logger.error("Failed to initialize Firebase Admin SDK", e)
        }
    }

    fun verifyToken(idToken: String): FirebaseDecodedToken? {
        // DEV BYPASS for local seed data / development
        val devBypass = System.getenv("DEV_BYPASS_AUTH") == "true"
        if (devBypass && idToken.startsWith("dev_token_")) {
            val cleanToken = idToken.removePrefix("dev_token_")
            val dummyUid = if (cleanToken.isBlank()) "user1" else cleanToken
            val dummyEmail = "$dummyUid@example.com"
            val dummyUsername = dummyUid
            return FirebaseDecodedToken(
                uid = dummyUid,
                email = dummyEmail,
                name = dummyUid.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            )
        }

        if (!isInitialized) {
            initialize()
        }

        return try {
            val token: FirebaseToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
            FirebaseDecodedToken(
                uid = token.uid,
                email = token.email ?: "",
                name = token.name ?: ""
            )
        } catch (e: Exception) {
            logger.error("Error verifying Firebase ID token: ${e.message}")
            null
        }
    }
}

data class FirebaseDecodedToken(
    val uid: String,
    val email: String,
    val name: String
)

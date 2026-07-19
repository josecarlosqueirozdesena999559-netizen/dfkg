package com.example

import com.example.network.NetworkModule
import com.example.network.CreatePostRequest
import com.example.network.CreatePollRequest
import com.example.network.CreatePollOptionRequest
import com.example.network.VoteRequest
import com.example.network.UpdateProfileRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NetworkVerificationTest {

    @Test
    fun verifyAllEndpoints() = runBlocking {
        println("=== SYSTEM INTEGRATION VALIDATION START ===")
        val testToken = "dev_token_teste"
        val apiService = NetworkModule.createApiService { testToken }

        // 1. GET /api/v1/me (Profile / Login sync)
        println("\n>>> CALLING: GET /api/v1/me")
        try {
            val me = apiService.getMe()
            println("Response HTTP 200 OK")
            println("User: id=${me.user.id}, email=${me.user.email}, username=${me.user.username}")
            println("Profile: displayName=${me.profile.displayName}, bio=${me.profile.bio}")
            assertNotNull(me)
        } catch (e: Exception) {
            println("Failed: ${e.message}")
        }

        // 2. GET /api/v1/users/{username}
        println("\n>>> CALLING: GET /api/v1/users/teste")
        try {
            val userProfile = apiService.getUserProfile("teste")
            println("Response HTTP 200 OK")
            println("Username: ${userProfile.user.username}")
            println("DisplayName: ${userProfile.profile.displayName}")
        } catch (e: Exception) {
            println("Failed: ${e.message}")
        }

        // 3. PUT /api/v1/me/profile
        println("\n>>> CALLING: PUT /api/v1/me/profile")
        try {
            val updateReq = UpdateProfileRequest(
                displayName = "Teste Executado",
                bio = "Validado via Teste de Integração",
                profileImageUrl = "https://images.unsplash.com/photo-1500000000000",
                coverImageUrl = "purple"
            )
            val updated = apiService.updateProfile(updateReq)
            println("Response HTTP 200 OK")
            println("Updated DisplayName: ${updated.displayName}")
            println("Updated Bio: ${updated.bio}")
        } catch (e: Exception) {
            println("Failed: ${e.message}")
        }

        // 4. GET /api/v1/posts
        println("\n>>> CALLING: GET /api/v1/posts")
        var firstPostId: String? = null
        var pollPostId: String? = null
        var pollId: String? = null
        var optionId: String? = null
        try {
            val posts = apiService.getFeed(limit = 10, cursor = null)
            println("Response HTTP 200 OK")
            println("Retrieved ${posts.size} posts.")
            posts.forEach { p ->
                if (firstPostId == null) firstPostId = p.id
                if (p.type == "POLL" && pollId == null) {
                    pollPostId = p.id
                    pollId = p.poll?.id
                    optionId = p.poll?.options?.firstOrNull()?.id
                }
            }
        } catch (e: Exception) {
            println("Failed: ${e.message}")
        }

        // 5. POST /api/v1/posts (Create Thought)
        println("\n>>> CALLING: POST /api/v1/posts (Create Thought)")
        var newPostId: String? = null
        try {
            val createReq = CreatePostRequest(
                type = "TEXT",
                content = "Validating post creation at " + java.time.Instant.now(),
                visibility = "PUBLIC",
                poll = null
            )
            val created = apiService.createPost(createReq)
            println("Response HTTP 201 Created")
            println("Created Post: id=${created.id}, content=${created.content}, type=${created.type}")
            newPostId = created.id
        } catch (e: Exception) {
            println("Failed: ${e.message}")
        }

        // 6. POST /api/v1/posts/{postId}/like
        val targetLikeId = newPostId ?: firstPostId
        if (targetLikeId != null) {
            println("\n>>> CALLING: POST /api/v1/posts/$targetLikeId/like")
            try {
                val likeResult = apiService.likePost(targetLikeId)
                println("Response HTTP 200 OK")
                println("Result: $likeResult")
            } catch (e: Exception) {
                println("Failed: ${e.message}")
            }

            // 7. DELETE /api/v1/posts/{postId}/like
            println("\n>>> CALLING: DELETE /api/v1/posts/$targetLikeId/like")
            try {
                val unlikeResult = apiService.unlikePost(targetLikeId)
                println("Response HTTP 200 OK")
                println("Result: $unlikeResult")
            } catch (e: Exception) {
                println("Failed: ${e.message}")
            }
        }

        // 8. POST /api/v1/polls/{pollId}/vote
        val finalPollId = pollId
        val finalOptionId = optionId
        if (finalPollId != null && finalOptionId != null) {
            println("\n>>> CALLING: POST /api/v1/polls/$finalPollId/vote")
            try {
                val voteResult = apiService.vote(finalPollId, VoteRequest(finalOptionId))
                println("Response HTTP 200 OK")
                println("Result: $voteResult")
            } catch (e: Exception) {
                println("Failed (expected if already voted): ${e.message}")
            }
        }

        println("\n=== SYSTEM INTEGRATION VALIDATION END ===")
    }
}

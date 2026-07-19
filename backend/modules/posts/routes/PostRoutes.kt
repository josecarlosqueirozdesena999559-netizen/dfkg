package com.decisoes.modules.posts.routes

import com.decisoes.modules.posts.models.CreatePostRequest
import com.decisoes.modules.posts.models.VoteRequest
import com.decisoes.modules.posts.services.PostService
import com.decisoes.modules.users.services.UserService
import com.decisoes.shared.security.authenticateFirebase
import com.decisoes.shared.security.firebaseUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID
import java.time.Instant

fun Route.postRoutes(postService: PostService, userService: UserService) {
    route("/api/v1") {
        authenticateFirebase(userService) {
            // Get Feed (cursor-based pagination)
            get("/posts") {
                val currentUser = call.firebaseUser
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
                val cursorStr = call.request.queryParameters["cursor"]
                val cursor = cursorStr?.let {
                    try {
                        Instant.parse(it)
                    } catch (e: Exception) {
                        null
                    }
                }
                val feed = postService.getFeed(limit, cursor, currentUser.id)
                call.respond(HttpStatusCode.OK, feed)
            }

            // Create a Post
            post("/posts") {
                val currentUser = call.firebaseUser
                val req = call.receive<CreatePostRequest>()
                val newPost = postService.createPost(currentUser.id, req)
                call.respond(HttpStatusCode.Created, newPost)
            }

            // Get a single Post (authenticated so we can determine hasLiked status)
            get("/posts/{postId}") {
                val currentUser = call.firebaseUser
                val postIdStr = call.parameters["postId"] ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing postId"))
                val postId = UUID.fromString(postIdStr)
                
                val post = postService.getPost(postId, currentUser.id)
                if (post != null) {
                    call.respond(HttpStatusCode.OK, post)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Post not found"))
                }
            }

            // Delete a Post
            delete("/posts/{postId}") {
                val currentUser = call.firebaseUser
                val postIdStr = call.parameters["postId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing postId"))
                val postId = UUID.fromString(postIdStr)

                val success = postService.deletePost(postId, currentUser.id)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Post deleted successfully"))
                } else {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Post not found or you are not authorized to delete it"))
                }
            }

            // Like a Post
            post("/posts/{postId}/like") {
                val currentUser = call.firebaseUser
                val postIdStr = call.parameters["postId"] ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing postId"))
                val postId = UUID.fromString(postIdStr)

                val success = postService.likePost(postId, currentUser.id)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Post liked successfully"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Could not like post (already liked or not found)"))
                }
            }

            // Unlike a Post
            delete("/posts/{postId}/like") {
                val currentUser = call.firebaseUser
                val postIdStr = call.parameters["postId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing postId"))
                val postId = UUID.fromString(postIdStr)

                val success = postService.unlikePost(postId, currentUser.id)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Post unliked successfully"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Could not unlike post (not liked yet or not found)"))
                }
            }

            // Vote on a Poll
            post("/polls/{pollId}/vote") {
                val currentUser = call.firebaseUser
                val pollIdStr = call.parameters["pollId"] ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing pollId"))
                val pollId = UUID.fromString(pollIdStr)
                val req = call.receive<VoteRequest>()
                val optionId = UUID.fromString(req.optionId)

                val success = postService.vote(pollId, optionId, currentUser.id)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Vote registered successfully"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Could not register vote (already voted, poll expired, or database error)"))
                }
            }
        }
    }
}

package com.decisoes.modules.follows.routes

import com.decisoes.modules.follows.services.FollowService
import com.decisoes.modules.users.services.UserService
import com.decisoes.shared.security.authenticateFirebase
import com.decisoes.shared.security.firebaseUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.followRoutes(followService: FollowService, userService: UserService) {
    route("/api/v1/users/{userId}/follow") {
        authenticateFirebase(userService) {
            post {
                val currentUser = call.firebaseUser
                val userIdStr = call.parameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing userId"))
                val targetUserId = UUID.fromString(userIdStr)

                val success = followService.followUser(currentUser.id, targetUserId)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Successfully followed user"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Could not follow user (already following or invalid operation)"))
                }
            }

            delete {
                val currentUser = call.firebaseUser
                val userIdStr = call.parameters["userId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing userId"))
                val targetUserId = UUID.fromString(userIdStr)

                val success = followService.unfollowUser(currentUser.id, targetUserId)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Successfully unfollowed user"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Could not unfollow user (not following or invalid operation)"))
                }
            }
        }
    }

    route("/api/v1/users/{userId}/followers") {
        get {
            val userIdStr = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing userId"))
            val targetUserId = UUID.fromString(userIdStr)
            val followers = followService.getFollowers(targetUserId)
            call.respond(HttpStatusCode.OK, followers)
        }
    }

    route("/api/v1/users/{userId}/following") {
        get {
            val userIdStr = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing userId"))
            val targetUserId = UUID.fromString(userIdStr)
            val following = followService.getFollowing(targetUserId)
            call.respond(HttpStatusCode.OK, following)
        }
    }
}

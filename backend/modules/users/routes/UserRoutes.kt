package com.decisoes.modules.users.routes

import com.decisoes.modules.users.models.UpdateProfileRequest
import com.decisoes.modules.users.services.UserService
import com.decisoes.shared.security.authenticateFirebase
import com.decisoes.shared.security.firebaseUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userService: UserService) {
    route("/api/v1") {
        // Protected routes
        authenticateFirebase(userService) {
            get("/me") {
                val currentUser = call.firebaseUser
                val profile = userService.getProfileByUserId(currentUser.id)
                if (profile != null) {
                    call.respond(HttpStatusCode.OK, profile)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Profile not found"))
                }
            }

            put("/me/profile") {
                val currentUser = call.firebaseUser
                val request = call.receive<UpdateProfileRequest>()
                val updatedProfile = userService.updateProfile(currentUser.id, request)
                call.respond(HttpStatusCode.OK, updatedProfile)
            }
        }

        // Public/Authenticated routes
        get("/users/{username}") {
            val username = call.parameters["username"]
            if (username.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing username parameter"))
                return@get
            }
            
            val profile = userService.getProfileByUsername(username)
            if (profile != null) {
                call.respond(HttpStatusCode.OK, profile)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
            }
        }
    }
}

package com.decisoes.modules.comments.routes

import com.decisoes.modules.comments.models.CreateCommentRequest
import com.decisoes.modules.comments.services.CommentService
import com.decisoes.modules.users.services.UserService
import com.decisoes.shared.security.authenticateFirebase
import com.decisoes.shared.security.firebaseUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.commentRoutes(commentService: CommentService, userService: UserService) {
    route("/api/v1/posts/{postId}/comments") {
        authenticateFirebase(userService) {
            get {
                val postIdStr = call.parameters["postId"] ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing postId"))
                val postId = UUID.fromString(postIdStr)
                val comments = commentService.getCommentsForPost(postId)
                call.respond(HttpStatusCode.OK, comments)
            }

            post {
                val currentUser = call.firebaseUser
                val postIdStr = call.parameters["postId"] ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing postId"))
                val postId = UUID.fromString(postIdStr)
                val req = call.receive<CreateCommentRequest>()
                
                val comment = commentService.addComment(postId, currentUser.id, req)
                call.respond(HttpStatusCode.Created, comment)
            }
        }
    }
}

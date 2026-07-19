package com.decisoes.shared.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import java.util.UUID
import com.decisoes.modules.users.services.UserService

class FirebaseUserPrincipal(
    val id: UUID,
    val firebaseUid: String,
    val email: String,
    val username: String
)

val FirebaseUserKey = AttributeKey<FirebaseUserPrincipal>("FirebaseUserKey")

val ApplicationCall.firebaseUser: FirebaseUserPrincipal
    get() = attributes[FirebaseUserKey]

fun Route.authenticateFirebase(userService: UserService, buildProtected: Route.() -> Unit) {
    val authenticatedRoute = createChild(object : RouteSelector() {
        override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
            return RouteSelectorEvaluation.Constant
        }
    })

    val plugin = createRouteScopedPlugin("FirebaseAuthPlugin") {
        onCall { call ->
            val authHeader = call.request.header(HttpHeaders.Authorization)
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Missing or invalid Authorization header (Bearer token required)"))
                return@onCall
            }

            val tokenStr = authHeader.removePrefix("Bearer ")
            val decodedToken = FirebaseAuthService.verifyToken(tokenStr)
            if (decodedToken == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or expired Firebase ID token"))
                return@onCall
            }

            // Get or create user from DB
            val principal = userService.getOrCreateUserFromFirebase(decodedToken)
            if (principal == null) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "User account is suspended, banned, or deleted"))
                return@onCall
            }

            call.attributes.put(FirebaseUserKey, principal)
        }
    }

    authenticatedRoute.install(plugin)
    authenticatedRoute.buildProtected()
}

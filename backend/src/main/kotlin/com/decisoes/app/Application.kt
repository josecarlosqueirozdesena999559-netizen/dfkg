package com.decisoes.app

import com.decisoes.modules.comments.repositories.CommentRepository
import com.decisoes.modules.comments.services.CommentService
import com.decisoes.modules.comments.routes.commentRoutes
import com.decisoes.modules.follows.repositories.FollowRepository
import com.decisoes.modules.follows.services.FollowService
import com.decisoes.modules.follows.routes.followRoutes
import com.decisoes.modules.posts.repositories.PostRepository
import com.decisoes.modules.posts.services.PostService
import com.decisoes.modules.posts.routes.postRoutes
import com.decisoes.modules.users.repositories.UserRepository
import com.decisoes.modules.users.services.UserService
import com.decisoes.modules.users.routes.userRoutes
import com.decisoes.shared.database.DatabaseConnector
import com.decisoes.shared.database.DatabaseSeeder
import com.decisoes.shared.errors.GlobalExceptionHandler
import com.decisoes.shared.security.FirebaseAuthService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

fun main() {
    val port = 8080
    val host = System.getenv("HOST") ?: "0.0.0.0"
    
    embeddedServer(Netty, port = port, host = host, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")
    logger.info("Starting Decisões Social Network Backend API...")

    // 1. Initialize Database
    DatabaseConnector.init()

    // 2. Initialize Firebase SDK
    FirebaseAuthService.initialize()

    // 3. Run Seeder
    logger.info("Running database seeder check...")
    kotlinx.coroutines.runBlocking {
        DatabaseSeeder.seedIfNeeded()
    }

    // 4. Content Negotiation
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }

    // 5. CORS configuration
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost() // CORS open and ready for remote frontend connection
    }

    // 6. Global Error Handler
    install(StatusPages) {
        GlobalExceptionHandler.configure(this)
    }

    // 7. Initialize Repositories and Services
    val userRepository = UserRepository()
    val userService = UserService(userRepository)

    val postRepository = PostRepository()
    val postService = PostService(postRepository)

    val commentRepository = CommentRepository()
    val commentService = CommentService(commentRepository)

    val followRepository = FollowRepository()
    val followService = FollowService(followRepository)

    // 8. Register Routes
    routing {
        // Health Check GET /health
        get("/health") {
            val dbHealthy = DatabaseConnector.checkHealth()
            if (dbHealthy) {
                call.respond(HttpStatusCode.OK, mapOf("status" to "UP", "database" to "UP"))
            } else {
                call.respond(HttpStatusCode.ServiceUnavailable, mapOf("status" to "DOWN", "database" to "DOWN"))
            }
        }

        // Module routes
        userRoutes(userService)
        postRoutes(postService, userService)
        commentRoutes(commentService, userService)
        followRoutes(followService, userService)
    }
}

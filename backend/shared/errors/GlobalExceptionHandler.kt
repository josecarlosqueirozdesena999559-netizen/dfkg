package com.decisoes.shared.errors

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

object GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    fun configure(config: StatusPagesConfig) {
        config.exception<Throwable> { call, cause ->
            logger.error("Unhandled exception: ", cause)
            val isProduction = System.getenv("KTOR_ENV") == "production"
            
            val errorMessage = if (isProduction) {
                "An unexpected internal server error occurred."
            } else {
                cause.stackTraceToString()
            }

            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to errorMessage)
            )
        }

        config.exception<IllegalArgumentException> { call, cause ->
            logger.warn("Validation error: {}", cause.message)
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to (cause.message ?: "Invalid request parameters"))
            )
        }
    }
}

package ru.earl.feature.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.earl.feature.auth.AuthController
import ru.earl.security.hashing.SHA256HashingService
import ru.earl.security.token.JwtTokenService
import ru.earl.security.token.TokenConfig

fun Application.configureAuthenticationRouting(config: TokenConfig) {

    val authController = AuthController(SHA256HashingService(), JwtTokenService(), config)

    routing {
        post("/register") {
            authController.register(call)
        }
        post("/login") {
            authController.login(call)
        }
        authenticate {
            get("/authenticate") {
                call.respond(HttpStatusCode.OK, "success")
            }
            get("/getSecretInfo") {
                authController.getSecretInfo(call)
            }
        }
    }
}
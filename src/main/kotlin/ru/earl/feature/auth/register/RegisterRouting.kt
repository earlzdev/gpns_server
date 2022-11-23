package ru.earl.feature.auth.register

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.earl.security.hashing.SHA256HashingService
import ru.earl.security.token.JwtTokenService
import ru.earl.security.token.TokenConfig

fun Application.configureRegisterRouting(config: TokenConfig) {

    val registerController = RegisterController(SHA256HashingService(), JwtTokenService(), config)

    routing {
        post("/register") {
            registerController.register(call)
        }
        post("/login") {
            registerController.login(call)
        }
        authenticate {
            get("/authenticate") {
                call.respond(HttpStatusCode.OK, "success")
            }
            get("/getSecretInfo") {
                registerController.getSecretInfo(call)
            }
        }
    }
}
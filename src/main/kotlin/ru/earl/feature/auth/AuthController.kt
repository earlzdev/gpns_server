package ru.earl.feature.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import ru.earl.models.users.User
import ru.earl.models.users.UserDto
import ru.earl.security.hashing.HashingService
import ru.earl.security.hashing.SaltedHash
import ru.earl.security.token.TokenClaim
import ru.earl.security.token.TokenConfig
import ru.earl.security.token.TokenService
import java.util.*

class AuthController(
    private val hashingService: HashingService,
    private val tokenService: TokenService,
    private val tokenConfig: TokenConfig,
) {

    suspend fun register(call: ApplicationCall) {
        val request = call.receive<RegisterRequest>()
        println("request $request")
        try {
            val saltedHash = hashingService.generateSaltedHash(request.password)
            val user = UserDto(
                request.email,
                request.username,
                saltedHash.hash,
                saltedHash.salt,
                UUID.nameUUIDFromBytes(request.email.toByteArray()).toString()
            )
            User.insert(user)
            call.respond(HttpStatusCode.OK, "success")
        } catch (e: Exception) {
            println("error $e")
            call.respond(HttpStatusCode.InternalServerError, "$e")
        }
    }

    suspend fun login(call: ApplicationCall) {
        val request = call.receive<LoginRequest>()
        println("request ligin $request")
        val user = User.fetchUser(request.email)
        println("login fetchuser -> $user")
        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user?.password!!,
                salt = user.userSalt
            )
        )
        println("login isValidPassword  $isValidPassword")
        println("value -> ${request.password} + salt -> ${user.userSalt} == hash -> ${user.password}")
        println("login saltedHash  ${SaltedHash(
            hash = user.password,
            salt = user.userSalt
        )}")
        if (isValidPassword) {
            val token = tokenService.generate(
                config = tokenConfig,
                TokenClaim(
                    name = "userId",
                    value = user.userId
                )
            )
            println("login token  $token")
            call.respond(
                status = HttpStatusCode.OK,
                message = AuthResponses(
                    token = token
                )
            )
        }
    }

    suspend fun getSecretInfo(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        println("principal -> $principal")
        val userId = principal?.getClaim("userId", String::class)
        println("userId -> $userId")
        call.respond(HttpStatusCode.OK, TokenResponse(userId!!))
    }
}
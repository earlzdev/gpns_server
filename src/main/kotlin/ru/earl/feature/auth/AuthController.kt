package ru.earl.feature.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import ru.earl.models.group.Groups
import ru.earl.models.group.GroupsDto
import ru.earl.models.groupUsers.GroupUsers
import ru.earl.models.group_occupancy.GroupOccupancy
import ru.earl.models.userDetails.UserDetails
import ru.earl.models.userDetails.UserDetailsDto
import ru.earl.models.users.User
import ru.earl.models.users.UserDto
import ru.earl.models.usersOnline.UsersOnline
import ru.earl.models.usersOnline.UsersOnlineDto
import ru.earl.security.hashing.HashingService
import ru.earl.security.hashing.SaltedHash
import ru.earl.security.token.TokenClaim
import ru.earl.security.token.TokenConfig
import ru.earl.security.token.TokenService
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AuthController(
    private val hashingService: HashingService,
    private val tokenService: TokenService,
    private val tokenConfig: TokenConfig,
) {

    suspend fun register(call: ApplicationCall) {
        insertCommonGroup(call)
        val request = call.receive<RegisterRequest>()
        val userByEmail = User.fetchUserByEmail(request.email)
        val userByName = User.fetchUserByUsername(request.username)
        if (userByEmail != null) {
            call.respond(HttpStatusCode(409, "User with this email is already exists"))
        } else if (userByName != null) {
            call.respond(HttpStatusCode(409, "User with this name is already exists"))
        } else {
            val currentDate = Date()
            val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val dateText = dateFormat.format(currentDate)
            try {
                val userId = UUID.nameUUIDFromBytes(request.email.toByteArray()).toString()
                val saltedHash = hashingService.generateSaltedHash(request.password)
                val user = UserDto(
                    request.email,
                    request.username,
                    saltedHash.hash,
                    saltedHash.salt,
                    userId
                )
                val userDetails = UserDetailsDto(
                    userId,
                    "",
                    request.username,
                    1,
                    dateText,
                    ""
                )
                User.insert(user)
                UserDetails.insertUserDetails(userDetails)
                UsersOnline.insertNewUser(
                    UsersOnlineDto(
                        userId,
                        dateText,
                        1
                    )
                )
                UsersOnline.setUserOnline(userId)
                GroupUsers.insertNewUserForCommonGroup(userId, COMMON_GROUP_ID)
                call.respond(HttpStatusCode.OK, "success")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "$e")
            }
        }
    }

    suspend fun login(call: ApplicationCall) {
        val request = call.receive<LoginRequest>()
        val userByEmail = User.fetchUserByEmail(request.email)
        if (userByEmail != null) {
            val user = User.fetchUserByEmail(request.email)
            UsersOnline.setUserOnline(user?.userId!!)
            val isValidPassword = hashingService.verify(
                value = request.password,
                saltedHash = SaltedHash(
                    hash = user.password,
                    salt = user.userSalt
                )
            )
            if (isValidPassword) {
                val token = tokenService.generate(
                    config = tokenConfig,
                    TokenClaim(
                        name = "userId",
                        value = user.userId
                    )
                )
                call.respond(
                    status = HttpStatusCode.OK,
                    message = AuthResponses(
                        token = token
                    )
                )
            } else {
                call.respond(HttpStatusCode(409, "wrongPassword"))
            }
        } else {
            call.respond(HttpStatusCode(409, "noUserWithSuchEmail"))
        }
    }

    private fun insertCommonGroup(call: ApplicationCall) {
        if (!Groups.checkCommonGroupAvailability()) {
            val commonGroup = GroupsDto(
                COMMON_GROUP_ID,
                "Общий чат",
                "",
                "",
                "",
                "",
                "",
                0,
                0,
                0
            )
            Groups.insertNewGroup(commonGroup)
            GroupOccupancy.insertNewGroupOccupancy(COMMON_GROUP_ID)
        }
    }

    suspend fun getSecretInfo(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        println("principal -> $principal")
        val userId = principal?.getClaim("userId", String::class)
        println("userId -> $userId")
        call.respond(HttpStatusCode.OK, TokenResponse(userId!!))
    }

    companion object {
        private const val COMMON_GROUP_ID = "common"
    }
}
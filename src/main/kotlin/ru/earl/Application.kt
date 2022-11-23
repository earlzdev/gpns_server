package ru.earl

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import ru.earl.feature.auth.register.configureRegisterRouting
import ru.earl.plugins.*
import ru.earl.security.token.TokenConfig

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {

    Database.connect("jdbc:postgresql://localhost:5432/gpns", "org.postgresql.Driver",
        "postgres", "postgres")

    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 365L * 1000L * 60L * 60L * 24L,
        secret = System.getenv("JWT_SECRET")
    )

    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureSecurity(tokenConfig)
    configureRouting()
    //jwt
    configureRegisterRouting(tokenConfig)
}

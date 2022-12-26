package ru.earl.feature.search

import io.ktor.websocket.*

data class SearchingMember(
    val userId: String,
    val username: String,
    val socket: WebSocketSession
)

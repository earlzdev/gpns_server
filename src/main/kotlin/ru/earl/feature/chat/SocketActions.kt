package ru.earl.feature.chat

enum class SocketActions {
    REMOVE_DELETED_BY_ANOTHER_USER_ROOM, NEW_ROOM, UPDATE_LAST_MESSAGE_IN_ROOM, UPDATE_LAST_MESSAGE_READ_STATE, UPDATE_USER_ONLINE_IN_ROOM,
    UPDATE_USER_TYPING_MESSAGE_STATE, UPDATE_USER_ONLINE_STATUS_IN_CHAT, MARK_MESSAGE_AS_READ_IN_CHAT, NEW_MESSAGE
}
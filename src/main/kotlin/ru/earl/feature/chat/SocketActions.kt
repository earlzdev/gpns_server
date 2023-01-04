package ru.earl.feature.chat

enum class SocketActions {
    REMOVE_DELETED_BY_ANOTHER_USER_ROOM, NEW_ROOM, UPDATE_LAST_MESSAGE_IN_ROOM, UPDATE_LAST_MESSAGE_READ_STATE, UPDATE_USER_ONLINE_IN_ROOM,
    UPDATE_USER_TYPING_MESSAGE_STATE, UPDATE_USER_ONLINE_STATUS_IN_CHAT, MARK_MESSAGE_AS_READ_IN_CHAT, NEW_MESSAGE, NEW_MESSAGE_IN_GROUP, NEW_UPDATABLE_MESSAGE_IN_GROUP,
    UPDATE_TYPING_MESSAGE_STATUS_IN_GROUP, MARK_MESSAGES_AS_READ_IN_GROUP, MARK_AUTHORED_MESSAGES_AS_READ_IN_GROUP,
    NEW_SEARCHING_FORM, NEW_NOTIFICATION, REMOVE_DELETED_FORM, DRIVER_DELETED_FORM_NOTIFICATION, NEW_GROUP, REMOVE_DELETED_GROUP,
    COMPANION_LEAVED_GROUP
}
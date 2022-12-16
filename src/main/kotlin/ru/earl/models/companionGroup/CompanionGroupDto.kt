package ru.earl.models.companionGroup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompanionGroupDto (
    @SerialName("compId") val compId: String,
    @SerialName("lastMsgText") val lastMsgText: String,
    @SerialName("lastMsgTimestamp") val lastMsgTimestamp: String,
    @SerialName("lastMsgAuthor") val lastMsgAuthor: String,
    @SerialName("lastMsgAuthorImage") val lastMsgAuthorImage: String,
)
package ru.earl.feature.search

import io.ktor.server.application.*
import io.ktor.websocket.*

class SearchController(
    private val searchFormsServiceImpl: SearchFormsServiceImpl
) : SearchFormsService {

    override suspend fun fetchAllForms(call: ApplicationCall) {
        searchFormsServiceImpl.fetchAllForms(call)
    }

    override suspend fun insertNewCompanionForm(call: ApplicationCall) {
        searchFormsServiceImpl.insertNewCompanionForm(call)
    }

    override suspend fun insertNewDriverForm(call: ApplicationCall) {
        searchFormsServiceImpl.insertNewDriverForm(call)
    }

    override suspend fun deleteCompanionForm(call: ApplicationCall) {
        searchFormsServiceImpl.deleteCompanionForm(call)
    }

    override suspend fun deleteDriverForm(call: ApplicationCall) {
        searchFormsServiceImpl.deleteDriverForm(call)
    }

    override suspend fun inviteCompanion(call: ApplicationCall) {
        searchFormsServiceImpl.inviteCompanion(call)
    }

    override suspend fun answerTripInvitation(call: ApplicationCall) {
        searchFormsServiceImpl.answerTripInvitation(call)
    }

    override suspend fun inviteDriver(call: ApplicationCall) {
        searchFormsServiceImpl.inviteDriver(call)
    }

    override suspend fun initSearchingWebSocket(call: ApplicationCall, socket: WebSocketSession) {
        searchFormsServiceImpl.initSearchingWebSocket(call, socket)
    }

    override suspend fun closeSearchingWebSocket(call: ApplicationCall) {
        searchFormsServiceImpl.closeSearchingWebSocket(call)
    }

    override suspend fun fetchAllNotificationsForUser(call: ApplicationCall) {
        searchFormsServiceImpl.fetchAllNotificationsForUser(call)
    }

    override suspend fun fetchCompanionForm(call: ApplicationCall) {
        searchFormsServiceImpl.fetchCompanionForm(call)
    }

    override suspend fun fetchDriverForm(call: ApplicationCall) {
        searchFormsServiceImpl.fetchDriverForm(call)
    }

    override suspend fun acceptDriverToRideTogether(call: ApplicationCall) {
        searchFormsServiceImpl.acceptDriverToRideTogether(call)
    }

    override suspend fun denyDriverToRideTogether(call: ApplicationCall) {
        searchFormsServiceImpl.denyDriverToRideTogether(call)
    }

    override suspend fun acceptCompanionToRideTogether(call: ApplicationCall) {
        searchFormsServiceImpl.acceptCompanionToRideTogether(call)
    }

    override suspend fun denyCompanionToRideTogether(call: ApplicationCall) {
        searchFormsServiceImpl.denyCompanionToRideTogether(call)
    }
}
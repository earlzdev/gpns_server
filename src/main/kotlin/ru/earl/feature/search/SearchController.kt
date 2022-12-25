package ru.earl.feature.search

import io.ktor.server.application.*

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
}

/*
методы по серчу :
1 добавить новую форму
2 удалить форму
3 забронироваь место
4 фетч ол формс
 */
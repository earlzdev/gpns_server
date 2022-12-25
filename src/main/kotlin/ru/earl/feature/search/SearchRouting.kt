package ru.earl.feature.search

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureSearchRouting() {

    val controller = SearchController(
        SearchFormsServiceImpl()
    )

    routing {
        authenticate {
            post("/sendNewDriverForm") {
                controller.insertNewDriverForm(call)
            }
            post("/sendNewCompanionForm") {
                controller.insertNewCompanionForm(call)
            }
            get("/fetchAllCompForms") {
                controller.fetchAllForms(call)
            }
        }
    }
}

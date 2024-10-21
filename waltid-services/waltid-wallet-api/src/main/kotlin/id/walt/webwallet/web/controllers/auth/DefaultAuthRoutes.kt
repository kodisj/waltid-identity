@file:OptIn(ExperimentalUuidApi::class)

package id.walt.webwallet.web.controllers.auth

import com.nimbusds.jose.JWSObject
import id.walt.commons.web.UnauthorizedException
import id.walt.webwallet.db.models.Account
import id.walt.webwallet.service.account.AccountsService
import id.walt.webwallet.web.WebBaseRoutes.webWalletRoute
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

const val defaultAuthPath = "auth"
val defaultAuthTags = listOf("Authentication")

fun Application.defaultAuthRoutes() = webWalletRoute {
    route(defaultAuthPath, { tags = defaultAuthTags }) {
        authenticate("auth-session", "auth-bearer", "auth-bearer-alternative") {
            get("user-info", {
                summary = "Return user ID if logged in"
                response {
                    HttpStatusCode.OK to {
                        body<Account>()
                    }
                }
            }) {
                getUsersSessionToken()?.run {
                    val jwsObject = JWSObject.parse(this)
                    val uuid =
                        Json.parseToJsonElement(jwsObject.payload.toString()).jsonObject["sub"]?.jsonPrimitive?.content.toString()
                    call.respond(AccountsService.get(Uuid.parse(uuid)))
                } ?: call.respond(HttpStatusCode.BadRequest)
            }
            get("session", { summary = "Return session ID if logged in" }) {
                val token = getUsersSessionToken() ?: throw UnauthorizedException("Invalid session")
                call.respond(mapOf("token" to mapOf("accessToken" to token)))
            }
        }
    }
    object : RegisterControllerBase() {}.routes("register")(this)
    object : LoginControllerBase() {}.routes("login")(this)
    object : LogoutControllerBase() {}.routes("logout")(this)
}

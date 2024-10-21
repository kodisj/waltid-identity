package id.walt.webwallet.web.controllers.auth.oidc

import id.walt.webwallet.service.WalletServiceManager.oidcConfig
import id.walt.webwallet.web.controllers.auth.LogoutControllerBase
import io.github.smiley4.ktorswaggerui.dsl.routes.OpenApiRoute
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

class OidcLogoutController : LogoutControllerBase() {
    override fun apiBuilder(): OpenApiRoute.() -> Unit = { description = "Logout via OIDC provider" }
    override suspend fun PipelineContext<Unit, ApplicationCall>.execute() {
        call.respondRedirect("${oidcConfig.logoutUrl}?post_logout_redirect_uri=${oidcConfig.publicBaseUrl}&client_id=${oidcConfig.clientId}")
    }
}
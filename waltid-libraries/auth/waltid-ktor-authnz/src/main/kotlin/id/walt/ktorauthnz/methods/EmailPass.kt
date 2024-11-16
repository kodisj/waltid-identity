package id.walt.ktorauthnz.methods

import id.walt.ktorauthnz.AuthContext
import id.walt.ktorauthnz.KtorAuthnzManager
import id.walt.ktorauthnz.accounts.identifiers.methods.AccountIdentifier
import id.walt.ktorauthnz.accounts.identifiers.methods.EmailIdentifier
import id.walt.ktorauthnz.exceptions.authCheck
import id.walt.ktorauthnz.methods.data.EmailPassStoredData
import id.walt.ktorauthnz.security.PasswordHash
import id.walt.ktorauthnz.security.PasswordHashing
import id.walt.ktorauthnz.sessions.AuthSession
import id.walt.ktorauthnz.sessions.AuthSessionInformation
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("email")
data class EmailPassMethodInstance(
    override val data: EmailPassStoredData,
) : MethodInstance {
    override val config = null

    override fun authMethod() = EmailPass
}

object EmailPass : UserPassBasedAuthMethod("email", usernameName = "email") {

    override val relatedAuthMethodStoredData = EmailPassStoredData::class

    override suspend fun auth(session: AuthSession, credential: UserPasswordCredential, context: ApplicationCall): AccountIdentifier {
        val identifier = EmailIdentifier(credential.name)

        val storedData: EmailPassStoredData = lookupAccountIdentifierStoredData(identifier /*context()*/)

        val passwordHash = PasswordHash.fromString(storedData.passwordHash ?: error("Missing password hash"))
        val check = PasswordHashing.check(credential.password, passwordHash)

        authCheck(check.valid) { "Invalid password" }
        if (check.updated) {
            val newData = storedData.copy(passwordHash = check.updatedHash!!.toString())
            KtorAuthnzManager.accountStore.updateAccountIdentifierStoredData(identifier, id, newData)
        }

        return identifier
    }

    @Serializable
    data class EmailPassCredentials(val email: String, val password: String)

    override fun Route.register(authContext: PipelineContext<Unit, ApplicationCall>.() -> AuthContext) {
        post("emailpass", {
            request { body<EmailPassCredentials>() }
            response { HttpStatusCode.OK to { body<AuthSessionInformation>() } }
        }) {
            val session = getSession(authContext)

            val credential = call.getUsernamePasswordFromRequest()

            val identifier = auth(session, credential, context)

            context.handleAuthSuccess(session, identifier.resolveToAccountId())
        }
    }
}

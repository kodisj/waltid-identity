package id.walt.webwallet.service.account

import id.walt.webwallet.db.models.Accounts
import id.walt.webwallet.db.models.OidcLogins
import id.walt.webwallet.utils.JwkUtils.verifyToken
import id.walt.webwallet.web.model.OidcUniqueSubjectRequest
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant


import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object OidcUniqueSubjectStrategy : PasswordlessAccountStrategy<OidcUniqueSubjectRequest>() {
    override suspend fun register(tenant: String, request: OidcUniqueSubjectRequest): Result<RegistrationResult> {
        val jwt = verifyToken(request.token)
        val sub = jwt.subject

        require(!AccountsService.hasAccountOidcId(sub)) { "Account already exists with OIDC id: ${request.token}" }

        val createdAccountId = transaction {
            val accountId = Accounts.insert {
                it[Accounts.tenant] = tenant
                it[id] = Uuid.random()
                it[name] = sub
                it[email] = sub
                it[createdOn] = Clock.System.now().toJavaInstant()
            }[Accounts.id]

            OidcLogins.insert {
                it[OidcLogins.tenant] = tenant
                it[OidcLogins.accountId] = accountId
                it[oidcId] = jwt.subject
            }

            accountId
        }

        return Result.success(RegistrationResult(createdAccountId))
    }

    override suspend fun authenticate(tenant: String, request: OidcUniqueSubjectRequest): AuthenticatedUser {
        val jwt = verifyToken(request.token)

        val registeredUserId = if (AccountsService.hasAccountOidcId(jwt.subject)) {
            AccountsService.getAccountByOidcId(jwt.subject)!!.id
        } else {
            AccountsService.register(tenant, request).getOrThrow().id
        }
        // TODO: change id to wallet-id (also in the frontend)
        return UsernameAuthenticatedUser(registeredUserId, jwt.subject)
    }
}

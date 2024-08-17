package id.walt.webwallet.service.account

import id.walt.commons.config.ConfigManager
import id.walt.commons.featureflag.FeatureManager.whenFeatureSuspend
import id.walt.webwallet.FeatureCatalog
import id.walt.webwallet.config.RegistrationDefaultsConfig
import id.walt.webwallet.db.models.*
import id.walt.webwallet.service.WalletService
import id.walt.webwallet.service.WalletServiceManager
import id.walt.webwallet.service.events.AccountEventData
import id.walt.webwallet.service.events.EventType
import id.walt.webwallet.service.issuers.IssuerDataTransferObject
import id.walt.webwallet.web.model.*
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object AccountsService {

    private suspend fun initializeUserAccount(tenant: String, name: String?, registrationResult: RegistrationResult) =
        let {
            val registeredUserId = registrationResult.id

            val createdInitialWalletId = transaction {
                WalletServiceManager.createWallet(tenant, registeredUserId)
            }.also { walletId ->
                //TODO: inject
                WalletServiceManager.issuerUseCase.add(IssuerDataTransferObject.default(walletId))
            }

            val walletService = WalletServiceManager.getWalletService(tenant, registeredUserId, createdInitialWalletId)
            suspend { tryAddDefaultData(walletService) } whenFeatureSuspend (FeatureCatalog.registrationDefaultsFeature)
            registrationResult.also {
                WalletServiceManager.eventUseCase.log(
                    action = EventType.Account.Create,
                    originator = "wallet",
                    tenant = tenant,
                    accountId = registeredUserId,
                    walletId = createdInitialWalletId,
                    data = AccountEventData(accountId = name)
                )
            }
        }

    suspend fun register(tenant: String = "", request: AccountRequest): Result<RegistrationResult> = runCatching {
        when (request) {
            is EmailAccountRequest -> EmailAccountStrategy.register(tenant, request)
            is AddressAccountRequest -> Web3WalletAccountStrategy.register(tenant, request)
            is OidcAccountRequest -> OidcAccountStrategy.register(tenant, request)
            is KeycloakAccountRequest -> KeycloakAccountStrategy.register(tenant, request)
            is OidcUniqueSubjectRequest -> OidcUniqueSubjectStrategy.register(tenant, request)

        }.fold(onSuccess = {
            initializeUserAccount(tenant, request.name, it)

        }, onFailure = {
            throw it
        })
    }


    suspend fun authenticate(tenant: String, request: AccountRequest): Result<AuthenticatedUser> = runCatching {
        when (request) {
            is EmailAccountRequest -> EmailAccountStrategy.authenticate(tenant, request)
            is AddressAccountRequest -> Web3WalletAccountStrategy.authenticate(tenant, request)
            is OidcAccountRequest -> OidcAccountStrategy.authenticate(tenant, request)
            is OidcUniqueSubjectRequest -> OidcUniqueSubjectStrategy.authenticate(tenant, request)
            is KeycloakAccountRequest -> KeycloakAccountStrategy.authenticate(tenant, request)

        }
    }.fold(onSuccess = {
        WalletServiceManager.eventUseCase.log(
            action = EventType.Account.Login,
            tenant = tenant,
            originator = "wallet",
            accountId = it.id,
            walletId = UUID.NIL,
            data = AccountEventData(accountId = it.id.toString())
        )
        Result.success(it)
    }, onFailure = { Result.failure(it) })

    fun getAccountWalletMappings(tenant: String, account: UUID) =
        AccountWalletListing(
            account,
            wallets =
            transaction {
                AccountWalletMappings.innerJoin(Wallets)
                    .selectAll()
                    .where {
                        (AccountWalletMappings.tenant eq tenant) and
                                (AccountWalletMappings.accountId eq account)
                    }
                    .map {
                        AccountWalletListing.WalletListing(
                            id = it[AccountWalletMappings.wallet].value,
                            name = it[Wallets.name],
                            createdOn = it[Wallets.createdOn].toKotlinInstant(),
                            addedOn = it[AccountWalletMappings.addedOn].toKotlinInstant(),
                            permission = it[AccountWalletMappings.permissions]
                        )
                    }
            })

    fun getAccountForWallet(wallet: UUID) = transaction {
        AccountWalletMappings.select(AccountWalletMappings.accountId)
            .where { AccountWalletMappings.wallet eq wallet }.firstOrNull()
            ?.let { it[AccountWalletMappings.accountId] }
    }

    fun hasAccountEmail(tenant: String, email: String) = transaction {
        Accounts.selectAll()
            .where { (Accounts.tenant eq tenant) and (Accounts.email eq email) }
            .count() > 0
    }

    fun hasAccountWeb3WalletAddress(address: String) = transaction {
        Accounts.innerJoin(Web3Wallets).selectAll().where { Web3Wallets.address eq address }.count() > 0
    }

    fun hasAccountOidcId(oidcId: String): Boolean = transaction {
        Accounts.crossJoin(OidcLogins) // TODO crossJoin
            .selectAll()
            .where {
                (Accounts.tenant eq OidcLogins.tenant) and
                        (Accounts.id eq OidcLogins.accountId) and
                        (OidcLogins.oidcId eq oidcId)
            }
            .count() > 0
    }

    fun getAccountByWeb3WalletAddress(address: String) = transaction {
        Accounts.innerJoin(Web3Wallets)
            .selectAll()
            .where { Web3Wallets.address eq address }
            .map { Account(it) }
    }

    fun getAccountByOidcId(oidcId: String) = transaction {
        Accounts.crossJoin(OidcLogins) // TODO crossJoin
            .selectAll()
            .where {
                (Accounts.tenant eq OidcLogins.tenant) and
                        (Accounts.id eq OidcLogins.accountId) and
                        (OidcLogins.oidcId eq oidcId)
            }
            .map { Account(it) }
            .firstOrNull()
    }

    fun get(account: UUID) = transaction {
        Accounts.selectAll().where { Accounts.id eq account }.single().let {
            Account(it)
        }
    }

    private suspend fun tryAddDefaultData(walletService: WalletService) = runCatching {
        val defaultGenerationConfig by lazy { ConfigManager.getConfig<RegistrationDefaultsConfig>() }
        val createdKey = walletService.generateKey(defaultGenerationConfig.defaultKeyConfig)
        val createdDid =
            walletService.createDid(
                method = defaultGenerationConfig.didMethod,
                args = defaultGenerationConfig.didConfig!!.toMutableMap().apply {
                    put("keyId", JsonPrimitive(createdKey))
                    put("alias", JsonPrimitive("Onboarding"))
                })
        walletService.setDefault(createdDid)
    }
}

@Serializable
data class RegistrationResult(
    val id: UUID,
)

@Serializable
sealed class AuthenticatedUser {
    abstract val id: UUID
}

@Serializable
data class UsernameAuthenticatedUser(
    override val id: UUID,
    val username: String,
) : AuthenticatedUser()

@Serializable
data class AddressAuthenticatedUser(
    override val id: UUID,
    val address: String,
) : AuthenticatedUser()

@Serializable
data class KeycloakAuthenticatedUser(
    override val id: UUID,
    val keycloakUserId: String,
) : AuthenticatedUser()

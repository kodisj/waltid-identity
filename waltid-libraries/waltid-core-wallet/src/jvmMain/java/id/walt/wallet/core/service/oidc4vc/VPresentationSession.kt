package id.walt.wallet.core.service.oidc4vc

import id.walt.oid4vc.providers.SIOPSession
import id.walt.oid4vc.requests.AuthorizationRequest
import id.walt.wallet.core.service.exchange.CredentialDataResult
import id.walt.wallet.core.utils.WalletCredential
import kotlinx.datetime.Instant

data class VPresentationSession(
    override val id: String,
    override val authorizationRequest: AuthorizationRequest?,
    override val expirationTimestamp: Instant,
    val selectedCredentials: Set<CredentialDataResult>,
) : SIOPSession(id, authorizationRequest, expirationTimestamp)

package id.walt.issuer.issuance

import id.walt.credentials.vc.vcs.W3CVC
import id.walt.oid4vc.data.AuthenticationMethod
import id.walt.oid4vc.data.OpenId4VPProfile
import id.walt.sdjwt.SDMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class IssuanceRequest(
    val issuerKey: JsonObject,
    val issuerDid: String,

    val credentialConfigurationId: String,
    val credentialData: W3CVC,
    val mapping: JsonObject? = null,
    val selectiveDisclosure: SDMap? = null,
    val authenticationMethod: AuthenticationMethod? = AuthenticationMethod.PRE_AUTHORIZED, // "PWD" OR "ID_TOKEN" OR "VP_TOKEN" OR "PRE_AUTHORIZED" OR "NONE"
    val vpRequestValue: String? = null,
    val vpProfile: OpenId4VPProfile? = OpenId4VPProfile.DEFAULT,
    val useJar: Boolean? = true,
)

@Serializable
data class IssuerOnboardingResponse(
    val issuerKey: JsonElement, val issuerDid: String,
)

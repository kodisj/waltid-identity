package id.walt.webwallet.service.oidc4vc

import id.walt.oid4vc.data.CredentialFormat
import id.walt.webwallet.db.models.WalletCredential

object CredentialFilterUtils {

    fun getJwtVcList(credentials: List<WalletCredential>, disclosures: Map<String, List<String>>?) =
        credentials.filter {
            setOf(CredentialFormat.jwt_vc, CredentialFormat.jwt_vc_json).contains(it.format)
        }.map {
            if (disclosures?.containsKey(it.id) == true) {
                it.document + "~${disclosures[it.id]!!.joinToString("~")}"
            } else {
                it.document
            }
        }
}

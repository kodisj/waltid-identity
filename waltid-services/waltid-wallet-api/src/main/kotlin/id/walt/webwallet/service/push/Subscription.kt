package id.walt.webwallet.service.push

import id.walt.crypto.utils.Base64Utils.decodeFromBase64
import kotlinx.serialization.Serializable
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECPublicKeySpec
import java.security.KeyFactory
import java.security.PublicKey

@Serializable
data class Subscription(
    val auth: String,
    val key: String,
    val endpoint: String,
) {
    /**
     * Returns the base64 encoded auth string as a byte[]
     */
    fun authAsBytes(): ByteArray = auth.decodeFromBase64()

    /**
     * Returns the base64 encoded public key string as a byte[]
     */
    fun keyAsBytes(): ByteArray = key.decodeFromBase64()

    /**
     * Returns the base64 encoded public key as a PublicKey object
     */
    fun userPublicKey(): PublicKey {
        val kf = KeyFactory.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME)
        val ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1")
        val point = ecSpec.curve.decodePoint(keyAsBytes())
        val pubSpec = ECPublicKeySpec(point, ecSpec)
        return kf.generatePublic(pubSpec)
    }
}

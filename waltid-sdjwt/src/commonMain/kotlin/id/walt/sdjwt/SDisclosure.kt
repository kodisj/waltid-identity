package id.walt.sdjwt

import korlibs.crypto.encoding.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Selective Disclosure for a given payload field. Contains salt, field key and field value.
 * @param disclosure  The encoded disclosure, as given in the SD-JWT token.
 * @param salt  Salt value
 * @param key Field key
 * @param value Field value
 */
@ExperimentalJsExport
@JsExport
data class SDisclosure internal constructor(
    val disclosure: String,
    val salt: String,
    val key: String,
    val value: JsonElement
) {
    companion object {
        /**
         * Parse an encoded disclosure string
         */
        fun parse(disclosure: String) = Json.parseToJsonElement(Base64.decode(disclosure, url = true).decodeToString()).jsonArray.let {
            if (it.size != 3) {
                throw Exception("Invalid selective disclosure")
            }
            SDisclosure(disclosure, it[0].jsonPrimitive.content, it[1].jsonPrimitive.content, it[2])
        }
    }
}

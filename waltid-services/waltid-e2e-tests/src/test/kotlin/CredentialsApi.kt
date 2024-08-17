import E2ETestWebService.test
import id.walt.webwallet.db.models.WalletCredential
import id.walt.webwallet.service.credentials.CredentialFilterObject
import id.walt.webwallet.usecase.credential.CredentialStatusResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.uuid.UUID
import kotlin.test.assertNotNull

class CredentialsApi(private val client: HttpClient) {
    suspend fun list(
        wallet: UUID,
        filter: CredentialFilterObject = CredentialFilterObject.default,
        expectedSize: Int = 0,
        vararg expectedCredential: String
    ) = test("/wallet-api/wallet/{wallet}/credentials - list credentials") {
        client.get("/wallet-api/wallet/$wallet/credentials") {
            url {
                filter.toMap().onEach {
                    parameters.append(it.key, it.value.toString())
                }
            }
        }.expectSuccess().apply {
            val credentials = body<List<WalletCredential>>()
            assert(credentials.size == expectedSize) { "should have $expectedSize credentials, but has ${credentials.size}" }
            expectedCredential.onEach { cid -> assertNotNull(credentials.single { it.id == cid }) { "credential not found for id: $cid" } }
        }
    }

    suspend fun get(wallet: UUID, credential: String, output: ((WalletCredential) -> Unit)? = null) =
        test("/wallet-api/wallet/{wallet}/credentials/{credentialId} - get credential") {
            client.get("/wallet-api/wallet/$wallet/credentials/$credential").expectSuccess().apply {
                val credential = body<WalletCredential>()
                output?.invoke(credential)
            }
        }

    suspend fun delete(wallet: UUID, credential: String, permanent: Boolean = false) =
        test("/wallet-api/wallet/{wallet}/credentials/{credentialId} - delete credential") {
            client.delete("/wallet-api/wallet/$wallet/credentials/$credential") {
                url {
                    parameters.append("permanent", "$permanent")
                }
            }.expectSuccess()
        }

    suspend fun restore(wallet: UUID, credential: String, output: ((WalletCredential) -> Unit)? = null) =
        test("/wallet-api/wallet/{wallet}/credentials/{credentialId}/restore - restore credential") {
            client.post("/wallet-api/wallet/$wallet/credentials/$credential/restore").expectSuccess().apply {
                output?.invoke(body<WalletCredential>())
            }
        }

    suspend fun accept(wallet: UUID, credential: String) =
        test("/wallet-api/wallet/{wallet}/credentials/{credentialId}/accept - accept credential") {
            client.post("/wallet-api/wallet/$wallet/credentials/$credential/accept").expectSuccess()
        }

    suspend fun reject(wallet: UUID, credential: String, note: String? = null) =
        test("/wallet-api/wallet/{wallet}/credentials/{credentialId}/reject - reject credential") {
            client.post("/wallet-api/wallet/$wallet/credentials/$credential/reject") {
                setBody(mapOf("note" to note))
            }.expectSuccess()
        }

    suspend fun status(wallet: UUID, credential: String, output: ((List<CredentialStatusResult>) -> Unit)? = null) =
        test("/wallet-api/wallet/{wallet}/credentials/{credentialId}/status - get credential status") {
            client.get("/wallet-api/wallet/$wallet/credentials/$credential/status").expectSuccess().apply {
                val result = body<List<CredentialStatusResult>>()
                output?.invoke(result)
            }
        }

    suspend fun attachCategory(wallet: UUID, credential: String, vararg categories: String) =
        test("/wallet-api/wallet/{wallet}/credentials/{credentialId}/category - attach category") {
            client.put("/wallet-api/wallet/$wallet/credentials/$credential/category") {
                setBody(categories.toList())
            }.expectSuccess()
        }

    suspend fun detachCategory(wallet: UUID, credential: String, vararg categories: String) =
        test("/wallet-api/wallet/{wallet}/credentials/{credentialId}/category - detach category") {
            client.delete("/wallet-api/wallet/$wallet/credentials/$credential/category") {
                setBody(categories.toList())
            }.expectSuccess()
        }

    suspend fun store(wallet: UUID, credential: String) =
        test("/wallet-api/wallet/{wallet}/credentials - store credential") {
            TODO("Not implemented")
        }

    private fun CredentialFilterObject.toMap() = Json.encodeToJsonElement(this).jsonObject.toMap()
}
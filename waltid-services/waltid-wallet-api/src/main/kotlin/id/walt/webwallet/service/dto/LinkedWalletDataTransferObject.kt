@file:OptIn(ExperimentalUuidApi::class)

package id.walt.webwallet.service.dto

import id.walt.commons.temp.UuidSerializer
import id.walt.webwallet.db.models.Web3Wallets
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class LinkedWalletDataTransferObject(
    @Serializable(with = UuidSerializer::class) // required to serialize Uuid, until kotlinx.serialization uses Kotlin 2.1.0
    val id: Uuid,
    val address: String,
    val ecosystem: String,
    val owner: Boolean,
) {

    constructor(result: ResultRow) : this(
        id = result[Web3Wallets.id],
        address = result[Web3Wallets.address],
        ecosystem = result[Web3Wallets.ecosystem],
        owner = result[Web3Wallets.owner],
    )

}

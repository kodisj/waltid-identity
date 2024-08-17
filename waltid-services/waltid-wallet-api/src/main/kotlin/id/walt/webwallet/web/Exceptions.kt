package id.walt.webwallet.web

import id.walt.webwallet.db.models.AccountWalletPermissions
import io.ktor.http.*
import kotlinx.serialization.SerialName

sealed class WebException(val status: HttpStatusCode, message: String) : Exception(message)


@SerialName("InsufficientPermissions")
class InsufficientPermissionsException(
    minimumRequired: AccountWalletPermissions,
    current: AccountWalletPermissions,
) : WebException(
    HttpStatusCode.Forbidden,
    "You do not have enough permissions to access this action. Minimum required permissions: $minimumRequired, your current permissions: $current"
)

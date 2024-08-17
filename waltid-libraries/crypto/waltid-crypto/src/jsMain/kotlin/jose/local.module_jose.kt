@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

import kotlin.js.Promise

external fun <T : KeyLike> createLocalJWKSet(jwks: JSONWebKeySet): (protectedHeader: JWSHeaderParameters, token: FlattenedJWSInput) -> Promise<T>

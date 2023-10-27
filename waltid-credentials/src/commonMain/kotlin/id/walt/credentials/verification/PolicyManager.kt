package id.walt.credentials.verification

import id.walt.credentials.verification.policies.*
import id.walt.credentials.verification.policies.vp.MaximumCredentialsPolicy
import id.walt.credentials.verification.policies.vp.MinimumCredentialsPolicy

object PolicyManager {
    private val mappedPolicies = HashMap<String, VerificationPolicy>()

    // TODO: policies
    /*
     *  "presentation_definition",
     *  "revoked",
     *  "rego",
     *  multi signature?
     *
     * >>> NEXT:
            "challenge",
            "webhook"
     */

    fun registerPolicies(vararg policies: VerificationPolicy) {
        policies.forEach { policy ->
            if (mappedPolicies.containsKey(policy.name))
                throw IllegalArgumentException("Policy does already exist: ${policy.name} (mapped to ${mappedPolicies[policy.name]!!::class.simpleName}). Choose another name for your policy (${policy::class.simpleName}.")

            mappedPolicies[policy.name] = policy
        }
    }

    init {
        registerPolicies(
            JwtSignaturePolicy(),
            JsonSchemaPolicy(),
            ExpirationDatePolicy(),
            NotBeforeDatePolicy(),
            WebhookPolicy(),
            MinimumCredentialsPolicy(),
            MaximumCredentialsPolicy()
        )
    }

    fun getPolicy(name: String): VerificationPolicy =
        mappedPolicies[name] ?: throw IllegalArgumentException("No policy found by name: $name")



}

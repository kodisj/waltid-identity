<div align="center">
 <h1>Kotlin Multiplatform Crypto library</h1>
 <span>by </span><a href="https://walt.id">walt.id</a>
  <p>Create JSON Web Tokens (JWTs) that support <b>Selective Disclosure</b><p>

<a href="https://walt.id/community">
<img src="https://img.shields.io/badge/Join-The Community-blue.svg?style=flat" alt="Join community!" />
</a>
<a href="https://twitter.com/intent/follow?screen_name=walt_id">
<img src="https://img.shields.io/twitter/follow/walt_id.svg?label=Follow%20@walt_id" alt="Follow @walt_id" />
</a>
</div>

## Installation

Add the crypto library as a dependency to your Kotlin or Java project.

### walt.id Repository

Add the Maven repository which hosts the walt.id libraries to your build.gradle file.

```kotlin
repositories {
    maven { url = uri("https://maven.waltid.dev/releases") }
} 
```

### Library Dependency

Adding the crypto library as dependency. Specify the version that coincides with the latest or required
snapshot for your project. [Latest releases](https://github.com/walt-id/waltid-identity/releases).

```kotlin
dependencies {
  implementation("id.walt.crypto:waltid-crypto:<version>")
}
```

Replace `version` with the version of the walt.id crypto library you want to use.
Note: As the crypto lib is part of the mono-repo walt.id identity, you need to use the version of
walt.id identity.

## What it provides

<table>
    <tbody>
        <!-- header -->
        <tr>
            <td align="center" rowspan="3">Feature</td>
            <td align="center" rowspan="3" colspan="2">Category</td>
            <td align="center" colspan="8">Key</td>
        </tr>
        <!-- sub-header key type -->
        <tr>
            <td align="center" colspan="4">Local</td>
            <td align="center" colspan="4">TSE</td>
        </tr>
        <!-- sub-sub-header key algorithm -->
        <tr>
            <!-- local -->
            <td align="center">ed25519</td>
            <td align="center">secp256k1</td>
            <td align="center">secp256r1</td>
            <td align="center">rsa</td>
            <!-- tse -->
            <td align="center">ed25519</td>
            <td align="center">secp256k1</td>
            <td align="center">secp256r1</td>
            <td align="center">rsa</td>
        </tr>
        <!-- content -->
        <!-- sign -->
        <!-- jws -->
        <tr>
            <td align="center" rowspan="2">sign</td>
            <td align="center" colspan="2">jws</td>
            <!-- local -->
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <!-- tse -->
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
        </tr>
        <!-- raw -->
        <tr>
            <td align="center" colspan="2">raw</td>
            <!-- local -->
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <!-- tse -->
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
        </tr>
        <!-- end sign -->
        <tr><td align="center" colspan="11"></td></tr>
        <!-- verify -->
        <!-- jws -->
        <tr>
            <td align="center" rowspan="2">verify</td>
            <td align="center" colspan="2">jws</td>
            <!-- local -->
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <!-- tse -->
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
        </tr>
        <!-- raw -->
        <tr>
            <td align="center" colspan="2">raw</td>
            <!-- local -->
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <!-- tse -->
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
        </tr>
        <!-- end verify -->
        <tr><td align="center" colspan="11"></td></tr>
        <!-- export -->
        <!-- jwk -->
        <!-- private -->
        <tr>
            <td align="center" rowspan="6">export</td>
            <td align="center" rowspan="2">jwk</td>
            <td align="center">private</td>
            <!-- local -->
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <!-- tse -->
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
        </tr>
        <!-- public -->
        <tr>
            <td align="center">public</td>
            <!-- local -->
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <!-- tse -->
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
        </tr>
        <!-- pem -->
        <!-- private -->
        <tr>
            <td align="center" rowspan="2">pem</td>
            <td align="center">private</td>
            <!-- local -->
            <td align="center">&cross;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <!-- tse -->
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
        </tr>
        <!-- public -->
        <tr>
            <td align="center">public</td>
            <!-- local -->
            <td align="center">&cross;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <!-- tse -->
            <td align="center">&cross;</td>
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
        </tr>
        <!-- JsonObject -->
        <!-- private -->
        <tr>
            <td align="center" rowspan="2">JsonObject</td>
            <td align="center">private</td>
            <!-- local -->
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
            <!-- tse -->
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
        </tr>
        <!-- public -->
        <tr>
            <td align="center">public</td>
            <!-- local -->
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <!-- tse -->
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
        </tr>
        <!-- end export -->
        <tr><td align="center" colspan="11"></td></tr>
        <!-- import -->
        <!-- jwk -->
        <!-- private -->
        <tr>
            <td align="center" rowspan="6">import</td>
            <td align="center" rowspan="2">jwk</td>
            <td align="center">private</td>
            <!-- local -->
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <!-- tse -->
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
        </tr>
        <!-- public -->
        <tr>
            <td align="center">public</td>
            <!-- local -->
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <!-- tse -->
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
        </tr>
        <!-- pem -->
        <!-- private -->
        <tr>
            <td align="center" rowspan="2">pem</td>
            <td align="center">private</td>
            <!-- local -->
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <!-- tse -->
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
        </tr>
        <!-- public -->
        <tr>
            <td align="center">public</td>
            <!-- local -->
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <!-- tse -->
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
        </tr>
        <!-- raw -->
        <!-- private -->
        <tr>
            <td align="center" rowspan="2">raw</td>
            <td align="center">private</td>
            <!-- local -->
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <!-- tse -->
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
            <td align="center">&dash;</td>
        </tr>
        <!-- public -->
        <tr>
            <td align="center">public</td>
            <!-- local -->
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <td align="center">&check;</td>
            <!-- tse -->
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
            <td align="center">&cross;</td>
        </tr>
        <!-- end import -->
    </tbody>
</table>

- &check; implemented
- &cross; not implemented
- &dash; not available

### Signature schemes

| Type  |   ECDSA   | JOSE ID | Description                                                     |
|:-----:|:---------:|:-------:|:----------------------------------------------------------------|
| EdDSA |  Ed25519  |  EdDSA  | EdDSA + Curve25519                                              |
| ECDSA | secp256r1 |  ES256  | ECDSA + SECG curve secp256r1 ("NIST P-256")                     |
| ECDSA | secp256k1 | ES256K  | ECDSA + SECG curve secp256k1 (Koblitz curve as used in Bitcoin) |
|  RSA  |    RSA    |  RS256  | RSA                                                             |

### Available on platforms

|  Platform   |           | Availability |
|:-----------:|:---------:|:------------:|
|    Java     |    JVM    |   &check;    |
|     JS      |   Node    |   &check;    |
|             | WebCrypto |   &check;    |
|   Native    | libsodium |   &cross;    |
|             |  OpenSSL  |   &cross;    |
| WebAssembly |   WASM    |   &cross;    |

### JWS compatibility (recommended)

| Algorithm | JVM provider |   JS provider / platform    |
|:---------:|:------------:|:---------------------------:|
|   EdDSA   | Nimbus JOSE  |       jose / Node.js        |
|   ES256   | Nimbus JOSE  | jose / Node.js & Web Crypto |
|  ES256K   | Nimbus JOSE  |       jose / Node.js        |
|   RS256   | Nimbus JOSE  | jose / Node.js & Web Crypto |

## How to use it

The library provides the following key entities to work with:

- [JWKKey](https://github.com/walt-id/waltid-identity/blob/main/waltid-libraries/waltid-crypto/src/commonMain/kotlin/id/walt/crypto/keys/jwk/JWKKey.kt) -
  an implementation of a local (in-memory) key (private / public)
- [TSEKey](https://github.com/walt-id/waltid-identity/blob/main/waltid-libraries/waltid-crypto/src/commonMain/kotlin/id/walt/crypto/keys/tse/TSEKey.kt) -
  an implementation of a Hashicorp Vault Transit Secrets Engine key (private / public)
- [OCIKey](https://github.com/walt-id/waltid-identity/blob/main/waltid-libraries/waltid-crypto/src/commonMain/kotlin/id/walt/crypto/keys/oci/OCIKeyRestApi.kt) - an implementation of an OCI key (private / public)
### Working with JWKKey

**Create key**

```kotlin
val key = JWKKey.generate(KeyType.Ed25519)
```

**Sign**

- jws

```kotlin
val signature = key.signJws(payloadString.encodeToByteArray())
```

- raw

```kotlin
val signature = key.signRaw(payloadString.encodeToByteArray())
```

**Verify**

- jws

```kotlin
val verificationResult = key.getPublicKey().verifyJws(signature)
```

- raw

```kotlin
val verificationResult = key.getPublicKey().verifyRaw(signature , payloadString.encodeToByteArray())
```

**Import key**

- jwk

```kotlin
val keyResult = JWKKey.importJWK(jwkString)
```

- pem

```kotlin
val keyResult = JWKKey.importPEM(pemString)
```

- raw

```kotlin
val key = JWKKey.importRawPublicKey(KeyType.Ed25519, bytes, JWKKeyMetadata())
```

**Export public key**

- jwk

```kotlin
val jwkString = key.exportJWK()
```


- JsonObject

```kotlin
val jwkObject = key.exportJWKObject()
```

### Working with TSEKey

A Hashicorp Vault's Transit Secrets Engine instance is required in order to be able to use a
`TSEKey` for signing and verification. This implies covering the following steps:

1. [set up the vault](#setup-vault)
2. [enable a Transit Secrets Engine instance](#enable-a-transit-secrets-engine-instance)

### Setup Vault

More details about installing Hashicorp Vault can be found in the Hashicorp Vault
[documentation](https://developer.hashicorp.com/vault/docs/install)
and [tutorials](https://developer.hashicorp.com/vault/install).

#### Linux

- [binary download](https://developer.hashicorp.com/vault/install)
- package manager (see below)

Install the vault using the package manager:

```shell
wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo
tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install vault
```

Start up the vault server:

```shell
vault server -dev -dev-root-token-id="dev-only-token"
```

#### MacOS

- [binary download](https://developer.hashicorp.com/vault/install)
- package manager (see below)

Install the vault using the package manager:

```shell
brew tap hashicorp/tap
brew install hashicorp/tap/vault
```

Start up the vault server:

```shell
vault server -dev -dev-root-token-id="dev-only-token"
```

#### Windows

- [binary download](https://developer.hashicorp.com/vault/install)

#### Docker

```shell
docker run -p 8200:8200 --cap-add=IPC_LOCK -e VAULT_DEV_ROOT_TOKEN_ID=dev-only-token -e VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200 hashicorp/vault
```

### Enable a Transit Secrets Engine instance

Check
the [encryption as a service tutorial](https://developer.hashicorp.com/vault/tutorials/encryption-as-a-service/eaas-transit#configure-transit-secrets-engine)
for more details on how to enable a Transit Secrets Engine.

**Command line interface**

Linux / MacOS

```shell
export VAULT_TOKEN="dev-only-token"
export VAULT_ADDR='http://localhost:8200'
vault secrets enable transit
```

Windows

```shell
set VAULT_TOKEN="dev-only-token"
set VAULT_ADDR=http://localhost:8200
vault secrets enable transit
```

**User interface**

1. log into the Vault (http://localhost:8200) with token (_dev-only-token_)
2. on the left-side menu, select 'Secrets Engines'
3. on the 'Secrets Engines' page, select 'enable new engine'
4. on the 'Enable a secrets engine' page, select 'Transit' from the 'Generic' group
5. click 'Next', then 'Enable Engine'

For usage examples on _create_, _sign_, _verify_, _import_ and _export_ functions see
[Working with JWKKey](#working-with-jwkKey).

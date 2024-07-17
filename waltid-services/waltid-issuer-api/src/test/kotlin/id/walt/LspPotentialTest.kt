package id.walt

import COSE.AlgorithmID
import COSE.OneKey
import cbor.Cbor
import com.upokecenter.cbor.CBORObject
import id.walt.crypto.keys.KeyGenerationRequest
import id.walt.crypto.keys.KeyManager
import id.walt.crypto.keys.KeyType
import id.walt.crypto.utils.Base64Utils.base64UrlDecode
import id.walt.did.helpers.WaltidServices
import id.walt.issuer.base.config.ConfigManager
import id.walt.issuer.base.config.WebConfig
import id.walt.issuer.issuerModule
import id.walt.issuer.utils.LspPotentialInteropEvent
import id.walt.mdoc.COSECryptoProviderKeyInfo
import id.walt.mdoc.SimpleCOSECryptoProvider
import id.walt.mdoc.cose.COSESign1
import id.walt.mdoc.dataelement.*
import id.walt.mdoc.doc.MDoc
import id.walt.mdoc.doc.MDocVerificationParams
import id.walt.mdoc.doc.VerificationType
import id.walt.mdoc.docrequest.MDocRequestBuilder
import id.walt.mdoc.issuersigned.IssuerSigned
import id.walt.oid4vc.OpenID4VCI
import id.walt.oid4vc.data.*
import id.walt.oid4vc.requests.AuthorizationRequest
import id.walt.oid4vc.requests.CredentialRequest
import id.walt.oid4vc.requests.TokenRequest
import id.walt.oid4vc.responses.CredentialResponse
import id.walt.oid4vc.responses.TokenResponse
import id.walt.oid4vc.util.randomUUID
import id.walt.sdjwt.SDJwtVC
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.kotlincrypto.hash.sha2.SHA256
import java.security.KeyPairGenerator
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.*

object LspPotentialTest {
  val http = HttpClient {
    install(ContentNegotiation) {
      json()
    }
    followRedirects = false
  }
  var webConfig: WebConfig? = null

  init {
      runBlocking {
        WaltidServices.minimalInit()
      }

      ConfigManager.loadConfigs(arrayOf())
      webConfig = ConfigManager.getConfig<WebConfig>()
      embeddedServer(
        CIO,
        port = webConfig!!.webPort,
        host = webConfig!!.webHost,
        module = Application::issuerModule
      ).start(wait = false)
  }

  @OptIn(ExperimentalEncodingApi::class)
  @Test
  fun testLspPotentialTrack1(): Unit = runBlocking {
    // ### steps 1-6
    val offerResp = http.get("http://${webConfig!!.webHost}:${webConfig!!.webPort}/openid4vc/lspPotentialCredentialOffer")
    //val offerResp = http.get("https://issuer.potential.walt-test.cloud/openid4vc/lspPotentialCredentialOffer")
    assertEquals(HttpStatusCode.OK, offerResp.status)

    val offerUri = offerResp.bodyAsText()
    //val offerUri = "openid-credential-offer://?credential_offer=%7B%22credential_issuer%22:%22https://mdlpilot.japaneast.cloudapp.azure.com:8017%22,%22credential_configuration_ids%22:%5B%22org.iso.18013.5.1.mDL%22%5D,%22grants%22:%7B%22authorization_code%22:%7B%22issuer_state%22:%22fX35ofcFrYUh2ltjnDZtNXIW7UTUrme767eqPvNix44%22,%22authorization_server%22:%22https://mdlpilot.japaneast.cloudapp.azure.com:8017%22%7D%7D%7D"
    //val offerUri = "openid-credential-offer://?credential_offer=%7B%22credential_issuer%22%3A%22https%3A%2F%2Flaunchpad.vii.proton.mattrlabs.io%22%2C%22credential_configuration_ids%22%3A%5B%22b59d6a4c-b331-476b-9a4e-ea234c7882c8%22%5D%7D"

    // -------- WALLET ----------
    // as WALLET: receive credential offer, either being called via deeplink or by scanning QR code
    // parse credential URI
    val parsedOffer = OpenID4VCI.parseAndResolveCredentialOfferRequestUrl(offerUri)
    //assertContains(parsedOffer.credentialConfigurationIds, "potential.light.profile")

    // ### get issuer metadata, steps 7-10
    val providerMetadataUri = OpenID4VCI.getCIProviderMetadataUrl(parsedOffer.credentialIssuer)
    val oauthMetadataUri = OpenID4VCI.getOAuthProviderMetadataUrl(parsedOffer.credentialIssuer)
    val providerMetadata = http.get(providerMetadataUri).bodyAsText().let { OpenIDProviderMetadata.fromJSONString(it) }
    val oauthMetadata = http.get(oauthMetadataUri).body<OpenIDProviderMetadata>()
    assertNotNull(providerMetadata.credentialConfigurationsSupported)
    assertNotNull(providerMetadata.credentialEndpoint)
    assertNotNull(oauthMetadata.authorizationEndpoint)
    assertNotNull(oauthMetadata.tokenEndpoint)
    //assertContains(providerMetadata.codeChallengeMethodsSupported.orEmpty(), "S256")

    // resolve offered credentials
    val offeredCredentials = OpenID4VCI.resolveOfferedCredentials(parsedOffer, providerMetadata)
    val offeredCredential = offeredCredentials.first()
    assertEquals(CredentialFormat.mso_mdoc, offeredCredential.format)
    assertEquals("org.iso.18013.5.1.mDL", offeredCredential.docType)

    // ### step 11: confirm issuance (nothing to do)

    // ### step 12-15: authorization
    val codeVerifier = randomUUID()

    val codeChallenge =
      codeVerifier.let { Base64.UrlSafe.encode(SHA256().digest(it.toByteArray(Charsets.UTF_8))).trimEnd('=') }

    val authReq = AuthorizationRequest(
      responseType = setOf(ResponseType.Code),
      clientId = "test-wallet",
      redirectUri = "https://test-wallet.org",
      scope = setOf("openid"),
      issuerState = parsedOffer.grants[GrantType.authorization_code.value]?.issuerState,
      authorizationDetails = offeredCredentials.map {
        AuthorizationDetails.fromOfferedCredential(
          it,
          providerMetadata.credentialIssuer
        )
      },
      codeChallenge = codeChallenge,
      codeChallengeMethod = "S256"
    )
    val location = if(oauthMetadata.requirePushedAuthorizationRequests != true) {
      val authResp = http.get(oauthMetadata.authorizationEndpoint!!) {
        authReq.toHttpParameters().forEach {
          parameter(it.key, it.value.first())
        }
      }
      assertEquals(HttpStatusCode.Found, authResp.status)
      Url(authResp.headers[HttpHeaders.Location]!!)

    } else {
      // TODO: check PAR support, also check issuer of panasonic (https://mdlpilot.japaneast.cloudapp.azure.com:8017), which uses PAR and doesn't seem to work
      val parResp = http.submitForm(oauthMetadata.pushedAuthorizationRequestEndpoint!!, parametersOf(
        authReq.toHttpParameters()
      ))
      val authResp = http.get(oauthMetadata.authorizationEndpoint!!) {
        parameter("request_uri", Json.parseToJsonElement(parResp.bodyAsText()).jsonObject["request_uri"]!!.jsonPrimitive.content)
      }
      assertEquals(HttpStatusCode.Found, authResp.status)
      Url(authResp.headers[HttpHeaders.Location]!!)
    }
    assertContains(location.parameters.names(), "code")
    // ### step 16-18: access token retrieval
    val tokenResp = http.submitForm(oauthMetadata.tokenEndpoint!!,
      parametersOf(
        TokenRequest(
          GrantType.authorization_code, authReq.clientId,
          authReq.redirectUri, location.parameters["code"]!!,
          codeVerifier = codeVerifier
      ).toHttpParameters())).let { TokenResponse.fromJSON(it.body<JsonObject>()) }
    assertNotNull(tokenResp.accessToken)
    assertTrue(tokenResp.isSuccess)

    val kpg = KeyPairGenerator.getInstance("EC")
    kpg.initialize(256)
    val deviceKeyId = "device-key"
    val deviceKeyPair = kpg.genKeyPair()
    // ### steps 19-22: credential issuance

    // TODO: move COSE signing functionality to crypto lib?
    val coseKey = OneKey(deviceKeyPair.public, null).AsCBOR().EncodeToBytes()
    val credReq = CredentialRequest.forOfferedCredential(offeredCredential, ProofOfPossession.CWTProofBuilder(
      issuerUrl = parsedOffer.credentialIssuer, clientId = authReq.clientId, nonce = tokenResp.cNonce,
      coseKeyAlgorithm = COSE.AlgorithmID.ECDSA_256.AsCBOR().toString(), coseKey = coseKey, null
    ).build(SimpleCOSECryptoProvider(listOf(COSECryptoProviderKeyInfo(deviceKeyId, AlgorithmID.ECDSA_256,
      deviceKeyPair.public, deviceKeyPair.private))), deviceKeyId))

    val cwt = Cbor.decodeFromByteArray(COSESign1.serializer(), credReq.proof!!.cwt!!.base64UrlDecode())
    assertNotNull(cwt.payload)
    val cwtPayload = Cbor.decodeFromByteArray<MapElement>(cwt.payload!!)
    assertEquals(DEType.textString, cwtPayload.value.get(MapKey(ProofOfPossession.CWTProofBuilder.LABEL_ISS))?.type)
    val cwtProtectedHeader = Cbor.decodeFromByteArray<MapElement>(cwt.protectedHeader)
    assertEquals(cwtProtectedHeader.value[MapKey(ProofOfPossession.CWTProofBuilder.HEADER_LABEL_ALG)]!!.value, -7L)
    assertEquals(cwtProtectedHeader.value[MapKey(ProofOfPossession.CWTProofBuilder.HEADER_LABEL_CONTENT_TYPE)]!!.value, "openid4vci-proof+cwt")
    assertContentEquals((cwtProtectedHeader.value[MapKey(ProofOfPossession.CWTProofBuilder.HEADER_LABEL_COSE_KEY)] as ByteStringElement).value, coseKey)

    val credResp = http.post(providerMetadata.credentialEndpoint!!) {
      contentType(ContentType.Application.Json)
      bearerAuth(tokenResp.accessToken!!)
      setBody(credReq.toJSON())
    }.body<JsonObject>().let { CredentialResponse.fromJSON(it) }

    assertTrue(credResp.isSuccess)
    assertContains(credResp.customParameters.keys, "credential_encoding")
    assertEquals("issuer-signed", credResp.customParameters["credential_encoding"]!!.jsonPrimitive.content)
    assertNotNull(credResp.credential)
    val mdoc = MDoc(credReq.docType!!.toDE(), IssuerSigned.fromMapElement(
      Cbor.decodeFromByteArray(credResp.credential!!.jsonPrimitive.content.base64UrlDecode())
    ), null)
    assertEquals(credReq.docType, mdoc.docType.value)
    assertNotNull(mdoc.issuerSigned)
    assertTrue(mdoc.verifySignature(SimpleCOSECryptoProvider(listOf(
      LspPotentialInteropEvent.loadPotentialIssuerKeys()
    )), LspPotentialInteropEvent.POTENTIAL_ISSUER_KEY_ID))
    assertNotNull(mdoc.issuerSigned.nameSpaces)
    assertNotEquals(0, mdoc.issuerSigned.nameSpaces!!.size)
    assertNotNull(mdoc.issuerSigned.issuerAuth?.x5Chain)
    assertTrue(mdoc.verify(MDocVerificationParams(
      VerificationType.forIssuance, LspPotentialInteropEvent.POTENTIAL_ISSUER_KEY_ID, deviceKeyId,
      mDocRequest = MDocRequestBuilder(offeredCredential.docType!!).build()
    ), SimpleCOSECryptoProvider(listOf(LspPotentialInteropEvent.POTENTIAL_ISSUER_CRYPTO_PROVIDER_INFO))))
  }

  @Test
  fun testParseCWTExample() {
    val data = "d28443a10126a104524173796d6d657472696345434453413235365850a701756" +
        "36f61703a2f2f61732e6578616d706c652e636f6d02656572696b77037818636f" +
        "61703a2f2f6c696768742e6578616d706c652e636f6d041a5612aeb0051a5610d" +
        "9f0061a5610d9f007420b7158405427c1ff28d23fbad1f29c4c7c6a555e601d6f" +
        "a29f9179bc3d7438bacaca5acd08c8d4d4f96131680c429a01f85951ecee743a5" +
        "2b9b63632c57209120e1c9e30"
    val parsedCwt = Cbor.decodeFromHexString(COSESign1.serializer(), data)
    parsedCwt != null

  }

  @OptIn(ExperimentalEncodingApi::class)
  @Test
  fun testLspPotentialTrack2(): Unit = runBlocking {
    // ### steps 1-6
    val offerResp = http.get("http://${webConfig!!.webHost}:${webConfig!!.webPort}/openid4vc/lspPotentialCredentialOfferT2")
    //val offerResp = http.get("https://issuer.potential.walt-test.cloud/openid4vc/lspPotentialCredentialOfferT2")
    assertEquals(HttpStatusCode.OK, offerResp.status)

    val offerUri = offerResp.bodyAsText()

    // -------- WALLET ----------
    // as WALLET: receive credential offer, either being called via deeplink or by scanning QR code
    // parse credential URI
    val parsedOffer = OpenID4VCI.parseAndResolveCredentialOfferRequestUrl(offerUri)
    //assertContains(parsedOffer.credentialConfigurationIds, "potential.light.profile")

    // ### get issuer metadata, steps 7-10
    val providerMetadataUri = OpenID4VCI.getCIProviderMetadataUrl(parsedOffer.credentialIssuer)
    val jwtIssuerMetadataUri = OpenID4VCI.getJWTIssuerProviderMetadataUrl(parsedOffer.credentialIssuer)
    val providerMetadata = http.get(providerMetadataUri).bodyAsText().let { OpenIDProviderMetadata.fromJSONString(it) }
    val oauthMetadata = http.get(jwtIssuerMetadataUri).body<OpenIDProviderMetadata>()
    val jwtIssuerMetadata = http.get(jwtIssuerMetadataUri).body<OpenIDProviderMetadata>()
    assertNotNull(providerMetadata.credentialConfigurationsSupported)
    assertNotNull(providerMetadata.credentialEndpoint)
    assertNotNull(jwtIssuerMetadata.issuer)
    assertNotNull(jwtIssuerMetadata.jwksUri)
    assertContains(providerMetadata.codeChallengeMethodsSupported.orEmpty(), "S256")

    val jwks = http.get(jwtIssuerMetadata.jwksUri!!).body<JsonObject>()
    assertContains(jwks.keys, "keys")

    // resolve offered credentials
    val offeredCredentials = OpenID4VCI.resolveOfferedCredentials(parsedOffer, providerMetadata)
    val offeredCredential = offeredCredentials.first()
    assertEquals(CredentialFormat.sd_jwt_vc, offeredCredential.format)
    assertEquals("urn:eu.europa.ec.eudi:pid:1", offeredCredential.docType)

    // ### step 11: confirm issuance (nothing to do)

    // ### step 12-15: authorization
    val codeVerifier = randomUUID()

    val codeChallenge =
      codeVerifier.let { Base64.UrlSafe.encode(SHA256().digest(it.toByteArray(Charsets.UTF_8))).trimEnd('=') }

    val authReq = AuthorizationRequest(
      responseType = setOf(ResponseType.Code),
      clientId = "test-wallet",
      redirectUri = "https://test-wallet.org",
      scope = setOf("openid"),
      issuerState = parsedOffer.grants[GrantType.authorization_code.value]?.issuerState,
      authorizationDetails = offeredCredentials.map {
        AuthorizationDetails.fromOfferedCredential(
          it,
          providerMetadata.credentialIssuer
        )
      },
      codeChallenge = codeChallenge,
      codeChallengeMethod = "S256"
    )
    val location = if(oauthMetadata.requirePushedAuthorizationRequests != true) {
      val authResp = http.get(oauthMetadata.authorizationEndpoint!!) {
        authReq.toHttpParameters().forEach {
          parameter(it.key, it.value.first())
        }
      }
      assertEquals(HttpStatusCode.Found, authResp.status)
      Url(authResp.headers[HttpHeaders.Location]!!)

    } else {
      // TODO: check PAR support, also check issuer of panasonic (https://mdlpilot.japaneast.cloudapp.azure.com:8017), which uses PAR and doesn't seem to work
      val parResp = http.submitForm(oauthMetadata.pushedAuthorizationRequestEndpoint!!, parametersOf(
        authReq.toHttpParameters()
      ))
      val authResp = http.get(oauthMetadata.authorizationEndpoint!!) {
        parameter("request_uri", Json.parseToJsonElement(parResp.bodyAsText()).jsonObject["request_uri"]!!.jsonPrimitive.content)
      }
      assertEquals(HttpStatusCode.Found, authResp.status)
      Url(authResp.headers[HttpHeaders.Location]!!)
    }
    assertContains(location.parameters.names(), "code")
    // ### step 16-18: access token retrieval
    val tokenResp = http.submitForm(oauthMetadata.tokenEndpoint!!,
      parametersOf(
        TokenRequest(
          GrantType.authorization_code, authReq.clientId,
          authReq.redirectUri, location.parameters["code"]!!,
          codeVerifier = codeVerifier
        ).toHttpParameters())).let { TokenResponse.fromJSON(it.body<JsonObject>()) }
    assertNotNull(tokenResp.accessToken)
    assertTrue(tokenResp.isSuccess)

    val kpg = KeyPairGenerator.getInstance("EC")
    kpg.initialize(256)
    val deviceKeyPair = KeyManager.createKey(KeyGenerationRequest(keyType = KeyType.secp256r1))
    // ### steps 19-22: credential issuance

    // TODO: move COSE signing functionality to crypto lib?
    val credReq = CredentialRequest.forOfferedCredential(offeredCredential, ProofOfPossession.JWTProofBuilder(
      issuerUrl = parsedOffer.credentialIssuer, clientId = authReq.clientId, nonce = tokenResp.cNonce, keyId = null,
      keyJwk = deviceKeyPair.getPublicKey().exportJWKObject()
    ).build(deviceKeyPair))

    val credResp = http.post(providerMetadata.credentialEndpoint!!) {
      contentType(ContentType.Application.Json)
      bearerAuth(tokenResp.accessToken!!)
      setBody(credReq.toJSON())
    }.body<JsonObject>().let { CredentialResponse.fromJSON(it) }
    assertTrue(credResp.isSuccess)
    assertNotNull(credResp.credential)
    val sdJwtVc = SDJwtVC.parse(credResp.credential!!.jsonPrimitive.content)
    assertNotNull(sdJwtVc.cnfObject)
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun testIdemiaMdoc() {
    val payload = "A26A6E616D65537061636573A1716F72672E69736F2E31383031332E352E3188D818585BA4686469676573744944016672616E646F6D50039DA9A7E5BCA31DBE656F25F5AF883B71656C656D656E744964656E7469666965726A69737375655F646174656C656C656D656E7456616C7565D903EC6A323032342D30362D3133D818585CA4686469676573744944026672616E646F6D50994A47799B9A787B7A40D933B75535B971656C656D656E744964656E7469666965726B6578706972795F646174656C656C656D656E7456616C7565D903EC6A323032352D30362D3133D818585AA4686469676573744944036672616E646F6D50B6EBB7401B7497C5C5EB8BA70A6C6DD371656C656D656E744964656E7469666965726B66616D696C795F6E616D656C656C656D656E7456616C75656B53696C76657273746F6E65D8185852A4686469676573744944046672616E646F6D50F00E60668324C2D471E4F7A790BD8C6F71656C656D656E744964656E7469666965726A676976656E5F6E616D656C656C656D656E7456616C756564496E6761D818585BA4686469676573744944056672616E646F6D5025665076612DE57BA76F08EAF211CAF671656C656D656E744964656E7469666965726A62697274685F646174656C656C656D656E7456616C7565D903EC6A313939312D31312D3036D8185855A4686469676573744944066672616E646F6D503A8655FAA8551A87D3E139ABA1BC31D171656C656D656E744964656E7469666965726F69737375696E675F636F756E7472796C656C656D656E7456616C7565625553D818585BA4686469676573744944076672616E646F6D501C17557857CA0A0ADD7480F428C05A3E71656C656D656E744964656E7469666965726F646F63756D656E745F6E756D6265726C656C656D656E7456616C7565683132333435363738D81858A2A4686469676573744944086672616E646F6D50C9C4380FFC5736DCB0C8D0083CB95DA171656C656D656E744964656E7469666965727264726976696E675F70726976696C656765736C656C656D656E7456616C756581A37576656869636C655F63617465676F72795F636F646561416A69737375655F64617465D903EC6A323032332D30312D30316B6578706972795F64617465D903EC6A323034332D30312D30316A697373756572417574688443A10126A118215901613082015D30820104A0030201020206018C91D9C219300A06082A8648CE3D04030230363134303206035504030C2B4A3146774A50383743362D514E5F5753494F6D4A415163366E3543515F625A6461464A3547446E5731526B301E170D3233313232323134303635365A170D3234313031373134303635365A30363134303206035504030C2B4A3146774A50383743362D514E5F5753494F6D4A415163366E3543515F625A6461464A3547446E5731526B3059301306072A8648CE3D020106082A8648CE3D03010703420004028A5579BA09A58472730D582A49113977C2A4A10A97D63560F1613868F8C5383650EBA7C53C52215913F78A85548BAED61904A04D8DC5E959A37FB24C550E0C300A06082A8648CE3D040302034700304402206716349C05CBC446578B8CC0AEE4D6F124A90DB1AFE04219174DF0E66EAF93DE022055ACDBB15750BD9825B9CF672584315A5CD74FD8BA7FEC2050AC7428261B8F70590282D81859027DA66776657273696F6E63312E306F646967657374416C676F726974686D675348412D3235366C76616C756544696765737473A1716F72672E69736F2E31383031332E352E31A80158202383E9A1CBD068A5A99889E920606A32BFDB849B903F3B8407D479374692578802582090BB126967F6B8C979EB5D44EBAEA2B10992CC5DD454FBA2A570DBEC831DFD0F035820E2F932893D5A4F423B6ED33EEB2465E6C4DB5DB5488F6077542DAF699CB8986404582057030E6F5F8201CE6BB83A4117C9CD52E2304D1EDE5B28C511E93909C2A455840558201AA7C2E53CFF82AC245ABCFEFD52443B36F912CDF0F556C3FCFFE8AA0B18E68A065820C8499DF84A68B10EF0C0D15E3F201E3794C1EE68D43014FBB86EA7F7A86194A3075820C2FFAE81BDDFEF9E4AC7678B11E6F8D27883D5084F37C584EEB684841DBBE34D08582097419184972CAAD230126C381023CFC9D1378207EF82DC6BC38FCFAE6B1604926D6465766963654B6579496E666FA2696465766963654B6579A4010220012158200307EAA2717D8D0B9D92517D3A1358DC4E2B1942288EE2B42CE6B44CDA8448BD2258203C5FB92E6CA0088B080DA9BBFC1E25E987DE7E147BCCAD02EB9367D0CF1731D9716B6579417574686F72697A6174696F6E73A16A6E616D6553706163657381716F72672E69736F2E31383031332E352E3167646F6354797065756F72672E69736F2E31383031332E352E312E6D444C6C76616C6964697479496E666FA3667369676E6564C074323032342D30362D31335430343A35383A34305A6976616C696446726F6DC074323032342D30362D31335430343A35383A34305A6A76616C6964556E74696CC074323032352D30362D31335430343A35383A34305A58405064B1D9C1B647D6C4B1EB9E0BFD65E5DD415FFB1A7FECE8ED73ACD56CED20A7CB14769DC6355F0B30484CEDE5831D845EF04B1C09033CC69C1FDE6CC29A44E0"
    val mdoc = MDoc("org.iso.18013.5.1.mDL".toDE(), IssuerSigned.fromMapElement(
      Cbor.decodeFromByteArray(payload.hexToByteArray())
    ), null)
    assertNotNull(mdoc.issuerSigned.issuerAuth?.x5Chain)
  }

  @Test
  fun testPanasonicCWTProof() {
    val cwtStr = "0oRYb6MBJgN0b3BlbmlkNHZjaS1wcm9vZitjd3RoQ09TRV9LZXlYS6QBAiABIVggLJJHre5L8a2_3AqrrCAEPHrtwlOz3dNqapJwFCZFOv4iWCA73eeuJFAlZ1UTiZ3wVyRr7hdANprYk5WQbbEmYyFTPKEEWCA0MTRlODQxOGMxYzlkOGM4NDczZWZhM2YxOWZhMjc2OVhlpAFqd2FsbGV0LWRldgN4KGh0dHBzOi8vaXNzdWVyLnBvdGVudGlhbC53YWx0LXRlc3QuY2xvdWQGGmZyXdoKWCRkNTdjNzQxYi0wYWI0LTQwMWYtYjg1NS05YzdjMWY3MDIzNmFYQAdLyVSbkUSCwO6QnYw5wNayBfgkC6W2MklsjisIJZuK3ld9om_Jo9mrfLDuPcUkV__IwUrNBiUgqVMZqIuSFKA"
    val cwt = Cbor.decodeFromByteArray(COSESign1.serializer(), cwtStr.base64UrlDecode())
    assertNotNull(cwt.payload)
    val cwtPayload = Cbor.decodeFromByteArray<MapElement>(cwt.payload!!)
    assertEquals(DEType.textString, cwtPayload.value.get(MapKey(ProofOfPossession.CWTProofBuilder.LABEL_ISS))?.type)
    val cwtProtectedHeader = Cbor.decodeFromByteArray<MapElement>(cwt.protectedHeader)
    assertEquals(cwtProtectedHeader.value[MapKey(ProofOfPossession.CWTProofBuilder.HEADER_LABEL_ALG)]!!.value, -7L)
    assertEquals(cwtProtectedHeader.value[MapKey(ProofOfPossession.CWTProofBuilder.HEADER_LABEL_CONTENT_TYPE)]!!.value, "openid4vci-proof+cwt")

    val tokenHeader = cwt.decodeProtectedHeader()
    val rawKey = (tokenHeader.value[MapKey(ProofOfPossession.CWTProofBuilder.HEADER_LABEL_COSE_KEY)] as ByteStringElement).value
    val cryptoProvider = SimpleCOSECryptoProvider(listOf(COSECryptoProviderKeyInfo("pub-key", AlgorithmID.ECDSA_256,
      OneKey(CBORObject.DecodeFromBytes(rawKey)).AsPublicKey()
    )))
    //assertTrue(cryptoProvider.verify1(cwt, "pub-key"))

  }
}

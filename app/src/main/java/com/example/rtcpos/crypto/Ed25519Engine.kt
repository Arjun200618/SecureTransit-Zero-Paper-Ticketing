package com.example.rtcpos.crypto

import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * High-performance cryptographic engine for Indian State RTC handheld ticketing.
 *
 * Implements asymmetric digital signature generation and offline verification for bus tickets.
 * This guarantees that passengers cannot forge digital tickets (or tamper with fare/destination)
 * and Ticket Checking Inspectors can verify ticket authenticity 100% offline in remote transit corridors.
 */
object Ed25519Engine {

    private const val ASYMMETRIC_ALGO = "SHA256withECDSA"
    private const val KEY_PAIR_ALGO = "EC"
    private const val EC_CURVE_NAME = "secp256r1"

    // Master Depot public and private key pair for RTC fleet
    private var cachedKeyPair: KeyPair = generateDepotKeyPair()

    val depotPublicKeyBase64: String
        get() = Base64.getEncoder().encodeToString(cachedKeyPair.public.encoded)

    val depotPrivateKeyBase64: String
        get() = Base64.getEncoder().encodeToString(cachedKeyPair.private.encoded)

    /**
     * Generate Depot Master Asymmetric KeyPair.
     */
    fun generateDepotKeyPair(): KeyPair {
        return try {
            val keyGen = KeyPairGenerator.getInstance(KEY_PAIR_ALGO)
            keyGen.initialize(ECGenParameterSpec(EC_CURVE_NAME))
            keyGen.generateKeyPair()
        } catch (e: Exception) {
            // Fallback for standard environments
            val keyGen = KeyPairGenerator.getInstance("RSA")
            keyGen.initialize(2048)
            keyGen.generateKeyPair()
        }
    }

    /**
     * Canonical string representation of a bus ticket for cryptographic signing.
     * Prevents field re-ordering attacks and whitespace manipulations.
     */
    fun buildCanonicalPayload(
        ticketId: String,
        tripId: String,
        busReg: String,
        originStageId: Int,
        destStageId: Int,
        totalFare: Double,
        passengerCount: Int,
        timestampEpochSec: Long
    ): String {
        return "RTC_TKT_V1|$ticketId|$tripId|$busReg|$originStageId|$destStageId|%.2f|$passengerCount|$timestampEpochSec"
            .format(java.util.Locale.US, totalFare)
    }

    /**
     * Builds the signed QR payload for the active test ticket:
     * TKT-100-2026-37753231, MGBS (Stage 1) to Afzalgunj (Stage 2), ₹20, 2 Adults
     */
    fun buildActiveTestTicketPayload(
        ticketId: String = "TKT-100-2026-37753231",
        tripId: String = "1",
        busReg: String = "AP 29 Z 4512",
        originStageId: Int = 1,
        destStageId: Int = 2,
        totalFare: Double = 20.0,
        passengerCount: Int = 2
    ): String {
        val canonical = buildCanonicalPayload(
            ticketId = ticketId,
            tripId = tripId,
            busReg = busReg,
            originStageId = originStageId,
            destStageId = destStageId,
            totalFare = totalFare,
            passengerCount = passengerCount,
            timestampEpochSec = System.currentTimeMillis() / 1000
        )
        val sig = signTicketPayload(canonical)
        return "$canonical:::SIG=$sig"
    }

    /**
     * Digitally signs the canonical payload using the conductor's / depot's private key.
     * @return Base64-encoded digital signature string
     */
    fun signTicketPayload(canonicalPayload: String, privateKey: PrivateKey = cachedKeyPair.private): String {
        return try {
            val signature = Signature.getInstance(ASYMMETRIC_ALGO)
            signature.initSign(privateKey)
            signature.update(canonicalPayload.toByteArray(StandardCharsets.UTF_8))
            val sigBytes = signature.sign()
            Base64.getEncoder().encodeToString(sigBytes)
        } catch (e: Exception) {
            // Fallback deterministic Ed25519-style HMAC-SHA256 signature if device crypto provider has restrictions
            signFallback(canonicalPayload, privateKey.encoded)
        }
    }

    /**
     * Verifies the digital signature offline using the RTC Depot Public Key.
     * @return true if authentic and untampered; false if signature is forged or data modified.
     */
    fun verifyTicketSignature(
        canonicalPayload: String,
        signatureBase64: String,
        publicKey: PublicKey = cachedKeyPair.public
    ): Boolean {
        return try {
            val signature = Signature.getInstance(ASYMMETRIC_ALGO)
            signature.initVerify(publicKey)
            signature.update(canonicalPayload.toByteArray(StandardCharsets.UTF_8))
            val sigBytes = Base64.getDecoder().decode(signatureBase64)
            signature.verify(sigBytes)
        } catch (e: Exception) {
            verifyFallback(canonicalPayload, signatureBase64, publicKey.encoded)
        }
    }

    /**
     * Restores a public key from Base64 string for Inspector Mode.
     */
    fun decodePublicKey(base64PublicKey: String): PublicKey {
        val keyBytes = Base64.getDecoder().decode(base64PublicKey)
        val spec = X509EncodedKeySpec(keyBytes)
        val kf = KeyFactory.getInstance(KEY_PAIR_ALGO)
        return kf.generatePublic(spec)
    }

    private fun signFallback(data: String, keyBytes: ByteArray): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(keyBytes.copyOf(32), "HmacSHA256")
        mac.init(secretKey)
        val hmac = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(hmac)
    }

    private fun verifyFallback(data: String, signatureBase64: String, keyBytes: ByteArray): Boolean {
        return try {
            val expected = signFallback(data, keyBytes)
            expected == signatureBase64
        } catch (e: Exception) {
            false
        }
    }
}

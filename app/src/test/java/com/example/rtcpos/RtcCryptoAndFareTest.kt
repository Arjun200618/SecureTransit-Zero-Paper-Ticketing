package com.example.rtcpos

import com.example.rtcpos.crypto.Ed25519Engine
import com.example.rtcpos.data.model.Direction
import com.example.rtcpos.data.model.ServiceType
import com.example.rtcpos.data.model.TransitPreloads
import com.example.rtcpos.upi.UpiProtocolEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RtcCryptoAndFareTest {

    @Test
    fun testFareCalculation() {
        val route = TransitPreloads.sampleRoutes[0] // Hyderabad to Vijayawada
        val origin = route.stages[0] // S1 MGBS (0 km)
        val dest = route.stages[3] // S4 Suryapet (135 km)

        // Express service: rate ~ 1.45/km -> distance 135 km -> approx 195.75 -> rounded to 196
        val fareAdult = TransitPreloads.calculateFare(
            origin = origin,
            destination = dest,
            serviceType = ServiceType.EXPRESS,
            adults = 1,
            children = 0,
            concessions = 0
        )
        assertTrue("Fare should be greater than 0", fareAdult > 0.0)

        val fareTwoAdults = TransitPreloads.calculateFare(
            origin = origin,
            destination = dest,
            serviceType = ServiceType.EXPRESS,
            adults = 2,
            children = 0,
            concessions = 0
        )
        assertEquals(fareAdult * 2, fareTwoAdults, 0.01)

        val fareChild = TransitPreloads.calculateFare(
            origin = origin,
            destination = dest,
            serviceType = ServiceType.EXPRESS,
            adults = 0,
            children = 1,
            concessions = 0
        )
        assertEquals((fareAdult * 0.5).toInt().toDouble(), fareChild, 1.0)
    }

    @Test
    fun testEd25519SignAndVerifyAuthentic() {
        val canonical = Ed25519Engine.buildCanonicalPayload(
            ticketId = "TKT-TEST-001",
            tripId = "1",
            busReg = "AP 29 Z 4512",
            originStageId = 1,
            destStageId = 8,
            totalFare = 240.0,
            passengerCount = 2,
            timestampEpochSec = 1718000000L
        )

        val signature = Ed25519Engine.signTicketPayload(canonical)
        assertTrue("Signature must not be empty", signature.isNotBlank())

        val isValid = Ed25519Engine.verifyTicketSignature(canonical, signature)
        assertTrue("Authentic signature must verify successfully", isValid)
    }

    @Test
    fun testEd25519RejectTamperedPayload() {
        val canonical = Ed25519Engine.buildCanonicalPayload(
            ticketId = "TKT-TEST-002",
            tripId = "1",
            busReg = "AP 29 Z 4512",
            originStageId = 1,
            destStageId = 4,
            totalFare = 70.0,
            passengerCount = 1,
            timestampEpochSec = 1718000000L
        )

        val signature = Ed25519Engine.signTicketPayload(canonical)

        // Passenger tries to tamper destination stage from 4 to 8
        val tampered = canonical.replace("|4|70.00|", "|8|70.00|")
        val isValid = Ed25519Engine.verifyTicketSignature(tampered, signature)
        assertFalse("Tampered ticket payload must fail cryptographic verification", isValid)
    }

    @Test
    fun testUpiUriConstruction() {
        val pwaUrl = UpiProtocolEngine.buildPwaTicketUrl("TXN12345", "PENDING")
        val upiUri = UpiProtocolEngine.buildUpiUri(
            UpiProtocolEngine.UpiParams(
                amount = 145.0,
                transactionRef = "TXN12345",
                transactionNote = "RTC-100-HYD-VJA-S1toS5",
                pwaTicketUrl = pwaUrl
            )
        )

        assertTrue(upiUri.startsWith("upi://pay?"))
        assertTrue(upiUri.contains("pa=apsrtc.depot101@sbi"))
        assertTrue(upiUri.contains("am=145.00"))
        assertTrue(upiUri.contains("cu=INR"))
        assertTrue(upiUri.contains("tr=TXN12345"))
        assertTrue(upiUri.contains("url="))
    }

    @Test
    fun testEmbeddedTicketUrlWithQueryParams() {
        val busNumber = "AP 29 Z 4512"
        val originStop = "MGBS Terminal"
        val destStop = "Vijayawada PNBS"
        val fareAmount = 240.00
        val passengerCount = 2

        val pwaUrl = UpiProtocolEngine.buildPwaTicketUrl(
            bus = busNumber,
            from = originStop,
            to = destStop,
            fare = fareAmount,
            passengers = passengerCount,
            ticketId = "TXN998877",
            signature = "TEST_SIG"
        )

        assertTrue("Should contain bus parameter", pwaUrl.contains("bus=AP29Z4512") || pwaUrl.contains("bus=AP+29+Z+4512") || pwaUrl.contains("bus=AP%2029%20Z%204512"))
        assertTrue("Should contain from parameter", pwaUrl.contains("from=MGBS"))
        assertTrue("Should contain to parameter", pwaUrl.contains("to=VijayawadaPNBS") || pwaUrl.contains("to=Vijayawada+PNBS") || pwaUrl.contains("to=Vijayawada%20PNBS"))
        assertTrue("Should contain fare parameter", pwaUrl.contains("fare=240"))
        assertTrue("Should contain passenger count parameter", pwaUrl.contains("p=2") || pwaUrl.contains("passengers=2"))

        // When embedded into UPI URI
        val upiUri = UpiProtocolEngine.buildUpiUri(
            UpiProtocolEngine.UpiParams(
                amount = fareAmount,
                transactionRef = "TXN998877",
                transactionNote = "RTC-Test",
                pwaTicketUrl = pwaUrl
            )
        )

        assertTrue(upiUri.startsWith("upi://pay?"))
        assertTrue(upiUri.contains("am=240.00"))
        assertTrue(upiUri.contains("url="))
    }

    @Test
    fun testExactRequestedTicketLinkUrl() {
        val ticketUrl = UpiProtocolEngine.buildTicketLinkUrl(
            bus = "AP 29 Z 4512",
            route = "100M",
            from = "MGBS",
            to = "HitechCity",
            fare = 40.0,
            passengers = 2
        )

        assertEquals(
            "https://upi-based-bus-e-ticket.vercel.app?bus=AP29Z4512&route=100M&from=MGBS&to=HitechCity&fare=40&p=2",
            ticketUrl
        )
    }

    @Test
    fun testActiveTestTicketPayloadGenerationAndVerification() {
        val payload = Ed25519Engine.buildActiveTestTicketPayload(
            ticketId = "TKT-100-2026-37753231",
            tripId = "1",
            busReg = "AP 29 Z 4512",
            originStageId = 1,
            destStageId = 2,
            totalFare = 20.0,
            passengerCount = 2
        )

        assertTrue("Payload must start with canonical prefix", payload.startsWith("RTC_TKT_V1|TKT-100-2026-37753231"))
        assertTrue("Payload must contain signature delimiter", payload.contains(":::SIG="))

        val parts = payload.split(":::SIG=")
        assertEquals(2, parts.size)
        val canonical = parts[0]
        val sig = parts[1]

        assertTrue("Signature on active ticket payload must be valid", Ed25519Engine.verifyTicketSignature(canonical, sig))
        assertTrue("Canonical payload must specify fare 20.00", canonical.contains("|20.00|"))
        assertTrue("Canonical payload must specify 2 passengers", canonical.contains("|2|"))
    }
}

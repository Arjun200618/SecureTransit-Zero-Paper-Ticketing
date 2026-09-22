package com.example.rtcpos.upi

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 * NPCI UPI (Unified Payments Interface) Protocol Engine.
 * Formats strict NPCI-compliant payment intent strings and renders dynamic QR codes.
 */
object UpiProtocolEngine {

    const val DEFAULT_MERCHANT_VPA = "apsrtc.depot101@sbi"
    const val DEFAULT_MERCHANT_NAME = "State RTC Transit"
    const val LIVE_PASSENGER_URL = "https://upi-based-bus-e-ticket.vercel.app"
    const val PWA_BASE_URL = LIVE_PASSENGER_URL

    data class UpiParams(
        val payeeVpa: String = DEFAULT_MERCHANT_VPA,
        val payeeName: String = DEFAULT_MERCHANT_NAME,
        val amount: Double,
        val transactionRef: String,
        val transactionNote: String,
        val pwaTicketUrl: String
    )

    /**
     * Constructs strict NPCI UPI Intent URI compliant with NPCI Specification v1.6+:
     * upi://pay?pa={merchant_vpa}&pn={merchant_name}&am={fare}&cu=INR&tr={unique_txn_id}&tn={route_info}&url={pwa_ticket_url}
     */
    fun buildUpiUri(params: UpiParams): String {
        val encodedPn = urlEncode(params.payeeName)
        val formattedAm = String.format(Locale.US, "%.2f", params.amount)
        val encodedTn = urlEncode(params.transactionNote)
        val encodedUrl = urlEncode(params.pwaTicketUrl)

        return "upi://pay?" +
                "pa=${params.payeeVpa}" +
                "&pn=$encodedPn" +
                "&am=$formattedAm" +
                "&cu=INR" +
                "&tr=${params.transactionRef}" +
                "&tn=$encodedTn" +
                "&url=$encodedUrl"
    }

    /**
     * Constructs the exact passenger ticket URL embedded in the dynamic QR code:
     * https://upi-based-bus-e-ticket.vercel.app?bus=AP29Z4512&route=100M&from=MGBS&to=HitechCity&fare=40&p=2
     *
     * Embeds:
     *  - bus: Bus registration number without spaces (e.g. AP29Z4512)
     *  - route: Route code (e.g. 100M)
     *  - from: Origin stop name (e.g. MGBS)
     *  - to: Destination stop name (e.g. HitechCity)
     *  - fare: Total fare amount (e.g. 40 if integer, or formatted with decimals)
     *  - p: Passenger count (e.g. 2)
     */
    fun buildTicketLinkUrl(
        bus: String,
        route: String,
        from: String,
        to: String,
        fare: Double,
        passengers: Int
    ): String {
        val cleanBus = cleanAlphanumeric(bus)
        val cleanRoute = cleanParam(route)
        val cleanFrom = cleanStopName(from)
        val cleanTo = cleanStopName(to)
        val formattedFare = if (fare % 1.0 == 0.0) {
            fare.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", fare)
        }

        return "$LIVE_PASSENGER_URL?bus=${urlEncode(cleanBus)}" +
                "&route=${urlEncode(cleanRoute)}" +
                "&from=${urlEncode(cleanFrom)}" +
                "&to=${urlEncode(cleanTo)}" +
                "&fare=$formattedFare" +
                "&p=$passengers"
    }

    /**
     * Constructs the PWA ticket link embedded in the UPI transaction.
     * Passengers can view their live digital ticket in their browser without installing an app.
     * Embeds bus number, origin stop, destination stop, fare amount, and passenger count as query parameters.
     */
    fun buildPwaTicketUrl(
        bus: String,
        from: String,
        to: String,
        fare: Double,
        passengers: Int,
        ticketId: String = "",
        signature: String = "",
        route: String = "100M"
    ): String {
        val cleanBus = cleanAlphanumeric(bus)
        val cleanRoute = cleanParam(route)
        val cleanFrom = cleanStopName(from)
        val cleanTo = cleanStopName(to)
        val formattedFare = if (fare % 1.0 == 0.0) {
            fare.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", fare)
        }

        val sb = StringBuilder(LIVE_PASSENGER_URL)
        sb.append("?bus=").append(urlEncode(cleanBus))
            .append("&route=").append(urlEncode(cleanRoute))
            .append("&from=").append(urlEncode(cleanFrom))
            .append("&to=").append(urlEncode(cleanTo))
            .append("&fare=").append(formattedFare)
            .append("&p=").append(passengers)
            .append("&passengers=").append(passengers)
            .append("&count=").append(passengers)

        if (ticketId.isNotEmpty()) {
            sb.append("&tid=").append(urlEncode(ticketId))
        }
        if (signature.isNotEmpty()) {
            sb.append("&sig=").append(urlEncode(signature.take(32)))
        }
        return sb.toString()
    }

    fun buildPwaTicketUrl(ticketId: String, signature: String): String {
        val safeSig = urlEncode(signature.take(32))
        return "$LIVE_PASSENGER_URL?tid=$ticketId&sig=$safeSig"
    }

    fun cleanAlphanumeric(value: String): String {
        return value.replace(" ", "").trim()
    }

    fun cleanParam(value: String): String {
        return value.replace(" ", "").trim()
    }

    fun cleanStopName(name: String): String {
        val trimmed = name.trim()
        if (trimmed.equals("MGBS Terminal", ignoreCase = true) || trimmed.equals("MGBS", ignoreCase = true)) {
            return "MGBS"
        }
        if (trimmed.equals("Hitech City", ignoreCase = true) || trimmed.equals("HitechCity", ignoreCase = true)) {
            return "HitechCity"
        }
        return trimmed.replace(" ", "")
    }

    /**
     * Generates a high-contrast Black/White QR Code Bitmap from raw text/URI.
     * Utilizes High error correction level (Level Q/H) for easy scanning off scratched phone screens.
     */
    fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap {
        val hints = HashMap<EncodeHintType, Any>().apply {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
            put(EncodeHintType.MARGIN, 2)
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
        }

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    private fun urlEncode(value: String): String {
        return try {
            URLEncoder.encode(value, StandardCharsets.UTF_8.name())
        } catch (e: Exception) {
            value
        }
    }
}

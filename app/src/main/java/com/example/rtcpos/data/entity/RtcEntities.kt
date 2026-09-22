package com.example.rtcpos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_sessions")
data class TripSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conductorEmpId: String,
    val conductorName: String,
    val busRegNumber: String,
    val routeCode: String,
    val routeName: String,
    val serviceType: String,
    val direction: String,
    val depotCode: String,
    val startTime: Long = System.currentTimeMillis(),
    val currentStageNumber: Int = 1,
    val isActive: Boolean = true
)

@Entity(tableName = "issued_tickets")
data class TicketEntity(
    @PrimaryKey
    val ticketId: String,
    val tripId: Long,
    val busRegNumber: String,
    val conductorEmpId: String,
    val originStageNumber: Int,
    val originStageName: String,
    val destStageNumber: Int,
    val destStageName: String,
    val adultCount: Int,
    val childCount: Int,
    val concessionCount: Int,
    val totalFare: Double,
    val paymentMode: String, // CASH or UPI
    val paymentStatus: String, // COMPLETED, PENDING
    val upiTxnId: String,
    val passengerPhone: String = "",
    val pwaUrl: String,
    val canonicalPayload: String,
    val ed25519Signature: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSyncedToDepot: Boolean = false
) {
    val totalPassengers: Int
        get() = adultCount + childCount + concessionCount
}

data class WaybillStats(
    val totalTickets: Int,
    val totalPassengers: Int,
    val totalRevenue: Double,
    val cashRevenue: Double,
    val upiRevenue: Double,
    val pendingUpiCount: Int,
    val pendingUpiAmount: Double
)

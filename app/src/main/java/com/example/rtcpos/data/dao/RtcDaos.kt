package com.example.rtcpos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rtcpos.data.entity.TicketEntity
import com.example.rtcpos.data.entity.TripSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripSessionEntity): Long

    @Query("SELECT * FROM trip_sessions WHERE isActive = 1 ORDER BY id DESC LIMIT 1")
    fun getActiveTrip(): Flow<TripSessionEntity?>

    @Query("SELECT * FROM trip_sessions WHERE id = :tripId")
    suspend fun getTripById(tripId: Long): TripSessionEntity?

    @Query("UPDATE trip_sessions SET currentStageNumber = :stageNumber WHERE id = :tripId")
    suspend fun updateCurrentStage(tripId: Long, stageNumber: Int)

    @Query("UPDATE trip_sessions SET isActive = 0 WHERE id = :tripId")
    suspend fun closeTrip(tripId: Long)
}

@Dao
interface TicketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity)

    @Query("SELECT * FROM issued_tickets WHERE tripId = :tripId ORDER BY timestamp DESC")
    fun getTicketsForTrip(tripId: Long): Flow<List<TicketEntity>>

    @Query("SELECT * FROM issued_tickets ORDER BY timestamp DESC")
    fun getAllTickets(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM issued_tickets WHERE ticketId = :ticketId LIMIT 1")
    suspend fun getTicketById(ticketId: String): TicketEntity?

    @Query("SELECT * FROM issued_tickets WHERE upiTxnId = :utr LIMIT 1")
    suspend fun getTicketByUtr(utr: String): TicketEntity?

    @Query("SELECT * FROM issued_tickets WHERE ticketId LIKE '%' || :query || '%' OR passengerPhone LIKE '%' || :query || '%' OR upiTxnId LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchTickets(query: String): List<TicketEntity>

    @Query("SELECT COUNT(*) FROM issued_tickets WHERE tripId = :tripId")
    fun getTotalTicketCount(tripId: Long): Flow<Int>

    @Query("UPDATE issued_tickets SET isSyncedToDepot = 1 WHERE ticketId IN (:ids)")
    suspend fun markTicketsSynced(ids: List<String>)
}

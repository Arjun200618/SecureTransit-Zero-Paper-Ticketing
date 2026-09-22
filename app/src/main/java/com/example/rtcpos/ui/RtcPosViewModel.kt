package com.example.rtcpos.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rtcpos.data.RtcDatabase
import com.example.rtcpos.data.RtcRepository
import com.example.rtcpos.data.entity.TicketEntity
import com.example.rtcpos.data.entity.TripSessionEntity
import com.example.rtcpos.data.entity.WaybillStats
import com.example.rtcpos.data.model.Direction
import com.example.rtcpos.data.model.PaymentMode
import com.example.rtcpos.data.RtcRepository.VerificationReport
import com.example.rtcpos.data.model.RouteInfo
import com.example.rtcpos.data.model.ServiceType
import com.example.rtcpos.data.model.Stage
import com.example.rtcpos.data.model.TransitPreloads
import com.example.rtcpos.data.model.VerificationStatus
import com.example.rtcpos.soundbox.SoundboxManager
import com.example.rtcpos.upi.UpiProtocolEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RtcPosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RtcRepository
    val soundboxManager = SoundboxManager(application.applicationContext)

    init {
        val db = RtcDatabase.getDatabase(application)
        repository = RtcRepository(db.tripDao(), db.ticketDao())
    }

    val activeTrip: StateFlow<TripSessionEntity?> = repository.activeTrip
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val waybillStats: StateFlow<WaybillStats> = activeTrip.flatMapLatest { trip ->
        if (trip != null) repository.getWaybillStats(trip.id)
        else flowOf(WaybillStats(0, 0, 0.0, 0.0, 0.0, 0, 0.0))
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WaybillStats(0, 0, 0.0, 0.0, 0.0, 0, 0.0)
    )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val tripTickets: StateFlow<List<TicketEntity>> = activeTrip.flatMapLatest { trip ->
        if (trip != null) repository.getTicketsForTrip(trip.id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Ticketing State ---
    private val _selectedOriginStage = MutableStateFlow<Stage?>(null)
    val selectedOriginStage = _selectedOriginStage.asStateFlow()

    private val _selectedDestStage = MutableStateFlow<Stage?>(null)
    val selectedDestStage = _selectedDestStage.asStateFlow()

    private val _adultCount = MutableStateFlow(1)
    val adultCount = _adultCount.asStateFlow()

    private val _childCount = MutableStateFlow(0)
    val childCount = _childCount.asStateFlow()

    private val _concessionCount = MutableStateFlow(0)
    val concessionCount = _concessionCount.asStateFlow()

    private val _paymentMode = MutableStateFlow(PaymentMode.UPI)
    val paymentMode = _paymentMode.asStateFlow()

    private val _passengerPhone = MutableStateFlow("")
    val passengerPhone = _passengerPhone.asStateFlow()

    // Calculated fare
    private val _currentCalculatedFare = MutableStateFlow(0.0)
    val currentCalculatedFare = _currentCalculatedFare.asStateFlow()

    // UPI Modal State
    data class UpiModalState(
        val isOpen: Boolean = false,
        val upiUri: String = "",
        val qrBitmap: Bitmap? = null,
        val fareAmount: Double = 0.0,
        val txnRef: String = "",
        val pwaUrl: String = "",
        val isPaymentReceived: Boolean = false,
        val secondsRemaining: Int = 120,
        val issuedTicket: TicketEntity? = null
    )
    private val _upiModalState = MutableStateFlow(UpiModalState())
    val upiModalState = _upiModalState.asStateFlow()
    private var paymentSimulationJob: Job? = null

    // Inspector Mode State
    private val _isInspectorMode = MutableStateFlow(false)
    val isInspectorMode = _isInspectorMode.asStateFlow()

    private val _inspectorVerificationReport = MutableStateFlow<RtcRepository.VerificationReport?>(null)
    val inspectorVerificationReport = _inspectorVerificationReport.asStateFlow()

    private val _inspectorSearchQuery = MutableStateFlow("")
    val inspectorSearchQuery = _inspectorSearchQuery.asStateFlow()

    private val _inspectorSearchResults = MutableStateFlow<List<TicketEntity>>(emptyList())
    val inspectorSearchResults = _inspectorSearchResults.asStateFlow()

    // Depot Export State
    private val _depotExportJson = MutableStateFlow<String?>(null)
    val depotExportJson = _depotExportJson.asStateFlow()
    private val _depotExportQr = MutableStateFlow<Bitmap?>(null)
    val depotExportQr = _depotExportQr.asStateFlow()

    // Active Route reference
    val availableRoutes = TransitPreloads.sampleRoutes

    fun getCurrentRoute(): RouteInfo {
        val trip = activeTrip.value
        return availableRoutes.firstOrNull { it.routeCode == trip?.routeCode } ?: availableRoutes.first()
    }

    fun initDefaultStages(trip: TripSessionEntity) {
        val route = availableRoutes.firstOrNull { it.routeCode == trip.routeCode } ?: availableRoutes.first()
        val currentStageNum = trip.currentStageNumber
        val origin = route.stages.firstOrNull { it.stageNumber == currentStageNum } ?: route.stages.first()
        val dest = route.stages.getOrNull(currentStageNum) ?: route.stages.last()
        _selectedOriginStage.value = origin
        _selectedDestStage.value = dest
        recalculateFare(origin, dest, ServiceType.valueOf(trip.serviceType))
    }

    fun setOriginStage(stage: Stage) {
        _selectedOriginStage.value = stage
        val trip = activeTrip.value ?: return
        recalculateFare(stage, _selectedDestStage.value, ServiceType.valueOf(trip.serviceType))
    }

    fun setDestStage(stage: Stage) {
        _selectedDestStage.value = stage
        val trip = activeTrip.value ?: return
        recalculateFare(_selectedOriginStage.value, stage, ServiceType.valueOf(trip.serviceType))
    }

    fun updatePassengerCounts(adults: Int, children: Int, concessions: Int) {
        _adultCount.value = adults.coerceAtLeast(0)
        _childCount.value = children.coerceAtLeast(0)
        _concessionCount.value = concessions.coerceAtLeast(0)
        val trip = activeTrip.value ?: return
        recalculateFare(_selectedOriginStage.value, _selectedDestStage.value, ServiceType.valueOf(trip.serviceType))
    }

    fun setPaymentMode(mode: PaymentMode) {
        _paymentMode.value = mode
    }

    fun setPassengerPhone(phone: String) {
        _passengerPhone.value = phone
    }

    private fun recalculateFare(origin: Stage?, dest: Stage?, serviceType: ServiceType) {
        if (origin == null || dest == null) {
            _currentCalculatedFare.value = 0.0
            return
        }
        val fare = TransitPreloads.calculateFare(
            origin = origin,
            destination = dest,
            serviceType = serviceType,
            adults = _adultCount.value,
            children = _childCount.value,
            concessions = _concessionCount.value
        )
        _currentCalculatedFare.value = fare
    }

    fun advanceToNextStage() {
        val trip = activeTrip.value ?: return
        val route = getCurrentRoute()
        val nextStageNum = (trip.currentStageNumber + 1).coerceAtMost(route.stages.size)
        viewModelScope.launch {
            repository.updateStage(trip.id, nextStageNum)
            val nextStage = route.stages.firstOrNull { it.stageNumber == nextStageNum }
            if (nextStage != null) {
                _selectedOriginStage.value = nextStage
                // Bump dest if it is behind
                val curDest = _selectedDestStage.value
                if (curDest != null && curDest.stageNumber <= nextStageNum) {
                    val newDest = route.stages.getOrNull(nextStageNum) ?: route.stages.last()
                    _selectedDestStage.value = newDest
                }
                recalculateFare(_selectedOriginStage.value, _selectedDestStage.value, ServiceType.valueOf(trip.serviceType))
            }
        }
    }

    fun regressToPrevStage() {
        val trip = activeTrip.value ?: return
        val route = getCurrentRoute()
        val prevStageNum = (trip.currentStageNumber - 1).coerceAtLeast(1)
        viewModelScope.launch {
            repository.updateStage(trip.id, prevStageNum)
            val prevStage = route.stages.firstOrNull { it.stageNumber == prevStageNum }
            if (prevStage != null) {
                _selectedOriginStage.value = prevStage
                recalculateFare(_selectedOriginStage.value, _selectedDestStage.value, ServiceType.valueOf(trip.serviceType))
            }
        }
    }

    /**
     * Issues Cash ticket directly or opens dynamic UPI QR Modal.
     */
    fun processTicketing() {
        val trip = activeTrip.value ?: return
        val origin = _selectedOriginStage.value ?: return
        val dest = _selectedDestStage.value ?: return
        val fare = _currentCalculatedFare.value
        if (fare <= 0.0) return

        if (_paymentMode.value == PaymentMode.CASH) {
            viewModelScope.launch {
                val ticket = repository.issueTicket(
                    trip = trip,
                    originStage = origin,
                    destStage = dest,
                    adultCount = _adultCount.value,
                    childCount = _childCount.value,
                    concessionCount = _concessionCount.value,
                    totalFare = fare,
                    paymentMode = PaymentMode.CASH,
                    paymentCompleted = true,
                    passengerPhone = _passengerPhone.value
                )
                // Soundbox feedback for Cash receipt
                soundboxManager.announcePayment(fare)
                resetTicketForm()
            }
        } else {
            // Initiate Dynamic UPI Flow
            initiateUpiFlow(trip, origin, dest, fare)
        }
    }

    private fun initiateUpiFlow(trip: TripSessionEntity, origin: Stage, dest: Stage, fare: Double) {
        val txnRef = "TXN${System.currentTimeMillis()}${(100..999).random()}"
        val note = "RTC-${trip.routeCode}-S${origin.stageNumber}toS${dest.stageNumber}-A${_adultCount.value}C${_childCount.value}"
        val totalPassengers = (_adultCount.value + _childCount.value + _concessionCount.value).coerceAtLeast(1)
        val pwaUrl = UpiProtocolEngine.buildTicketLinkUrl(
            bus = trip.busRegNumber,
            route = trip.routeCode,
            from = origin.nameEn,
            to = dest.nameEn,
            fare = fare,
            passengers = totalPassengers
        )

        val upiUri = UpiProtocolEngine.buildUpiUri(
            UpiProtocolEngine.UpiParams(
                amount = fare,
                transactionRef = txnRef,
                transactionNote = note,
                pwaTicketUrl = pwaUrl
            )
        )

        // Generate QR code embedding this exact ticket URL
        val qrBitmap = UpiProtocolEngine.generateQrBitmap(pwaUrl, 600)

        _upiModalState.value = UpiModalState(
            isOpen = true,
            upiUri = upiUri,
            qrBitmap = qrBitmap,
            fareAmount = fare,
            txnRef = txnRef,
            pwaUrl = pwaUrl,
            isPaymentReceived = false,
            secondsRemaining = 120
        )

        // Start simulated payment webhook listener
        startPaymentWebhookListener(trip, origin, dest, fare, txnRef)
    }

    private fun startPaymentWebhookListener(
        trip: TripSessionEntity,
        origin: Stage,
        dest: Stage,
        fare: Double,
        txnRef: String
    ) {
        paymentSimulationJob?.cancel()
        paymentSimulationJob = viewModelScope.launch {
            // Count down 120 seconds, or auto-complete after 7 seconds if passenger is quick!
            var count = 120
            while (count > 0 && _upiModalState.value.isOpen && !_upiModalState.value.isPaymentReceived) {
                delay(1000)
                count--
                _upiModalState.value = _upiModalState.value.copy(secondsRemaining = count)

                // Simulate payment success around 6 seconds
                if (count == 114) {
                    triggerPaymentSuccess(trip, origin, dest, fare, txnRef)
                    break
                }
            }
        }
    }

    /**
     * Triggered automatically by simulated bank webhook or on-demand by conductor "Simulate Payment".
     */
    fun triggerPaymentSuccess(
        trip: TripSessionEntity = activeTrip.value!!,
        origin: Stage = _selectedOriginStage.value!!,
        dest: Stage = _selectedDestStage.value!!,
        fare: Double = _upiModalState.value.fareAmount,
        txnRef: String = _upiModalState.value.txnRef
    ) {
        viewModelScope.launch {
            val ticket = repository.issueTicket(
                trip = trip,
                originStage = origin,
                destStage = dest,
                adultCount = _adultCount.value,
                childCount = _childCount.value,
                concessionCount = _concessionCount.value,
                totalFare = fare,
                paymentMode = PaymentMode.UPI,
                paymentCompleted = true,
                passengerPhone = _passengerPhone.value
            )

            _upiModalState.value = _upiModalState.value.copy(
                isPaymentReceived = true,
                issuedTicket = ticket,
                pwaUrl = ticket.pwaUrl
            )

            // Audio soundbox chime & voice confirmation!
            soundboxManager.announcePayment(fare)

            // Delay a brief moment to show success state in modal before closing
            delay(2500)
            closeUpiModal()
            resetTicketForm()
        }
    }

    fun closeUpiModal() {
        paymentSimulationJob?.cancel()
        _upiModalState.value = _upiModalState.value.copy(isOpen = false)
    }

    private fun resetTicketForm() {
        _passengerPhone.value = ""
        // Origin stays at current stage
    }

    // --- Inspector Mode Actions ---
    fun toggleInspectorMode(enable: Boolean) {
        _isInspectorMode.value = enable
        _inspectorVerificationReport.value = null
        _inspectorSearchResults.value = emptyList()
    }

    fun clearInspectorReport() {
        _inspectorVerificationReport.value = null
        _inspectorSearchQuery.value = ""
        _inspectorSearchResults.value = emptyList()
    }

    fun showDummyValidTicketVerification() {
        val trip = activeTrip.value
        val route = getCurrentRoute()
        val currentStageNum = trip?.currentStageNumber ?: 1

        // Dynamically bind to the conductor's active selection in the Ticketing tab
        val originStage = _selectedOriginStage.value
            ?: route.stages.firstOrNull { it.stageNumber == currentStageNum }
            ?: route.stages.first()

        val destStage = _selectedDestStage.value
            ?: route.stages.getOrNull(currentStageNum)
            ?: route.stages.last()

        val totalPassengers = (_adultCount.value + _childCount.value + _concessionCount.value).let {
            if (it > 0) it else 1
        }

        val serviceType = trip?.let {
            try { ServiceType.valueOf(it.serviceType) } catch (_: Exception) { ServiceType.PALLE_VELUGU }
        } ?: ServiceType.PALLE_VELUGU

        val calculatedFare = if (_currentCalculatedFare.value > 0.0) {
            _currentCalculatedFare.value
        } else {
            val calc = TransitPreloads.calculateFare(
                origin = originStage,
                destination = destStage,
                serviceType = serviceType,
                adults = _adultCount.value.coerceAtLeast(1),
                children = _childCount.value,
                concessions = _concessionCount.value
            )
            if (calc > 0.0) calc else 20.0
        }

        val routePrefix = trip?.routeCode?.take(3) ?: "100"
        val timestamp = System.currentTimeMillis()
        val dateStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(java.util.Date(timestamp))
        val timeSuffix = java.text.SimpleDateFormat("HHmmss", java.util.Locale.US).format(java.util.Date(timestamp))
        val serial = (1000..9999).random()
        val dynamicTicketId = "TKT-$routePrefix-$dateStr-$timeSuffix-$serial"

        _inspectorVerificationReport.value = VerificationReport(
            status = VerificationStatus.VALID,
            ticket = null,
            message = "VALID TICKET: $dynamicTicketId • Cryptographically Verified with RTC Master Key.",
            isSignatureAuthentic = true,
            originStage = originStage.nameEn,
            destStage = destStage.nameEn,
            passengers = totalPassengers,
            fare = calculatedFare,
            ticketId = dynamicTicketId
        )
    }

    fun verifyTicketQrPayload(rawPayload: String) {
        val currentStageNum = activeTrip.value?.currentStageNumber ?: 1
        viewModelScope.launch {
            try {
                val report = repository.verifyTicket(rawPayload, currentStageNum)
                _inspectorVerificationReport.value = report
            } catch (e: Throwable) {
                _inspectorVerificationReport.value = VerificationReport(
                    status = VerificationStatus.NOT_FOUND,
                    ticket = null,
                    message = "Invalid Ticket: ${e.localizedMessage ?: "Verification error"}",
                    isSignatureAuthentic = false,
                    originStage = "N/A",
                    destStage = "N/A",
                    passengers = 0,
                    fare = 0.0
                )
            }
        }
    }

    fun searchInspectorTickets(query: String) {
        _inspectorSearchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                _inspectorSearchResults.value = emptyList()
            } else {
                _inspectorSearchResults.value = repository.searchTickets(query)
            }
        }
    }

    // --- Trip Setup ---
    fun startTrip(
        empId: String,
        empName: String,
        busReg: String,
        route: RouteInfo,
        serviceType: ServiceType,
        direction: Direction,
        depotCode: String
    ) {
        viewModelScope.launch {
            repository.startNewTrip(
                empId = empId,
                empName = empName,
                busReg = busReg,
                route = route,
                serviceType = serviceType,
                direction = direction,
                depotCode = depotCode
            )
        }
    }

    fun endTrip() {
        val trip = activeTrip.value ?: return
        viewModelScope.launch {
            repository.closeTrip(trip.id)
        }
    }

    // --- Depot Waybill Export ---
    fun prepareDepotExport() {
        val trip = activeTrip.value ?: return
        viewModelScope.launch {
            val tickets = tripTickets.value
            val json = repository.generateDepotWaybillExport(trip, tickets)
            _depotExportJson.value = json
            val qr = UpiProtocolEngine.generateQrBitmap(json.take(800), 512)
            _depotExportQr.value = qr
        }
    }

    fun clearDepotExport() {
        _depotExportJson.value = null
        _depotExportQr.value = null
    }

    override fun onCleared() {
        super.onCleared()
        soundboxManager.shutdown()
    }
}

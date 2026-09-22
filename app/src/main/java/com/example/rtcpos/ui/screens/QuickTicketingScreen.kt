package com.example.rtcpos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rtcpos.data.entity.TripSessionEntity
import com.example.rtcpos.data.model.PaymentMode
import com.example.rtcpos.data.model.Stage
import com.example.rtcpos.soundbox.SoundboxManager
import com.example.rtcpos.ui.RtcPosViewModel
import com.example.rtcpos.ui.components.GiantStageButton
import com.example.rtcpos.ui.components.PassengerCounterItem

@Composable
fun QuickTicketingScreen(
    viewModel: RtcPosViewModel,
    trip: TripSessionEntity,
    modifier: Modifier = Modifier
) {
    val currentRoute = viewModel.getCurrentRoute()
    val selectedOrigin by viewModel.selectedOriginStage.collectAsState()
    val selectedDest by viewModel.selectedDestStage.collectAsState()
    val adultCount by viewModel.adultCount.collectAsState()
    val childCount by viewModel.childCount.collectAsState()
    val concessionCount by viewModel.concessionCount.collectAsState()
    val paymentMode by viewModel.paymentMode.collectAsState()
    val fare by viewModel.currentCalculatedFare.collectAsState()

    var showStagePickerForOrigin by remember { mutableStateOf(false) }

    LaunchedEffect(trip.id) {
        if (selectedOrigin == null) {
            viewModel.initDefaultStages(trip)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
    ) {
        // High-Contrast Transit Status Header (Bus & Stage Progress Bar)
        Surface(
            color = Color(0xFF1B263B),
            border = BorderStroke(1.dp, Color(0xFF415A77)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = trip.busRegNumber,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD166)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF00E5FF), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = trip.serviceType,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0D1B2A)
                                )
                            }
                        }
                        Text(
                            text = trip.routeName,
                            fontSize = 12.sp,
                            color = Color(0xFFE0E1DD),
                            maxLines = 1
                        )
                    }

                    // Soundbox Audio Tester & Language indicator
                    IconButton(
                        onClick = {
                            // Cycle soundbox language: English -> Telugu -> Hindi
                            val nextLang = when (viewModel.soundboxManager.selectedLanguage) {
                                SoundboxManager.SoundboxLanguage.ENGLISH -> SoundboxManager.SoundboxLanguage.TELUGU
                                SoundboxManager.SoundboxLanguage.TELUGU -> SoundboxManager.SoundboxLanguage.HINDI
                                SoundboxManager.SoundboxLanguage.HINDI -> SoundboxManager.SoundboxLanguage.ENGLISH
                            }
                            viewModel.soundboxManager.announcePayment(fare.coerceAtLeast(50.0), nextLang)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("soundbox_lang_btn"),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFF0D1B2A),
                            contentColor = Color(0xFF00E5FF)
                        )
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Test Audio Soundbox")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bumpy Bus Giant Stage Navigator
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1B2A)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFB703))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { viewModel.regressToPrevStage() },
                            enabled = trip.currentStageNumber > 1,
                            modifier = Modifier.size(42.dp),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFFFD166))
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Prev Stop")
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CURRENT BUS STOP (STAGE ${trip.currentStageNumber})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB703),
                                letterSpacing = 0.5.sp
                            )
                            val currStage = currentRoute.stages.firstOrNull { it.stageNumber == trip.currentStageNumber }
                            Text(
                                text = currStage?.nameEn ?: "Stage ${trip.currentStageNumber}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = { viewModel.advanceToNextStage() },
                            enabled = trip.currentStageNumber < currentRoute.stages.size,
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("next_stage_btn"),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFFFD166))
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next Stop")
                        }
                    }
                }
            }
        }

        // Main Quick-Ticketing Content Scroll
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Origin & Destination Indicator
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Origin Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showStagePickerForOrigin = true },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF415A77))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "FROM (ORIGIN)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF778DA9))
                            Text(
                                text = "S${selectedOrigin?.stageNumber ?: 1} ${selectedOrigin?.nameEn ?: "Select"}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }

                    // Destination Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3B6F)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF00E5FF))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "TO (DESTINATION)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                            Text(
                                text = "S${selectedDest?.stageNumber ?: 2} ${selectedDest?.nameEn ?: "Select"}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Passenger Counters (Adult, Child, Concession)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PassengerCounterItem(
                        label = "ADULT",
                        vernacularLabel = "పెద్దలు",
                        count = adultCount,
                        onIncrement = { viewModel.updatePassengerCounts(adultCount + 1, childCount, concessionCount) },
                        onDecrement = { viewModel.updatePassengerCounts(adultCount - 1, childCount, concessionCount) },
                        modifier = Modifier.weight(1f)
                    )

                    PassengerCounterItem(
                        label = "CHILD",
                        vernacularLabel = "పిల్లలు (50%)",
                        count = childCount,
                        onIncrement = { viewModel.updatePassengerCounts(adultCount, childCount + 1, concessionCount) },
                        onDecrement = { viewModel.updatePassengerCounts(adultCount, childCount - 1, concessionCount) },
                        modifier = Modifier.weight(1f)
                    )

                    PassengerCounterItem(
                        label = "CONCESSION",
                        vernacularLabel = "పాస్ / దివ్యాంగులు",
                        count = concessionCount,
                        onIncrement = { viewModel.updatePassengerCounts(adultCount, childCount, concessionCount + 1) },
                        onDecrement = { viewModel.updatePassengerCounts(adultCount, childCount, concessionCount - 1) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Destination Stage Quick-Select List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT DESTINATION STAGE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                    Text(
                        text = "Tap to calculate fare",
                        fontSize = 11.sp,
                        color = Color(0xFF778DA9)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // List of subsequent stages
            val relevantStages = currentRoute.stages.filter { it.stageNumber >= (selectedOrigin?.stageNumber ?: 1) }
            items(relevantStages) { stage ->
                val isSelected = selectedDest?.stageNumber == stage.stageNumber
                val isCurrent = trip.currentStageNumber == stage.stageNumber

                GiantStageButton(
                    stage = stage,
                    isSelected = isSelected,
                    isCurrentStage = isCurrent,
                    onClick = {
                        if (stage.stageNumber == selectedOrigin?.stageNumber) {
                            // Don't set origin as destination
                        } else {
                            viewModel.setDestStage(stage)
                        }
                    },
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }

        // High-Contrast Bottom Payment & Action Dock
        Surface(
            color = Color(0xFF1B263B),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            border = BorderStroke(1.dp, Color(0xFF415A77)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Fare Total & Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "TOTAL FARE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF778DA9))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = Color(0xFFFFD166), modifier = Modifier.size(26.dp))
                            Text(
                                text = "%.2f".format(fare),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD166)
                            )
                        }
                    }

                    // Payment Mode Toggle Buttons (Cash vs UPI)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.setPaymentMode(PaymentMode.CASH) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (paymentMode == PaymentMode.CASH) Color(0xFF00E676) else Color(0xFF0D1B2A),
                                contentColor = if (paymentMode == PaymentMode.CASH) Color(0xFF0D1B2A) else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.5.dp, if (paymentMode == PaymentMode.CASH) Color.White else Color(0xFF415A77)),
                            modifier = Modifier.testTag("cash_mode_btn")
                        ) {
                            Icon(Icons.Default.LocalAtm, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CASH", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { viewModel.setPaymentMode(PaymentMode.UPI) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (paymentMode == PaymentMode.UPI) Color(0xFF00E5FF) else Color(0xFF0D1B2A),
                                contentColor = if (paymentMode == PaymentMode.UPI) Color(0xFF0D1B2A) else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.5.dp, if (paymentMode == PaymentMode.UPI) Color.White else Color(0xFF415A77)),
                            modifier = Modifier.testTag("upi_mode_btn")
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("UPI QR", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Big Tactile Issuance Button
                Button(
                    onClick = { viewModel.processTicketing() },
                    enabled = fare > 0.0 && (adultCount + childCount + concessionCount) > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("issue_ticket_action_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (paymentMode == PaymentMode.UPI) Color(0xFF00E5FF) else Color(0xFF00E676),
                        contentColor = Color(0xFF0D1B2A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (paymentMode == PaymentMode.UPI) Icons.Default.QrCode2 else Icons.Default.LocalAtm,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (paymentMode == PaymentMode.UPI) "GENERATE DYNAMIC UPI QR (₹${fare.toInt()})" else "COLLECT CASH & ISSUE TICKET (₹${fare.toInt()})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

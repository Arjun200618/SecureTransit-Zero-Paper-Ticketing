package com.example.rtcpos.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rtcpos.data.entity.TripSessionEntity
import com.example.rtcpos.ui.RtcPosViewModel

@Composable
fun DepotWaybillScreen(
    viewModel: RtcPosViewModel,
    trip: TripSessionEntity,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val waybillStats by viewModel.waybillStats.collectAsState()
    val tickets by viewModel.tripTickets.collectAsState()
    val exportJson by viewModel.depotExportJson.collectAsState()
    val exportQr by viewModel.depotExportQr.collectAsState()

    var showEndTripDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
    ) {
        // Header
        Surface(
            color = Color(0xFF1B263B),
            border = BorderStroke(1.dp, Color(0xFF415A77)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Ticketing")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "DEPOT WAYBILL SUMMARY",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00E5FF)
                        )
                        Text(
                            text = "${trip.busRegNumber} • Shift Waybill Audit",
                            fontSize = 11.sp,
                            color = Color(0xFF778DA9)
                        )
                    }
                }

                // End Shift Button
                Button(
                    onClick = { showEndTripDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF1744),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("end_trip_btn")
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("END SHIFT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(14.dp)
        ) {
            // Main Revenue Stat Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Revenue
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3B6F)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "TOTAL REVENUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = Color(0xFFFFD166), modifier = Modifier.size(20.dp))
                                Text(
                                    text = "%.0f".format(waybillStats.totalRevenue),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD166)
                                )
                            }
                            Text(text = "${waybillStats.totalTickets} Tickets Issued", fontSize = 11.sp, color = Color(0xFFE0E1DD))
                        }
                    }

                    // Total Passengers
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF415A77)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "PASSENGERS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF778DA9))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.People, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${waybillStats.totalPassengers}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Text(text = "Occupancy Count", fontSize = 11.sp, color = Color(0xFF778DA9))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Cash vs UPI Breakdown Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF415A77)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "PAYMENT METHOD RECONCILIATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD166)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Cash in Bag
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Physical Cash in Bag:", fontSize = 13.sp, color = Color.White)
                            Text(
                                text = "₹%.2f".format(waybillStats.cashRevenue),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00E676)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Verified UPI
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Verified Digital UPI (NPCI):", fontSize = 13.sp, color = Color.White)
                            Text(
                                text = "₹%.2f".format(waybillStats.upiRevenue),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00E5FF)
                            )
                        }

                        if (waybillStats.pendingUpiCount > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Pending/Unsettled UPI:", fontSize = 13.sp, color = Color(0xFFFF1744))
                                Text(
                                    text = "₹%.2f (${waybillStats.pendingUpiCount})".format(waybillStats.pendingUpiAmount),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF1744)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Export Encrypted Waybill for Depot Docking
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2239)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "DEPOT DOCKING SYNCHRONIZATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00E5FF)
                        )
                        Text(
                            text = "Export cryptographically sealed SQLite manifest via High-Density QR or JSON",
                            fontSize = 11.sp,
                            color = Color(0xFF778DA9)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.prepareDepotExport() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color(0xFF0D1B2A)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("export_waybill_btn")
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GENERATE DEPOT SYNC QR & MANIFEST", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        exportQr?.let { qrBitmap ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = "Depot Waybill Sync QR",
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .size(200.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Scan at Depot Docking Terminal to settle shift revenue",
                                    fontSize = 10.sp,
                                    color = Color(0xFF00E5FF)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        exportJson?.let { json ->
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Waybill JSON", json))
                                            Toast.makeText(context, "Waybill JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    border = BorderStroke(1.dp, Color(0xFF00E5FF))
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Encrypted JSON", color = Color(0xFF00E5FF), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Waybill Passenger Manifest
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT ISSUED TICKETS (${tickets.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Real-time SQLite Log",
                        fontSize = 11.sp,
                        color = Color(0xFF778DA9)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            items(tickets) { t ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF415A77)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = t.ticketId,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (t.paymentMode == "UPI") Color(0xFF00E5FF) else Color(0xFF00E676),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = t.paymentMode,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0D1B2A)
                                    )
                                }
                            }
                            Text(
                                text = "${t.originStageName} → ${t.destStageName}",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${t.adultCount}A, ${t.childCount}C, ${t.concessionCount}Pass • Sig: ${t.ed25519Signature.take(12)}...",
                                fontSize = 10.sp,
                                color = Color(0xFF778DA9),
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "₹${t.totalFare.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD166)
                        )
                    }
                }
            }
        }
    }

    if (showEndTripDialog) {
        AlertDialog(
            onDismissRequest = { showEndTripDialog = false },
            title = { Text("End Shift & Close Waybill?", color = Color.White) },
            text = {
                Text(
                    "This will close the active trip session for ${trip.busRegNumber}. Ensure your cash bag of ₹%.2f matches the manifest before depot handover."
                        .format(waybillStats.cashRevenue),
                    color = Color(0xFFE0E1DD)
                )
            },
            containerColor = Color(0xFF1B263B),
            confirmButton = {
                Button(
                    onClick = {
                        showEndTripDialog = false
                        viewModel.endTrip()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744))
                ) {
                    Text("CLOSE WAYBILL", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTripDialog = false }) {
                    Text("CANCEL", color = Color(0xFF778DA9))
                }
            }
        )
    }
}

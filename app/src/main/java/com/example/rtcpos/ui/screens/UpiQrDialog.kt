package com.example.rtcpos.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SimCardDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rtcpos.ui.RtcPosViewModel

@Composable
fun UpiQrDialog(
    state: RtcPosViewModel.UpiModalState,
    onSimulatePayment: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.isOpen) return

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0D1B2A),
            border = BorderStroke(2.dp, if (state.isPaymentReceived) Color(0xFF00E676) else Color(0xFF00E5FF))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DYNAMIC UPI PAYMENT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00E5FF),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "NPCI Deep-Link Protocol v1.6",
                            fontSize = 11.sp,
                            color = Color(0xFF778DA9)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // High-Contrast QR Code Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(14.dp)
                            .size(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        state.qrBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Dynamic UPI QR Code",
                                modifier = Modifier.size(230.dp)
                            )
                        } ?: run {
                            CircularProgressIndicator(color = Color(0xFF0D1B2A))
                        }

                        // Overlay checkmark on success
                        if (state.isPaymentReceived) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .background(Color(0xFF00E676).copy(alpha = 0.95f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color(0xFF0D1B2A),
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }
                }

                // Fare Amount Display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        Icons.Default.CurrencyRupee,
                        contentDescription = null,
                        tint = if (state.isPaymentReceived) Color(0xFF00E676) else Color(0xFFFFD166),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "%.2f".format(state.fareAmount),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = if (state.isPaymentReceived) Color(0xFF00E676) else Color(0xFFFFD166)
                    )
                }

                // Webhook & Status Pill
                AnimatedVisibility(visible = !state.isPaymentReceived) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFF1B263B), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.Sensors,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Listening for Bank Webhook (${state.secondsRemaining}s)",
                                fontSize = 12.sp,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Simulation button to instantly test payment callback
                        Button(
                            onClick = onSimulatePayment,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E676),
                                contentColor = Color(0xFF0D1B2A)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("simulate_payment_btn")
                        ) {
                            Icon(Icons.Default.SimCardDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SIMULATE PASSENGER UPI PAYMENT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Success Message & PWA Details
                AnimatedVisibility(visible = state.isPaymentReceived) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            color = Color(0xFF00E676).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF00E676)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color(0xFF00E676))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SOUNDBOX CONFIRMATION PLAYED",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = Color(0xFF00E676)
                                    )
                                }
                                Text(
                                    text = "Ticket signed with Ed25519 & pushed to Passenger PWA URL",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Open PWA Ticket Link
                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.pwaUrl))
                                    context.startActivity(intent)
                                } catch (ignored: Exception) {}
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Passenger PWA Ticket", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Raw NPCI UPI URI & Ticket Specs
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        if (state.pwaUrl.isNotEmpty()) {
                            Text(
                                text = "EMBEDDED TICKET QR URL:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                            Text(
                                text = state.pwaUrl,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF80DEEA),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        Text(
                            text = "NPCI URI PAYLOAD:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF778DA9)
                        )
                        Text(
                            text = state.upiUri,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE0E1DD),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

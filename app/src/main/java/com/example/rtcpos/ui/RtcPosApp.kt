package com.example.rtcpos.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rtcpos.ui.screens.DepotWaybillScreen
import com.example.rtcpos.ui.screens.InspectorScreen
import com.example.rtcpos.ui.screens.QuickTicketingScreen
import com.example.rtcpos.ui.screens.TripSetupScreen
import com.example.rtcpos.ui.screens.UpiQrDialog

enum class PosTab(val title: String) {
    TICKETING("Ticketing"),
    INSPECTOR("Inspector Squad"),
    WAYBILL("Depot Waybill")
}

@Composable
fun RtcPosApp(
    viewModel: RtcPosViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val activeTrip by viewModel.activeTrip.collectAsState()
    val upiModalState by viewModel.upiModalState.collectAsState()
    var currentTab by remember { mutableStateOf(PosTab.TICKETING) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = Color(0xFF0D1B2A),
        bottomBar = {
            if (activeTrip != null) {
                NavigationBar(
                    containerColor = Color(0xFF1B263B),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == PosTab.TICKETING,
                        onClick = { currentTab = PosTab.TICKETING },
                        icon = {
                            Icon(
                                Icons.Default.DirectionsBus,
                                contentDescription = "Ticketing",
                                tint = if (currentTab == PosTab.TICKETING) Color(0xFF0D1B2A) else Color(0xFF778DA9)
                            )
                        },
                        label = {
                            Text(
                                "Ticketing",
                                fontSize = 11.sp,
                                fontWeight = if (currentTab == PosTab.TICKETING) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0D1B2A),
                            indicatorColor = Color(0xFF00E5FF),
                            selectedTextColor = Color(0xFF00E5FF),
                            unselectedTextColor = Color(0xFF778DA9)
                        ),
                        modifier = Modifier.testTag("nav_ticketing")
                    )

                    NavigationBarItem(
                        selected = currentTab == PosTab.INSPECTOR,
                        onClick = { currentTab = PosTab.INSPECTOR },
                        icon = {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = "Squad Inspector",
                                tint = if (currentTab == PosTab.INSPECTOR) Color(0xFF0D1B2A) else Color(0xFF778DA9)
                            )
                        },
                        label = {
                            Text(
                                "Inspector Squad",
                                fontSize = 11.sp,
                                fontWeight = if (currentTab == PosTab.INSPECTOR) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0D1B2A),
                            indicatorColor = Color(0xFFFFB703),
                            selectedTextColor = Color(0xFFFFB703),
                            unselectedTextColor = Color(0xFF778DA9)
                        ),
                        modifier = Modifier.testTag("nav_inspector")
                    )

                    NavigationBarItem(
                        selected = currentTab == PosTab.WAYBILL,
                        onClick = { currentTab = PosTab.WAYBILL },
                        icon = {
                            Icon(
                                Icons.Default.Assessment,
                                contentDescription = "Depot Waybill",
                                tint = if (currentTab == PosTab.WAYBILL) Color(0xFF0D1B2A) else Color(0xFF778DA9)
                            )
                        },
                        label = {
                            Text(
                                "Waybill",
                                fontSize = 11.sp,
                                fontWeight = if (currentTab == PosTab.WAYBILL) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0D1B2A),
                            indicatorColor = Color(0xFF00E676),
                            selectedTextColor = Color(0xFF00E676),
                            unselectedTextColor = Color(0xFF778DA9)
                        ),
                        modifier = Modifier.testTag("nav_waybill")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val trip = activeTrip
            if (trip == null) {
                // Trip Setup & Depot Initialization Screen
                TripSetupScreen(
                    onStartTrip = { empId, empName, busReg, route, serviceType, direction, depotCode ->
                        viewModel.startTrip(empId, empName, busReg, route, serviceType, direction, depotCode)
                    }
                )
            } else {
                when (currentTab) {
                    PosTab.TICKETING -> {
                        QuickTicketingScreen(
                            viewModel = viewModel,
                            trip = trip
                        )
                    }
                    PosTab.INSPECTOR -> {
                        InspectorScreen(
                            viewModel = viewModel,
                            onExitInspectorMode = { currentTab = PosTab.TICKETING }
                        )
                    }
                    PosTab.WAYBILL -> {
                        DepotWaybillScreen(
                            viewModel = viewModel,
                            trip = trip,
                            onBack = { currentTab = PosTab.TICKETING }
                        )
                    }
                }
            }

            // Dynamic UPI QR Modal
            UpiQrDialog(
                state = upiModalState,
                onSimulatePayment = { viewModel.triggerPaymentSuccess() },
                onDismiss = { viewModel.closeUpiModal() }
            )
        }
    }
}

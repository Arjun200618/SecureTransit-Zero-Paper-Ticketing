package com.example.rtcpos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rtcpos.data.model.Direction
import com.example.rtcpos.data.model.RouteInfo
import com.example.rtcpos.data.model.ServiceType
import com.example.rtcpos.data.model.TransitPreloads

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripSetupScreen(
    onStartTrip: (
        empId: String,
        empName: String,
        busReg: String,
        route: RouteInfo,
        serviceType: ServiceType,
        direction: Direction,
        depotCode: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var empId by remember { mutableStateOf("RTC-EMP-84210") }
    var empName by remember { mutableStateOf("R. Venkateshwarlu") }
    var busReg by remember { mutableStateOf("AP 29 Z 4512") }
    var selectedRoute by remember { mutableStateOf(TransitPreloads.sampleRoutes[0]) }
    var selectedService by remember { mutableStateOf(ServiceType.EXPRESS) }
    var selectedDirection by remember { mutableStateOf(Direction.UP) }
    var depotCode by remember { mutableStateOf("DEPOT-MGBS-01") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Depot Header Badge
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF415A77)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF00E5FF), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DirectionsBus,
                        contentDescription = "RTC POS",
                        tint = Color(0xFF0D1B2A),
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "INDIAN STATE RTC POS TERMINAL",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00E5FF),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Electronic Ticketing & Dynamic UPI Engine",
                        fontSize = 12.sp,
                        color = Color(0xFFE0E1DD)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TRIP INITIALIZATION & DEPOT SIGN-IN",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD166),
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Conductor Info Row
        OutlinedTextField(
            value = empId,
            onValueChange = { empId = it },
            label = { Text("Conductor Employee ID") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF00E5FF)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF415A77),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF00E5FF),
                unfocusedLabelColor = Color(0xFF778DA9)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("conductor_emp_id_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = empName,
                onValueChange = { empName = it },
                label = { Text("Conductor Name") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF415A77),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            )

            OutlinedTextField(
                value = busReg,
                onValueChange = { busReg = it },
                label = { Text("Bus Reg No.") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF415A77),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
                    .testTag("bus_reg_input")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Route Selection
        Text(
            text = "Select Operating Route",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(6.dp))

        TransitPreloads.sampleRoutes.forEach { route ->
            val isSelected = route.routeCode == selectedRoute.routeCode
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { selectedRoute = route }
                    .testTag("route_card_${route.routeCode}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF1B3B6F) else Color(0xFF1B263B)
                ),
                border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF415A77)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = route.routeName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isSelected) Color(0xFF00E5FF) else Color.White
                        )
                        Text(
                            text = "${route.stages.size} Stages • Depot: ${route.depotName}",
                            fontSize = 12.sp,
                            color = Color(0xFF778DA9)
                        )
                    }
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color(0xFF00E5FF))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Service Type Selection
        Text(
            text = "Service Type",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ServiceType.values().take(2).forEach { s ->
                val isSelected = s == selectedService
                Button(
                    onClick = { selectedService = s },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF00E5FF) else Color(0xFF1B263B)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) Color.White else Color(0xFF415A77))
                ) {
                    Text(
                        text = s.title.substringBefore(" ("),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF0D1B2A) else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ServiceType.values().drop(2).forEach { s ->
                val isSelected = s == selectedService
                Button(
                    onClick = { selectedService = s },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF00E5FF) else Color(0xFF1B263B)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) Color.White else Color(0xFF415A77))
                ) {
                    Text(
                        text = s.title.substringBefore(" ("),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF0D1B2A) else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Direction Toggle
        Text(
            text = "Trip Direction",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Direction.values().forEach { d ->
                val isSelected = d == selectedDirection
                Button(
                    onClick = { selectedDirection = d },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFFFFB703) else Color(0xFF1B263B)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) Color.White else Color(0xFF415A77))
                ) {
                    Text(
                        text = d.label.substringBefore(" ("),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF0D1B2A) else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Offline Status Banner
        Surface(
            color = Color(0xFF0A2239),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF1D4E89)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF00E5FF))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Offline SQLite Manifest Ready",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Ed25519 Depot Master Keys and ${selectedRoute.stages.size} stages loaded",
                        fontSize = 11.sp,
                        color = Color(0xFF778DA9)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Start Trip Button
        Button(
            onClick = {
                onStartTrip(
                    empId,
                    empName,
                    busReg,
                    selectedRoute,
                    selectedService,
                    selectedDirection,
                    depotCode
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_trip_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00E5FF),
                contentColor = Color(0xFF0D1B2A)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "INITIALIZE TRIP & LAUNCH POS",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

package com.example.rtcpos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rtcpos.data.model.Stage

@Composable
fun PassengerCounterItem(
    label: String,
    vernacularLabel: String,
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF415A77))
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = vernacularLabel,
                fontSize = 10.sp,
                color = Color(0xFF778DA9)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("decrement_${label.lowercase()}"),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFF0D1B2A),
                        contentColor = Color(0xFFFFD166)
                    )
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease $label")
                }

                Text(
                    text = "$count",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = if (count > 0) Color(0xFF00E5FF) else Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(36.dp)
                )

                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("increment_${label.lowercase()}"),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color(0xFF0D1B2A)
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase $label")
                }
            }
        }
    }
}

@Composable
fun GiantStageButton(
    stage: Stage,
    isSelected: Boolean,
    isCurrentStage: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected -> Color(0xFF00E5FF)
        isCurrentStage -> Color(0xFFFFB703)
        else -> Color(0xFF1B263B)
    }

    val contentColor = when {
        isSelected -> Color(0xFF0D1B2A)
        isCurrentStage -> Color(0xFF0D1B2A)
        else -> Color.White
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable { onClick() }
            .testTag("stage_btn_${stage.stageNumber}"),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(10.dp),
        border = if (isSelected || isCurrentStage) BorderStroke(2.dp, Color.White) else BorderStroke(1.dp, Color(0xFF415A77))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Stage Number Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isSelected || isCurrentStage) Color(0xFF0D1B2A) else Color(0xFF415A77),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S${stage.stageNumber}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = if (isSelected) Color(0xFF00E5FF) else if (isCurrentStage) Color(0xFFFFB703) else Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = stage.nameEn,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stage.nameVernacular,
                        fontSize = 12.sp,
                        color = if (isSelected || isCurrentStage) Color(0xFF1B263B) else Color(0xFF778DA9),
                        maxLines = 1
                    )
                }
            }

            // Distance Tag
            Text(
                text = "${stage.distanceKm.toInt()} km",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

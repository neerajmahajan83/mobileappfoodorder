package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSecondary
import com.example.ui.theme.ExpressPurple
import com.example.ui.theme.WarningOrange
import com.example.util.CutOffStatus
import com.example.util.MealSlot
import com.example.util.MealSlotStatusInfo

@Composable
fun CountdownBanner(
    statusInfo: MealSlotStatusInfo,
    onInfoClicked: () -> Unit = {}
) {
    val isExpress = statusInfo.status == CutOffStatus.LATE_EXPRESS
    val isClosed = statusInfo.status == CutOffStatus.CLOSED

    val bgBrush = when {
        isExpress -> Brush.horizontalGradient(
            colors = listOf(Color(0xFF4A148C), Color(0xFF6A1B9A))
        )
        isClosed -> Brush.horizontalGradient(
            colors = listOf(Color(0xFF37474F), Color(0xFF263238))
        )
        else -> Brush.horizontalGradient(
            colors = listOf(Color(0xFFBF360C), Color(0xFFD84315), Color(0xFFE65100))
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("countdown_banner_card"),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgBrush)
                .padding(16.dp)
        ) {
            Column {
                // Top row: Slot title & Status Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (statusInfo.slot) {
                                MealSlot.BREAKFAST -> Icons.Default.WbSunny
                                MealSlot.LUNCH -> Icons.Default.LunchDining
                                MealSlot.DINNER -> Icons.Default.DinnerDining
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${statusInfo.slot.title} Cut-Off Window",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    // Status Pill
                    Surface(
                        color = when {
                            isExpress -> Color(0xFFFFB300)
                            isClosed -> Color(0xFF90A4AE)
                            else -> Color(0xFF00E676)
                        },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = when {
                                isExpress -> "EXPRESS ACTIVE"
                                isClosed -> "BATCH LOCKED"
                                else -> "OPEN FOR PRE-ORDER"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Main Countdown / Status Text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isExpress) Icons.Default.Bolt else Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (isExpress) Color(0xFFFFE082) else Color(0xFFFFCC80),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isExpress) "Late Express Window" else statusInfo.timeRemainingText,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Cut-Off Time: ${statusInfo.slot.cutOffDisplay}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))

                // Operational Note & Delivery Window
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusInfo.deliveryNotice,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.95f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }

                    if (isExpress) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "+$2.50 Late Fee",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFE082)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

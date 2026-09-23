package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OrderEntity
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSecondary
import com.example.ui.theme.SuccessGreen

@Composable
fun BatchRouteMap(
    order: OrderEntity,
    modifier: Modifier = Modifier
) {
    // Pulse animation for live driver ping
    val infiniteTransition = rememberInfiniteTransition(label = "driver_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("batch_route_map_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Live Dispatch Badge & Stops
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Live Batch Van #12",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Surface(
                    color = CoralSecondary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Stop ${order.batchStopNumber} of ${order.totalBatchStops} on Route",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CoralSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Schematic Map Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEFEBE9))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Grid street lines simulation
                    val gridColor = Color(0xFFDDD4CF)
                    for (x in 40..w.toInt() step 60) {
                        drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), h), strokeWidth = 2f)
                    }
                    for (y in 30..h.toInt() step 50) {
                        drawLine(gridColor, Offset(0f, y.toFloat()), Offset(w, y.toFloat()), strokeWidth = 2f)
                    }

                    // Route Points:
                    // Point 0: Central Kitchen (Hub)
                    val p0 = Offset(w * 0.12f, h * 0.75f)
                    // Point 1: Batch Drop 1 (Finance Tower)
                    val p1 = Offset(w * 0.32f, h * 0.35f)
                    // Point 2: Batch Drop 2 (Biotech Plaza)
                    val p2 = Offset(w * 0.55f, h * 0.65f)
                    // Point 3: Customer Desk (Tech Park Tower B - YOU)
                    val p3 = Offset(w * 0.78f, h * 0.25f)
                    // Point 4: Next Stop
                    val p4 = Offset(w * 0.92f, h * 0.55f)

                    // Draw Planned Batch Route Path
                    val path = Path().apply {
                        moveTo(p0.x, p0.y)
                        lineTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        lineTo(p3.x, p3.y)
                        lineTo(p4.x, p4.y)
                    }

                    // Background route outline
                    drawPath(
                        path = path,
                        color = Color(0xFFB0BEC5),
                        style = Stroke(width = 8f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f))
                    )

                    // Active completed route segment up to driver
                    val progress = order.routeProgress.coerceIn(0f, 1f)
                    val currentDriverPos = when {
                        progress < 0.3f -> {
                            val t = progress / 0.3f
                            Offset(p0.x + (p1.x - p0.x) * t, p0.y + (p1.y - p0.y) * t)
                        }
                        progress < 0.65f -> {
                            val t = (progress - 0.3f) / 0.35f
                            Offset(p1.x + (p2.x - p1.x) * t, p1.y + (p2.y - p1.y) * t)
                        }
                        progress < 0.95f -> {
                            val t = (progress - 0.65f) / 0.30f
                            Offset(p2.x + (p3.x - p2.x) * t, p2.y + (p3.y - p2.y) * t)
                        }
                        else -> {
                            val t = (progress - 0.95f) / 0.05f
                            Offset(p3.x + (p4.x - p3.x) * t, p3.y + (p4.y - p3.y) * t)
                        }
                    }

                    // Draw stop pins
                    drawCircle(color = Color(0xFF5D4037), radius = 10f, center = p0) // Kitchen
                    drawCircle(color = Color(0xFF78909C), radius = 7f, center = p1)  // Stop 1
                    drawCircle(color = Color(0xFF78909C), radius = 7f, center = p2)  // Stop 2
                    drawCircle(color = CoralPrimary, radius = 12f, center = p3)      // YOU (Stop 3)
                    drawCircle(color = Color.White, radius = 5f, center = p3)
                    drawCircle(color = Color(0xFF78909C), radius = 7f, center = p4)  // Stop 4

                    // Draw driver pulse & driver icon marker
                    drawCircle(
                        color = SuccessGreen.copy(alpha = pulseAlpha),
                        radius = 20f * pulseScale,
                        center = currentDriverPos
                    )
                    drawCircle(
                        color = SuccessGreen,
                        radius = 12f,
                        center = currentDriverPos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = currentDriverPos
                    )
                }

                // Overlay Labels on Map
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🍳 Cloud Kitchen Hub",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(CoralPrimary.copy(alpha = 0.95f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "📍 YOUR DESK (Stop #3)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Driver & Vehicle Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Courier",
                            tint = CoralPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = order.driverName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${order.driverVehicle} • ETA: 12:18 PM",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { /* Call driver simulation */ },
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Delivery instructions preview
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MeetingRoom,
                    contentDescription = null,
                    tint = CoralSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = order.deliveryDesk,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

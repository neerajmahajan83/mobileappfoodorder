package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OrderEntity
import com.example.ui.components.BatchRouteMap
import com.example.ui.components.RaiseIssueDialog
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.PreBiteViewModel

@Composable
fun TrackingScreen(
    viewModel: PreBiteViewModel,
    onNavigateToSupport: () -> Unit
) {
    val orders by viewModel.allOrders.collectAsState()

    var selectedOrderForIssue by remember { mutableStateOf<String?>(null) }
    var showIssueDialog by remember { mutableStateOf(false) }

    // Active orders vs Delivered past orders
    val activeOrders = remember(orders) {
        orders.filter { it.status != "DELIVERED" && it.status != "CANCELLED" }
    }
    val pastOrders = remember(orders) {
        orders.filter { it.status == "DELIVERED" || it.status == "CANCELLED" }
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: Active, 1: History

    if (showIssueDialog && selectedOrderForIssue != null) {
        RaiseIssueDialog(
            orderId = selectedOrderForIssue!!,
            onDismiss = { showIssueDialog = false },
            onSubmit = { type, subject, desc ->
                viewModel.submitSupportTicket(
                    orderId = selectedOrderForIssue!!,
                    issueType = type,
                    subject = subject,
                    description = desc,
                    onSubmitted = {
                        showIssueDialog = false
                        onNavigateToSupport()
                    }
                )
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tracking_screen")
    ) {
        // Tab Header: Active Batches vs History
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CoralPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Active Batches")
                        if (activeOrders.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Badge(containerColor = CoralPrimary) {
                                Text("${activeOrders.size}", color = Color.White)
                            }
                        }
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Order History (${pastOrders.size})") }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedTab == 0) {
                if (activeOrders.isEmpty()) {
                    item {
                        EmptyOrdersPlaceholder(
                            message = "No active scheduled batches right now. Pre-order your next meal from the Dashboard!",
                            buttonText = "Explore Today's Menu",
                            onClick = { viewModel.setScreen("home") }
                        )
                    }
                } else {
                    items(activeOrders, key = { it.orderId }) { order ->
                        ActiveOrderCard(
                            order = order,
                            onRaiseIssue = {
                                selectedOrderForIssue = order.orderId
                                showIssueDialog = true
                            }
                        )
                    }
                }
            } else {
                if (pastOrders.isEmpty()) {
                    item {
                        EmptyOrdersPlaceholder(
                            message = "Your completed batch orders will show up here.",
                            buttonText = "Order Now",
                            onClick = { viewModel.setScreen("home") }
                        )
                    }
                } else {
                    items(pastOrders, key = { it.orderId }) { order ->
                        PastOrderCard(
                            order = order,
                            onRaiseIssue = {
                                selectedOrderForIssue = order.orderId
                                showIssueDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveOrderCard(
    order: OrderEntity,
    onRaiseIssue: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_order_card_${order.orderId}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Order ID & Meal Slot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.orderId}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${order.mealType} Batch • ${order.targetDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoralSecondary
                    )
                }

                Surface(
                    color = CoralPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.deliveryTimeWindow,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CoralPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4-Step Status Timeline
            OrderStatusTimeline(currentStatus = order.status)

            Spacer(modifier = Modifier.height(16.dp))

            // Batch Route Map Canvas (if out for delivery or confirmed)
            BatchRouteMap(order = order)

            Spacer(modifier = Modifier.height(14.dp))

            // Items breakdown
            Text(
                text = "Items in this Batch",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = order.itemsSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions: Total & Raise Issue Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Paid", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "$${String.format("%.2f", order.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CoralPrimary
                        )
                    )
                }

                OutlinedButton(
                    onClick = onRaiseIssue,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralPrimary),
                    modifier = Modifier.testTag("raise_issue_btn_${order.orderId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Raise an Issue", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun PastOrderCard(
    order: OrderEntity,
    onRaiseIssue: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("past_order_card_${order.orderId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.orderId}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${order.mealType} • ${order.targetDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = SuccessGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Delivered",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = order.itemsSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${String.format("%.2f", order.totalAmount)} • ${order.paymentMethod}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                TextButton(
                    onClick = onRaiseIssue,
                    modifier = Modifier.testTag("report_past_order_${order.orderId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = CoralPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Report Issue",
                        style = MaterialTheme.typography.labelMedium.copy(color = CoralPrimary)
                    )
                }
            }
        }
    }
}

@Composable
fun OrderStatusTimeline(currentStatus: String) {
    val steps = listOf(
        "CONFIRMED" to "Batch Confirmed",
        "BATCHING_PREP" to "Kitchen Prep",
        "OUT_FOR_DELIVERY" to "Van Dispatched",
        "DELIVERED" to "Delivered to Desk"
    )

    val activeIndex = when (currentStatus) {
        "CONFIRMED" -> 0
        "BATCHING_PREP" -> 1
        "OUT_FOR_DELIVERY" -> 2
        "DELIVERED" -> 3
        else -> 0
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (_, label) ->
            val isCompleted = index <= activeIndex
            val isCurrent = index == activeIndex

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> CoralPrimary
                                isCompleted -> SuccessGreen
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted && !isCurrent) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    } else if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) CoralPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun EmptyOrdersPlaceholder(
    message: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.LunchDining,
            contentDescription = null,
            tint = CoralSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Orders in View",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
        ) {
            Text(buttonText)
        }
    }
}

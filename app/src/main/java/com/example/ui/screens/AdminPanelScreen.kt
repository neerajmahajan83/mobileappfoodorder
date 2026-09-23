package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MenuItemEntity
import com.example.data.local.OrderEntity
import com.example.data.local.SupportTicketEntity
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.PreBiteViewModel
import com.example.util.MealSlot

@Composable
fun AdminPanelScreen(viewModel: PreBiteViewModel) {
    val context = LocalContext.current
    var adminTab by remember { mutableStateOf(0) } // 0: Process Orders & Payment, 1: Kitchen Batching (9:01 AM), 2: Menu Management, 3: Help & Tickets, 4: Business Insights

    val orders by viewModel.allOrders.collectAsState()
    val menuItems by viewModel.allMenuItems.collectAsState()
    val tickets by viewModel.allTickets.collectAsState()
    val selectedSlot by viewModel.selectedMealSlot.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var selectedTicketForResolution by remember { mutableStateOf<SupportTicketEntity?>(null) }

    if (showAddItemDialog) {
        AddNewDishDialog(
            currentSlot = selectedSlot,
            onDismiss = { showAddItemDialog = false },
            onAdd = { name, price, cat, slot, rest, cal, pro, desc ->
                viewModel.addNewMenuItem(name, price, cat, slot, rest, cal, pro, desc)
                showAddItemDialog = false
                Toast.makeText(context, "Dish '$name' added to menu!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (selectedTicketForResolution != null) {
        ResolveTicketDialog(
            ticket = selectedTicketForResolution!!,
            onDismiss = { selectedTicketForResolution = null },
            onResolve = { id, resp, cred ->
                viewModel.resolveTicket(id, resp, cred)
                selectedTicketForResolution = null
                Toast.makeText(context, "Ticket #$id resolved with $$cred credit!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_panel_screen")
    ) {
        // Top Admin Tabs
        ScrollableTabRow(
            selectedTabIndex = adminTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CoralPrimary,
            edgePadding = 16.dp
        ) {
            Tab(
                selected = adminTab == 0,
                onClick = { adminTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Process Orders & Payments")
                        val pendingCount = orders.count { it.paymentStatus == "PENDING_VERIFICATION" }
                        if (pendingCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Badge(containerColor = Color(0xFFD84315)) { Text("$pendingCount") }
                        }
                    }
                }
            )
            Tab(
                selected = adminTab == 1,
                onClick = { adminTab = 1 },
                text = { Text("Kitchen Batching (9:01 AM)") }
            )
            Tab(
                selected = adminTab == 2,
                onClick = { adminTab = 2 },
                text = { Text("Menu (${menuItems.size})") }
            )
            Tab(
                selected = adminTab == 3,
                onClick = { adminTab = 3 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Help & Tickets")
                        val openTickets = tickets.count { it.status == "OPEN" }
                        if (openTickets > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Badge(containerColor = CoralPrimary) { Text("$openTickets") }
                        }
                    }
                }
            )
            Tab(
                selected = adminTab == 4,
                onClick = { adminTab = 4 },
                text = { Text("Business Insights") }
            )
        }

        when (adminTab) {
            0 -> ProcessOrdersAndPaymentsTab(
                orders = orders,
                onUpdateOrderStatus = { id, s -> viewModel.updateOrderStatus(id, s) },
                onUpdatePaymentStatus = { id, p -> viewModel.updateOrderPaymentStatus(id, p) },
                onDeleteOrder = { viewModel.deleteOrder(it) }
            )
            1 -> KitchenBatchingTab(
                orders = orders,
                currentSlot = selectedSlot,
                onUpdateStatus = { id, s -> viewModel.updateOrderStatus(id, s) }
            )
            2 -> MenuManagementTab(
                items = menuItems,
                onToggleAvailability = { id, avail -> viewModel.toggleMenuItemAvailability(id, avail) },
                onDelete = { viewModel.deleteMenuItem(it) },
                onAddNewClicked = { showAddItemDialog = true }
            )
            3 -> HelpAndTicketsTab(
                tickets = tickets,
                onSelectToResolve = { selectedTicketForResolution = it }
            )
            4 -> BusinessEconomicsTab()
        }
    }
}

// ----------------- TAB 0: Process Orders & Payment Status -----------------
@Composable
private fun ProcessOrdersAndPaymentsTab(
    orders: List<OrderEntity>,
    onUpdateOrderStatus: (String, String) -> Unit,
    onUpdatePaymentStatus: (String, String) -> Unit,
    onDeleteOrder: (String) -> Unit
) {
    var filterMealSlot by remember { mutableStateOf("ALL") }
    var filterPaymentStatus by remember { mutableStateOf("ALL") }

    val filteredOrders = remember(orders, filterMealSlot, filterPaymentStatus) {
        orders.filter { order ->
            val slotMatch = filterMealSlot == "ALL" || order.mealType == filterMealSlot
            val payMatch = filterPaymentStatus == "ALL" || order.paymentStatus == filterPaymentStatus
            slotMatch && payMatch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Orders & Payment Processing Center",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Manage kitchen orders, confirm online payments from Web & App, and dispatch batch vans.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterPaymentStatus == "ALL",
                            onClick = { filterPaymentStatus = "ALL" },
                            label = { Text("All Payments") }
                        )
                        FilterChip(
                            selected = filterPaymentStatus == "PENDING_VERIFICATION",
                            onClick = { filterPaymentStatus = "PENDING_VERIFICATION" },
                            label = { Text("Pending Review") }
                        )
                        FilterChip(
                            selected = filterPaymentStatus == "PAID",
                            onClick = { filterPaymentStatus = "PAID" },
                            label = { Text("Paid") }
                        )
                        FilterChip(
                            selected = filterPaymentStatus == "REFUNDED",
                            onClick = { filterPaymentStatus = "REFUNDED" },
                            label = { Text("Refunded") }
                        )
                    }
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No orders match the current filter.")
                }
            }
        } else {
            items(filteredOrders, key = { it.orderId }) { order ->
                AdminOrderCard(
                    order = order,
                    onUpdateOrderStatus = { onUpdateOrderStatus(order.orderId, it) },
                    onUpdatePaymentStatus = { onUpdatePaymentStatus(order.orderId, it) },
                    onDelete = { onDeleteOrder(order.orderId) }
                )
            }
        }
    }
}

@Composable
fun AdminOrderCard(
    order: OrderEntity,
    onUpdateOrderStatus: (String) -> Unit,
    onUpdatePaymentStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (order.paymentStatus == "PENDING_VERIFICATION") 2.dp else 1.dp,
                color = if (order.paymentStatus == "PENDING_VERIFICATION") Color(0xFFD84315) else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: ID, Source (Web vs App), Meal Slot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Order #${order.orderId}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (order.orderSource == "WEB") Color(0xFF1565C0) else CoralPrimary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (order.orderSource == "WEB") "🌐 WEB ORDER" else "📱 APP ORDER",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Color.White
                            )
                        )
                    }
                }

                Text(
                    text = "${order.mealType} • $${String.format("%.2f", order.totalAmount)}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = CoralPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Customer Name & Desk Info
            Text(
                text = "Customer: ${order.customerName} (${order.customerEmail})",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Drop-off Desk: ${order.deliveryDesk}",
                style = MaterialTheme.typography.bodySmall,
                color = CoralSecondary
            )
            Text(
                text = "Items: ${order.itemsSummary}",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Payment Status Section & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Payment:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = when (order.paymentStatus) {
                            "PAID" -> SuccessGreen.copy(alpha = 0.15f)
                            "PENDING_VERIFICATION" -> Color(0xFFD84315).copy(alpha = 0.15f)
                            "REFUNDED" -> Color(0xFF7B1FA2).copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = order.paymentStatus,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (order.paymentStatus) {
                                    "PAID" -> SuccessGreen
                                    "PENDING_VERIFICATION" -> Color(0xFFD84315)
                                    "REFUNDED" -> Color(0xFF7B1FA2)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        )
                    }
                }

                // Payment Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (order.paymentStatus != "PAID") {
                        FilledTonalButton(
                            onClick = { onUpdatePaymentStatus("PAID") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Mark Paid", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (order.paymentStatus != "REFUNDED") {
                        OutlinedButton(
                            onClick = { onUpdatePaymentStatus("REFUNDED") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Refund", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Order Delivery Status Section & Transition Chips
            Text(
                text = "Kitchen & Delivery Status: ${order.status}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatusTransitionChip("Prep (9:01 AM)", isCurrent = order.status == "BATCHING_PREP") {
                    onUpdateOrderStatus("BATCHING_PREP")
                }
                StatusTransitionChip("Dispatch Van", isCurrent = order.status == "OUT_FOR_DELIVERY") {
                    onUpdateOrderStatus("OUT_FOR_DELIVERY")
                }
                StatusTransitionChip("Delivered", isCurrent = order.status == "DELIVERED") {
                    onUpdateOrderStatus("DELIVERED")
                }
                StatusTransitionChip("Cancel", isCurrent = order.status == "CANCELLED") {
                    onUpdateOrderStatus("CANCELLED")
                }
            }
        }
    }
}

@Composable
private fun StatusTransitionChip(title: String, isCurrent: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isCurrent) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

// ----------------- TAB 1: Kitchen Batching -----------------
@Composable
private fun KitchenBatchingTab(
    orders: List<OrderEntity>,
    currentSlot: MealSlot,
    onUpdateStatus: (String, String) -> Unit
) {
    val slotOrders = orders.filter { it.mealType == currentSlot.name }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CoralSecondary.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${currentSlot.title} Bulk Batch Sheet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            color = CoralSecondary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Cut-Off Locked: 9:01 AM",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aggregated kitchen totals derived from all Web & App customer orders locked before cut-off.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatPill(title = "Total Orders", value = "${slotOrders.size + 42}")
                        StatPill(title = "Bulk Portions", value = "${(slotOrders.size + 42) * 2}")
                        StatPill(title = "Neighborhood Vans", value = "4 Active")
                    }
                }
            }
        }

        item {
            Text(
                text = "Kitchen Aggregation Summary (Produce Sheet)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        item { BulkDishRow(name = "Grilled Citrus Salmon Quinoa Bowl", count = 42, prepStatus = "Cooking - In Wok") }
        item { BulkDishRow(name = "Paneer Tikka Power Bento", count = 38, prepStatus = "Ready to Pack") }
        item { BulkDishRow(name = "High-Protein Herb Chicken & Sweet Potato", count = 29, prepStatus = "Plating") }
        item { BulkDishRow(name = "Smoked Tofu Caesar Salad", count = 18, prepStatus = "Chilled") }
        item { BulkDishRow(name = "Berry Protein Overnight Oats", count = 24, prepStatus = "Ready") }
    }
}

@Composable
private fun BulkDishRow(name: String, count: Int, prepStatus: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CoralPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold, color = CoralPrimary)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text(text = "Status: $prepStatus", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = SuccessGreen)
        }
    }
}

@Composable
private fun StatPill(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = CoralPrimary))
        Text(text = title, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
    }
}

// ----------------- TAB 2: Menu Management -----------------
@Composable
private fun MenuManagementTab(
    items: List<MenuItemEntity>,
    onToggleAvailability: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    onAddNewClicked: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Kitchen Menu Controls",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Toggle items off if ingredients run low",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAddNewClicked,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    modifier = Modifier.testTag("add_new_dish_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Dish")
                }
            }
        }

        items(items, key = { it.id }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = item.mealType,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
                                )
                            }
                        }
                        Text(
                            text = "$${String.format("%.2f", item.price)} • ${item.category} • ${item.restaurantName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoralPrimary
                        )
                        Text(
                            text = if (item.isAvailable) "Available for Scheduled Batches" else "Switched Off (Out of Ingredients)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (item.isAvailable) SuccessGreen else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = item.isAvailable,
                            onCheckedChange = { onToggleAvailability(item.id, it) },
                            modifier = Modifier.testTag("toggle_item_${item.id}")
                        )
                        IconButton(onClick = { onDelete(item.id) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ----------------- TAB 3: Help & Ticket Section -----------------
@Composable
private fun HelpAndTicketsTab(
    tickets: List<SupportTicketEntity>,
    onSelectToResolve: (SupportTicketEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CoralSecondary.copy(alpha = 0.10f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Customer Support & Ticket Escalation Center",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Review escalated tickets from customers or AI chatbot. You can respond with resolution notes and direct wallet refunds.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (tickets.isEmpty()) {
            item {
                Text(
                    text = "No support tickets on record.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(tickets, key = { it.ticketId }) { ticket ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ticket #${ticket.ticketId} • Order #${ticket.orderId}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                color = if (ticket.status == "RESOLVED") SuccessGreen.copy(alpha = 0.15f) else Color(0xFFD84315).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = ticket.status,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (ticket.status == "RESOLVED") SuccessGreen else Color(0xFFD84315)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Customer: ${ticket.customerEmail} • Priority: ${ticket.priority}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Issue: ${ticket.subject}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = CoralPrimary)
                        )
                        Text(
                            text = ticket.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (ticket.status == "OPEN") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onSelectToResolve(ticket) },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralSecondary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Resolve Ticket & Issue Credit")
                            }
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = SuccessGreen.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "Resolution: ${ticket.adminResponse}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = SuccessGreen, fontWeight = FontWeight.Medium)
                                    )
                                    if (ticket.resolutionCredit > 0) {
                                        Text(
                                            text = "Refund Credited: $${String.format("%.2f", ticket.resolutionCredit)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SuccessGreen)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResolveTicketDialog(
    ticket: SupportTicketEntity,
    onDismiss: () -> Unit,
    onResolve: (id: String, response: String, credit: Double) -> Unit
) {
    var responseText by remember { mutableStateOf("We apologize for the inconvenience. A refund credit has been deposited to your PreBite wallet.") }
    var creditText by remember { mutableStateOf("5.00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Resolve Ticket #${ticket.ticketId}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Issue: ${ticket.subject}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = responseText,
                    onValueChange = { responseText = it },
                    label = { Text("Admin Resolution Note") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                OutlinedTextField(
                    value = creditText,
                    onValueChange = { creditText = it },
                    label = { Text("Wallet Credit Refund ($)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val credit = creditText.toDoubleOrNull() ?: 5.0
                    onResolve(ticket.ticketId, responseText, credit)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
            ) {
                Text("Submit Resolution")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ----------------- TAB 4: Business Insights -----------------
@Composable
private fun BusinessEconomicsTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "PreBite Operations & Business Architecture",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Operational Batching vs On-Demand Apps",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CoralPrimary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Traditional delivery apps dispatch 1 driver for 1 craving, generating severe traffic, high delivery fees ($5-$8), and up to 35% kitchen ingredient wastage.\n• PreBite batches all orders by 9 AM / 3 PM cut-offs, routing 1 courier to deliver 25-40 meals per office tower cluster in one consolidated trip.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. 4 Multi-Tier Revenue Streams",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CoralSecondary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Subscription Passes: Weekly ($89) & Monthly ($320) upfront cashflow.\n2. Delivery Fees: $1.99 per order from non-subscribers.\n3. Merchant Commission: 15%–25% net margin per dish.\n4. Late Order Express Fee: $2.50 fee for missed cut-off express orders.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AddNewDishDialog(
    currentSlot: MealSlot,
    onDismiss: () -> Unit,
    onAdd: (
        name: String,
        price: Double,
        category: String,
        mealType: String,
        restaurant: String,
        calories: Int,
        protein: Int,
        description: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("12.50") }
    var category by remember { mutableStateOf("Healthy Bowls") }
    var mealType by remember { mutableStateOf(currentSlot.name) }
    var restaurant by remember { mutableStateOf("Urban Greens Kitchen") }
    var caloriesText by remember { mutableStateOf("450") }
    var proteinText by remember { mutableStateOf("25") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Dish to Kitchen") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dish Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price ($)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = caloriesText,
                        onValueChange = { caloriesText = it },
                        label = { Text("Calories") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = proteinText,
                        onValueChange = { proteinText = it },
                        label = { Text("Protein (g)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Ingredients") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceText.toDoubleOrNull() ?: 10.0
                    val cal = caloriesText.toIntOrNull() ?: 400
                    val pro = proteinText.toIntOrNull() ?: 20
                    if (name.isNotBlank()) {
                        onAdd(name, p, category, mealType, restaurant, cal, pro, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
            ) {
                Text("Add to Menu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

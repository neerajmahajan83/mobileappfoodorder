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
import com.example.data.local.MenuItemEntity
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.PreBiteViewModel
import com.example.util.MealSlot

@Composable
fun AdminPanelScreen(viewModel: PreBiteViewModel) {
    var adminTab by remember { mutableStateOf(0) } // 0: Batch Kitchen, 1: Menu Items, 2: Support Tickets, 3: Economics

    val orders by viewModel.allOrders.collectAsState()
    val menuItems by viewModel.allMenuItems.collectAsState()
    val tickets by viewModel.allTickets.collectAsState()
    val selectedSlot by viewModel.selectedMealSlot.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }

    if (showAddItemDialog) {
        AddNewDishDialog(
            currentSlot = selectedSlot,
            onDismiss = { showAddItemDialog = false },
            onAdd = { name, price, cat, slot, rest, cal, pro, desc ->
                viewModel.addNewMenuItem(name, price, cat, slot, rest, cal, pro, desc)
                showAddItemDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_panel_screen")
    ) {
        // Admin Tab Row
        ScrollableTabRow(
            selectedTabIndex = adminTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CoralPrimary,
            edgePadding = 16.dp
        ) {
            Tab(
                selected = adminTab == 0,
                onClick = { adminTab = 0 },
                text = { Text("Kitchen Batching (9:01 AM)") }
            )
            Tab(
                selected = adminTab == 1,
                onClick = { adminTab = 1 },
                text = { Text("Menu Management (${menuItems.size})") }
            )
            Tab(
                selected = adminTab == 2,
                onClick = { adminTab = 2 },
                text = { Text("Escalated Tickets (${tickets.count { it.status == "OPEN" }})") }
            )
            Tab(
                selected = adminTab == 3,
                onClick = { adminTab = 3 },
                text = { Text("Business Plan Insights") }
            )
        }

        when (adminTab) {
            0 -> KitchenBatchingTab(orders = orders, currentSlot = selectedSlot, onUpdateStatus = { id, s -> viewModel.updateOrderStatus(id, s) })
            1 -> MenuManagementTab(
                items = menuItems,
                onToggleAvailability = { id, avail -> viewModel.toggleMenuItemAvailability(id, avail) },
                onAddNewClicked = { showAddItemDialog = true }
            )
            2 -> EscalatedTicketsTab(
                tickets = tickets,
                onResolve = { id, resp, cred -> viewModel.resolveTicket(id, resp, cred) }
            )
            3 -> BusinessEconomicsTab()
        }
    }
}

@Composable
private fun KitchenBatchingTab(
    orders: List<com.example.data.local.OrderEntity>,
    currentSlot: MealSlot,
    onUpdateStatus: (String, String) -> Unit
) {
    // Group orders for current meal slot to calculate exact kitchen prep requirements
    val slotOrders = orders.filter { it.mealType == currentSlot.name }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cut-Off Batch Header Banner
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
                                text = "Cut-Off Passed: 9:01 AM",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The kitchen receives the exact aggregated counts immediately at cut-off. Cooks prepare in bulk without waste!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatPill(title = "Total Orders", value = "${slotOrders.size + 38}")
                        StatPill(title = "Bulk Meals", value = "${(slotOrders.size + 38) * 2}")
                        StatPill(title = "Neighborhood Vans", value = "4 Dispatched")
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

        // Bulk Dish Prep Aggregation
        item {
            BulkDishRow(name = "Grilled Citrus Salmon Quinoa Bowl", count = 42, prepStatus = "Cooking - In Wok")
        }
        item {
            BulkDishRow(name = "Paneer Tikka Power Bento", count = 38, prepStatus = "Ready to Pack")
        }
        item {
            BulkDishRow(name = "High-Protein Herb Chicken & Sweet Potato", count = 29, prepStatus = "Plating")
        }
        item {
            BulkDishRow(name = "Smoked Tofu Caesar Salad", count = 18, prepStatus = "Chilled")
        }
        item {
            BulkDishRow(name = "Berry Protein Overnight Oats", count = 24, prepStatus = "Ready")
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Active Individual Dispatches",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(slotOrders, key = { it.orderId }) { order ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Order #${order.orderId} • ${order.deliveryAddress}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = order.itemsSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Desk: ${order.deliveryDesk}",
                            style = MaterialTheme.typography.bodySmall.copy(color = CoralSecondary, fontSize = 11.sp)
                        )
                    }

                    Surface(
                        color = when (order.status) {
                            "DELIVERED" -> SuccessGreen.copy(alpha = 0.15f)
                            "OUT_FOR_DELIVERY" -> CoralPrimary.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = order.status,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
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
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = CoralPrimary
                        )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text(text = "Status: $prepStatus", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Icon(
                imageVector = Icons.Default.CheckCircleOutline,
                contentDescription = null,
                tint = SuccessGreen
            )
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

@Composable
private fun MenuManagementTab(
    items: List<MenuItemEntity>,
    onToggleAvailability: (Long, Boolean) -> Unit,
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
                        text = "Toggle items off if out of ingredients",
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
                            text = "$${String.format("%.2f", item.price)} • ${item.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoralPrimary
                        )
                        Text(
                            text = if (item.isAvailable) "Available for Batches" else "Switched Off (Out of Stock)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (item.isAvailable) SuccessGreen else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Switch(
                        checked = item.isAvailable,
                        onCheckedChange = { onToggleAvailability(item.id, it) },
                        modifier = Modifier.testTag("toggle_item_${item.id}")
                    )
                }
            }
        }
    }
}

@Composable
private fun EscalatedTicketsTab(
    tickets: List<com.example.data.local.SupportTicketEntity>,
    onResolve: (String, String, Double) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Escalated Support & Resolution",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (tickets.isEmpty()) {
            item {
                Text(
                    text = "No open support tickets.",
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
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ticket #${ticket.ticketId} (Order ${ticket.orderId})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                color = if (ticket.status == "RESOLVED") SuccessGreen.copy(alpha = 0.15f) else Color(0xFFD84315).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = ticket.status,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Subject: ${ticket.subject}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text(text = ticket.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        if (ticket.status == "OPEN") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    onResolve(
                                        ticket.ticketId,
                                        "Refund of $5.00 PreBite Wallet Credit credited to customer account for batch resolution.",
                                        5.00
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralSecondary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Issue $5 Credit & Mark Resolved")
                            }
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Resolution: ${ticket.adminResponse}",
                                style = MaterialTheme.typography.bodySmall.copy(color = SuccessGreen, fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }
        }
    }
}

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
                text = "PreBite Business Plan Architecture",
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

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Solved Critical Success Factors",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Missed Cut-Off Buffer: Handled gracefully with Express Limited Menu and delayed batch notice.\n• Route Density: Tech parks & universities allow 80+ meals delivered per square kilometer.\n• Instant Support: Hybrid AI triage + WhatsApp floating button escalates unresolved issues in <30 seconds.",
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

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
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.PreBiteViewModel
import com.example.util.MealSlot

@Composable
fun WebPortalView(
    viewModel: PreBiteViewModel,
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current
    val simulatedTime by viewModel.simulatedTime.collectAsState()
    val selectedSlot by viewModel.selectedMealSlot.collectAsState()
    val allMenu by viewModel.allMenuItems.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val addresses by viewModel.userAddresses.collectAsState()

    var selectedDishId by remember { mutableStateOf<Long?>(null) }
    var selectedDeskNote by remember { mutableStateOf("Innovation Tower Desk 304 (Web Order)") }
    var webPaymentMethod by remember { mutableStateOf("Razorpay UPI Online") }
    var placedWebOrderId by remember { mutableStateOf<String?>(null) }

    val slotMenu = remember(allMenu, selectedSlot) {
        allMenu.filter { it.mealType == selectedSlot.name || it.mealType == "ALL" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .testTag("web_portal_screen")
    ) {
        // Simulated Web Browser Frame Top Bar
        Surface(
            color = Color(0xFF2D3748),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Browser window dots
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    // Browser URL Pill
                    Surface(
                        color = Color(0xFF1A202C),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "https://order.prebite.com/${selectedSlot.name.lowercase()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Web Client Banner
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = CoralPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PreBite Web Portal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Logged in as ${currentUser?.fullName ?: "Web Guest"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoralSecondary
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onNavigateToAdmin,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("View in Admin", fontSize = 11.sp)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cut-off banner on website
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CoralPrimary.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🌐 Web Ordering • ${selectedSlot.title} Cut-Off",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CoralPrimary)
                            )
                            Surface(
                                color = CoralPrimary,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Cut-Off ${selectedSlot.cutOffDisplay}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Orders placed here instantly sync with the Kitchen Admin panel for batch consolidation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (placedWebOrderId != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Web Order Placed: #$placedWebOrderId",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Order sent to Kitchen with status 'PENDING_VERIFICATION'. Tap 'View in Admin' to review payment and update kitchen status!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onNavigateToAdmin,
                                colors = ButtonDefaults.buttonColors(containerColor = CoralSecondary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Open Admin Process Orders")
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Select Meal to Order on Web",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(slotMenu, key = { it.id }) { item ->
                val isSelected = selectedDishId == item.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) CoralPrimary.copy(alpha = 0.05f) else Color.White
                    ),
                    onClick = { selectedDishId = item.id }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = "$${String.format("%.2f", item.price)} • ${item.restaurantName}", style = MaterialTheme.typography.bodySmall, color = CoralPrimary)
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedDishId = item.id }
                        )
                    }
                }
            }

            // Web Desk & Checkout section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Web Checkout & Drop-Off Desk",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        OutlinedTextField(
                            value = selectedDeskNote,
                            onValueChange = { selectedDeskNote = it },
                            label = { Text("Delivery Desk / Location") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Payment Gateway: Razorpay UPI (Simulated Online Web Payment)", style = MaterialTheme.typography.bodySmall, color = CoralSecondary)

                        Button(
                            onClick = {
                                val item = slotMenu.firstOrNull { it.id == selectedDishId } ?: slotMenu.firstOrNull()
                                if (item != null) {
                                    viewModel.simulateWebOrderPlacement(
                                        slot = selectedSlot,
                                        dishName = item.name,
                                        amount = item.price,
                                        deliveryDesk = selectedDeskNote
                                    ) { orderId ->
                                        placedWebOrderId = orderId
                                        Toast.makeText(context, "Web Order #$orderId placed successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("place_web_order_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Submit Web Order to Cloud Kitchen", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

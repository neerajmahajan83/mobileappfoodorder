package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.ui.components.CountdownBanner
import com.example.ui.components.DishCard
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSecondary
import com.example.ui.viewmodel.CartItem
import com.example.ui.viewmodel.PreBiteViewModel
import com.example.util.CutOffStatus
import com.example.util.MealSlot

@Composable
fun DashboardScreen(
    viewModel: PreBiteViewModel,
    onOpenCart: () -> Unit
) {
    val selectedSlot by viewModel.selectedMealSlot.collectAsState()
    val statusInfo by viewModel.currentSlotStatus.collectAsState()
    val allMenu by viewModel.allMenuItems.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }

    // Filter menu by current meal slot & category
    val currentSlotMenu = remember(allMenu, selectedSlot, selectedCategory) {
        allMenu.filter { item ->
            val slotMatches = item.mealType == selectedSlot.name || item.mealType == "ALL"
            val categoryMatches = selectedCategory == "All" || item.category == selectedCategory
            slotMatches && categoryMatches
        }
    }

    val categories = listOf("All", "Healthy Bowls", "High Protein", "Chef Thali", "Salads", "Wraps")

    val isExpressMode = statusInfo.status == CutOffStatus.LATE_EXPRESS
    val cartCount = cartItems.values.sumOf { it.quantity }
    val cartSubtotal = viewModel.cartSubtotal

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (cartCount > 0) 80.dp else 16.dp)
        ) {
            // Meal Slot Toggle Tabs (Breakfast, Lunch, Dinner)
            item {
                MealSlotSelector(
                    selectedSlot = selectedSlot,
                    onSelectSlot = { viewModel.setMealSlot(it) }
                )
            }

            // Dynamic Live Countdown Banner
            item {
                CountdownBanner(statusInfo = statusInfo)
            }

            // Category Filter Chips
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CoralPrimary.copy(alpha = 0.15f),
                                selectedLabelColor = CoralPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Section Header & Items Count
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedSlot.title} Menu",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isExpressMode) "Express items available for late batch" else "Prepared fresh at scheduled cut-off",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${currentSlotMenu.size} options",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Menu Items List
            if (currentSlotMenu.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No items found in this category.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(currentSlotMenu, key = { it.id }) { item ->
                    val quantity = viewModel.getItemCartQuantity(item.id)
                    DishCard(
                        item = item,
                        cartQuantity = quantity,
                        isExpressMode = isExpressMode,
                        onAddToCart = { viewModel.addToCart(item) },
                        onRemoveFromCart = { viewModel.removeFromCart(item) }
                    )
                }
            }

            // Explanatory Value Proposition Card at bottom
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CoralSecondary.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = CoralSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "How PreBite Saves You 25% + Zero Waste",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CoralSecondary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Because you lock in your meal before the cut-off time, our partner kitchens prep the exact number of dishes needed. No surplus discarded, and one delivery courier serves your entire office floor together!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }

        // Floating Cart Summary Bar
        AnimatedVisibility(
            visible = cartCount > 0,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                color = CoralPrimary,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$cartCount items • $${String.format("%.2f", cartSubtotal)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Scheduled for ${selectedSlot.title} Batch",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        )
                    }

                    Button(
                        onClick = onOpenCart,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("view_cart_button")
                    ) {
                        Text(
                            text = "View Cart",
                            color = CoralPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = CoralPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MealSlotSelector(
    selectedSlot: MealSlot,
    onSelectSlot: (MealSlot) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MealSlot.values().forEach { slot ->
            val isSelected = slot == selectedSlot
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("slot_tab_${slot.name.lowercase()}"),
                color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
                onClick = { onSelectSlot(slot) }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = when (slot) {
                            MealSlot.BREAKFAST -> Icons.Default.WbSunny
                            MealSlot.LUNCH -> Icons.Default.LunchDining
                            MealSlot.DINNER -> Icons.Default.DinnerDining
                        },
                        contentDescription = slot.title,
                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = slot.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = when (slot) {
                            MealSlot.BREAKFAST -> "By 9 PM"
                            MealSlot.LUNCH -> "By 9 AM"
                            MealSlot.DINNER -> "By 3 PM"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

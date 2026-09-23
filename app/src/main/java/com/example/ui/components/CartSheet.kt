package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.local.MealSubscriptionEntity
import com.example.data.local.MenuItemEntity
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.CartItem
import com.example.util.MealSlot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartSheet(
    cartItems: Map<Long, CartItem>,
    selectedSlot: MealSlot,
    subtotal: Double,
    deliveryFee: Double,
    lateFee: Double,
    discount: Double,
    total: Double,
    isLateExpress: Boolean,
    deliveryDeskNote: String,
    activePass: MealSubscriptionEntity?,
    onUpdateDeskNote: (String) -> Unit,
    onAddToCart: (MenuItemEntity) -> Unit,
    onRemoveFromCart: (MenuItemEntity) -> Unit,
    onDismiss: () -> Unit,
    onPlaceOrder: (String) -> Unit
) {
    var selectedPayment by remember {
        mutableStateOf(
            if (activePass != null && activePass.isActive && activePass.remainingMeals > 0)
                "PreBite Meal Pass"
            else
                "Stripe Card •••• 4242"
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("cart_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Title & Scheduled Slot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Scheduled Meal Cart",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${selectedSlot.title} Batch • Arrives ${selectedSlot.deliveryWindow}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoralSecondary
                    )
                }

                Surface(
                    color = CoralPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${cartItems.values.sumOf { it.quantity }} items",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CoralPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Items List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
            ) {
                items(cartItems.values.toList()) { cartItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cartItem.menuItem.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "$${String.format("%.2f", cartItem.menuItem.price)} each • ${cartItem.menuItem.calories} kcal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onRemoveFromCart(cartItem.menuItem) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = CoralPrimary)
                            }
                            Text(
                                text = "${cartItem.quantity}",
                                modifier = Modifier.padding(horizontal = 6.dp),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            IconButton(
                                onClick = { onAddToCart(cartItem.menuItem) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Increase", tint = CoralPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Delivery Desk Drop-off instruction
            OutlinedTextField(
                value = deliveryDeskNote,
                onValueChange = onUpdateDeskNote,
                label = { Text("Delivery Desk / Drop-off Note") },
                placeholder = { Text("e.g. Building B, 4th Floor Pantry") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delivery_desk_input"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.LocationCity, contentDescription = null, tint = CoralPrimary)
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Methods
            Text(
                text = "Select Payment Method",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (activePass != null && activePass.isActive && activePass.remainingMeals > 0) {
                    PaymentChip(
                        title = "Meal Pass",
                        subtitle = "${activePass.remainingMeals} left",
                        isSelected = selectedPayment == "PreBite Meal Pass",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedPayment = "PreBite Meal Pass" }
                    )
                }

                PaymentChip(
                    title = "Stripe Card",
                    subtitle = "•••• 4242",
                    isSelected = selectedPayment == "Stripe Card •••• 4242",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedPayment = "Stripe Card •••• 4242" }
                )

                PaymentChip(
                    title = "Razorpay UPI",
                    subtitle = "GPay/PhonePe",
                    isSelected = selectedPayment == "Razorpay UPI",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedPayment = "Razorpay UPI" }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cost Summary Card
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", style = MaterialTheme.typography.bodySmall)
                        Text("$${String.format("%.2f", subtotal)}", style = MaterialTheme.typography.bodySmall)
                    }

                    if (discount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Meal Pass Discount (15%)", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                            Text("-$${String.format("%.2f", discount)}", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Batched Delivery Fee", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = if (deliveryFee == 0.0) "FREE (Pass)" else "$${String.format("%.2f", deliveryFee)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (deliveryFee == 0.0) SuccessGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (lateFee > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Late Express Window Fee", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD84315))
                            Text("+$${String.format("%.2f", lateFee)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD84315))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = "$${String.format("%.2f", total)}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = CoralPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Button
            Button(
                onClick = { onPlaceOrder(selectedPayment) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_order_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
            ) {
                Icon(Icons.Default.LockClock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lock In Scheduled Batch • $${String.format("%.2f", total)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun PaymentChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(10.dp)
            ),
        color = if (isSelected) CoralPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

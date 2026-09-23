package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.local.UserEntity
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSecondary
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopHeader(
    simulatedTime: LocalTime,
    isAdminMode: Boolean,
    cartItemCount: Int,
    currentUser: UserEntity?,
    onToggleAdminMode: (Boolean) -> Unit,
    onSelectTimePreset: (String) -> Unit,
    onCartClicked: () -> Unit,
    onProfileClicked: () -> Unit,
    onWebPortalClicked: () -> Unit
) {
    var showTimeMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo & Brand Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CoralPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "PreBite Logo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PreBite",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (isAdminMode) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (isAdminMode) "ADMIN" else "SCHEDULED",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp
                                    ),
                                    color = if (isAdminMode) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Text(
                            text = "Batch Dining • Cut-Off",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Actions: Mode switch, Time preset, Web View, Profile, Cart
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Time preset simulation dropdown
                    Box {
                        FilledTonalButton(
                            onClick = { showTimeMenu = true },
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("time_sim_button"),
                            contentPadding = PaddingValues(horizontal = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Simulate Time",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = simulatedTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        DropdownMenu(
                            expanded = showTimeMenu,
                            onDismissRequest = { showTimeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("🌅 8:15 AM (Lunch Open - 45m left)") },
                                onClick = {
                                    onSelectTimePreset("morning_open")
                                    showTimeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("⚠️ 9:15 AM (Lunch Cut-Off Passed - Express)") },
                                onClick = {
                                    onSelectTimePreset("lunch_cutoff_passed")
                                    showTimeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🍽️ 2:10 PM (Dinner Open - 50m left)") },
                                onClick = {
                                    onSelectTimePreset("afternoon_dinner_open")
                                    showTimeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🌙 8:20 PM (Breakfast Open - 40m left)") },
                                onClick = {
                                    onSelectTimePreset("evening_breakfast_open")
                                    showTimeMenu = false
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("📱 Use Actual Device Clock") },
                                onClick = {
                                    onSelectTimePreset("system_time")
                                    showTimeMenu = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Web Portal button
                    IconButton(
                        onClick = onWebPortalClicked,
                        modifier = Modifier.size(32.dp).testTag("web_portal_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Web Portal Mode",
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Cart Icon with Badge
                    IconButton(
                        onClick = onCartClicked,
                        modifier = Modifier.size(32.dp).testTag("header_cart_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge(containerColor = CoralPrimary) {
                                        Text(
                                            text = "$cartItemCount",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Cart",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // User Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (currentUser?.role == "ADMIN") CoralSecondary else CoralPrimary)
                            .clickable(onClick = onProfileClicked)
                            .testTag("header_profile_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = currentUser?.fullName?.split(" ")?.mapNotNull { it.firstOrNull()?.toString() }?.take(2)?.joinToString("") ?: "U"
                        Text(
                            text = initials,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        )
                    }
                }
            }
        }
    }
}

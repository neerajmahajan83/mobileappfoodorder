package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralPrimary

@Composable
fun RaiseIssueDialog(
    orderId: String,
    onDismiss: () -> Unit,
    onSubmit: (issueType: String, subject: String, description: String) -> Unit
) {
    var selectedIssueType by remember { mutableStateOf("MISSING_ITEM") }
    var description by remember { mutableStateOf("") }
    var resolutionPreference by remember { mutableStateOf("PreBite Wallet Credit") }

    val issueTypes = listOf(
        "MISSING_ITEM" to "Missing Item / Side",
        "LATE_DELIVERY" to "Batch Delivered >15m Late",
        "PACKAGING" to "Packaging Spilled / Damaged",
        "TEMPERATURE" to "Meal Was Not Warm / Fresh",
        "OTHER" to "Other Delivery Concern"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ReportProblem,
                    contentDescription = null,
                    tint = CoralPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Raise an Issue",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Order $orderId",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "What went wrong with your batch?",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Issue Type Dropdown / Radio Buttons
                issueTypes.forEach { (typeKey, typeLabel) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedIssueType == typeKey,
                            onClick = { selectedIssueType = typeKey }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = typeLabel, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details / Affected Items") },
                    placeholder = { Text("e.g. Smoothie bottle was missing from paper bag") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("issue_description_input"),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Preferred Resolution",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = resolutionPreference == "PreBite Wallet Credit",
                        onClick = { resolutionPreference = "PreBite Wallet Credit" },
                        label = { Text("Instant Credit", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = resolutionPreference == "Next Batch Replacement",
                        onClick = { resolutionPreference = "Next Batch Replacement" },
                        label = { Text("Replacement in Next Batch", fontSize = 11.sp) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val subject = issueTypes.firstOrNull { it.first == selectedIssueType }?.second ?: "Order Issue"
                    val fullDesc = if (description.isBlank()) "$subject (Preferred: $resolutionPreference)" else "$description (Preferred: $resolutionPreference)"
                    onSubmit(selectedIssueType, subject, fullDesc)
                },
                modifier = Modifier.testTag("submit_issue_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
            ) {
                Text("Submit Ticket")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(18.dp)
    )
}

package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val price: Double,
    val mealType: String, // "BREAKFAST", "LUNCH", "DINNER"
    val category: String, // "Healthy Bowls", "High Protein", "Chef Thali", "Wraps", "Salads", "Drinks"
    val restaurantName: String,
    val calories: Int,
    val proteinGrams: Int,
    val rating: Double,
    val isAvailable: Boolean = true,
    val isExpressAvailable: Boolean = true, // Available if user missed cut-off
    val iconCategory: String = "bowl" // "bowl", "wrap", "salad", "thali", "drink", "oats"
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val orderId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val mealType: String, // "BREAKFAST", "LUNCH", "DINNER"
    val targetDate: String, // "Today", "Tomorrow"
    val deliveryTimeWindow: String, // e.g. "12:00 PM - 12:45 PM"
    val status: String, // "CONFIRMED", "BATCHING_PREP", "OUT_FOR_DELIVERY", "DELIVERED"
    val itemsSummary: String, // e.g. "2x Grilled Salmon Bowl, 1x Green Smoothie"
    val subtotal: Double,
    val deliveryFee: Double,
    val lateFee: Double,
    val discount: Double,
    val totalAmount: Double,
    val isLateOrder: Boolean = false,
    val deliveryAddress: String = "Tech Park Tower B, Suite 402",
    val deliveryDesk: String = "Drop off at 4th Floor Pantry / Reception",
    val paymentMethod: String = "Stripe Card •••• 4242",
    val driverName: String = "Marcus Vance",
    val driverPhone: String = "+1 (555) 782-9011",
    val driverVehicle: String = "Eco-Electric Van #12",
    val batchStopNumber: Int = 3,
    val totalBatchStops: Int = 7,
    val routeProgress: Float = 0.55f // 0.0 to 1.0 for driver map progress
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey
    val ticketId: String,
    val orderId: String,
    val issueType: String, // "MISSING_ITEM", "LATE_DELIVERY", "PACKAGING", "TEMPERATURE", "OTHER"
    val subject: String,
    val description: String,
    val status: String = "OPEN", // "OPEN", "INVESTIGATING", "RESOLVED"
    val priority: String = "HIGH", // "NORMAL", "HIGH", "URGENT"
    val createdAt: Long = System.currentTimeMillis(),
    val adminResponse: String = "",
    val resolutionCredit: Double = 0.0
)

@Entity(tableName = "subscriptions")
data class MealSubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val planName: String,
    val remainingMeals: Int,
    val totalMeals: Int,
    val expiresDate: String,
    val discountPercent: Int = 15,
    val hasFreeDelivery: Boolean = true,
    val isActive: Boolean = true
)

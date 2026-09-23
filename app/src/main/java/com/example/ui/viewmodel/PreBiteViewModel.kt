package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MenuItemEntity
import com.example.data.local.MealSubscriptionEntity
import com.example.data.local.OrderEntity
import com.example.data.local.PreBiteDatabase
import com.example.data.local.SupportTicketEntity
import com.example.data.repository.PreBiteRepository
import com.example.util.CutOffManager
import com.example.util.CutOffStatus
import com.example.util.MealSlot
import com.example.util.MealSlotStatusInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class CartItem(
    val menuItem: MenuItemEntity,
    val quantity: Int
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user", "bot", "system"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val canEscalateToTicket: Boolean = false,
    val suggestedAction: String? = null
)

class PreBiteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PreBiteRepository

    init {
        val database = PreBiteDatabase.getDatabase(application)
        repository = PreBiteRepository(database.dao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
        startClockAndRouteTicker()
    }

    // Selected Meal Slot Tab
    private val _selectedMealSlot = MutableStateFlow(MealSlot.LUNCH)
    val selectedMealSlot: StateFlow<MealSlot> = _selectedMealSlot.asStateFlow()

    // Simulated Time (Defaults to 8:15 AM so user starts in open Lunch cut-off window)
    private val _simulatedTime = MutableStateFlow<LocalTime>(LocalTime.of(8, 15, 0))
    val simulatedTime: StateFlow<LocalTime> = _simulatedTime.asStateFlow()

    private val _isTimeSimulationActive = MutableStateFlow(true)
    val isTimeSimulationActive: StateFlow<Boolean> = _isTimeSimulationActive.asStateFlow()

    // Cart State
    private val _cartItems = MutableStateFlow<Map<Long, CartItem>>(emptyMap())
    val cartItems: StateFlow<Map<Long, CartItem>> = _cartItems.asStateFlow()

    private val _deliveryDeskNote = MutableStateFlow("Building B, 4th Floor Reception")
    val deliveryDeskNote: StateFlow<String> = _deliveryDeskNote.asStateFlow()

    // Active Tab in App
    private val _currentScreen = MutableStateFlow("home") // "home", "tracking", "pass", "support", "admin"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Chatbot Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "bot",
                text = "👋 Hello! I'm your PreBite Meal Assistant. Ask me about your batch schedule, our 9 AM / 3 PM cut-offs, or report an issue with an active delivery."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Data from Room
    val allMenuItems = repository.allMenuItems.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allOrders = repository.allOrders.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allTickets = repository.allTickets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val activeSubscription = repository.activeSubscription.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    // Current slot status calculated reactively
    val currentSlotStatus: StateFlow<MealSlotStatusInfo> = combine(
        _selectedMealSlot,
        _simulatedTime
    ) { slot, time ->
        CutOffManager.getSlotStatus(slot, time)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CutOffManager.getSlotStatus(MealSlot.LUNCH, LocalTime.of(8, 15, 0))
    )

    fun setMealSlot(slot: MealSlot) {
        _selectedMealSlot.value = slot
    }

    fun setSimulatedPreset(preset: String) {
        when (preset) {
            "morning_open" -> { // 8:15 AM
                _simulatedTime.value = LocalTime.of(8, 15, 0)
                _isTimeSimulationActive.value = true
                _selectedMealSlot.value = MealSlot.LUNCH
            }
            "lunch_cutoff_passed" -> { // 9:15 AM (Express Late mode)
                _simulatedTime.value = LocalTime.of(9, 15, 0)
                _isTimeSimulationActive.value = true
                _selectedMealSlot.value = MealSlot.LUNCH
            }
            "afternoon_dinner_open" -> { // 2:10 PM
                _simulatedTime.value = LocalTime.of(14, 10, 0)
                _isTimeSimulationActive.value = true
                _selectedMealSlot.value = MealSlot.DINNER
            }
            "evening_breakfast_open" -> { // 8:20 PM
                _simulatedTime.value = LocalTime.of(20, 20, 0)
                _isTimeSimulationActive.value = true
                _selectedMealSlot.value = MealSlot.BREAKFAST
            }
            "system_time" -> {
                _simulatedTime.value = LocalTime.now()
                _isTimeSimulationActive.value = false
            }
        }
    }

    fun setScreen(screen: String) {
        _currentScreen.value = screen
    }

    fun updateDeliveryDeskNote(note: String) {
        _deliveryDeskNote.value = note
    }

    // Cart Operations
    fun addToCart(item: MenuItemEntity) {
        val current = _cartItems.value.toMutableMap()
        val existing = current[item.id]
        if (existing != null) {
            current[item.id] = existing.copy(quantity = existing.quantity + 1)
        } else {
            current[item.id] = CartItem(menuItem = item, quantity = 1)
        }
        _cartItems.value = current
    }

    fun removeFromCart(item: MenuItemEntity) {
        val current = _cartItems.value.toMutableMap()
        val existing = current[item.id] ?: return
        if (existing.quantity > 1) {
            current[item.id] = existing.copy(quantity = existing.quantity - 1)
        } else {
            current.remove(item.id)
        }
        _cartItems.value = current
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
    }

    fun getItemCartQuantity(itemId: Long): Int {
        return _cartItems.value[itemId]?.quantity ?: 0
    }

    val cartSubtotal: Double
        get() = _cartItems.value.values.sumOf { it.menuItem.price * it.quantity }

    val isLateExpressActive: Boolean
        get() = currentSlotStatus.value.status == CutOffStatus.LATE_EXPRESS

    val cartLateFee: Double
        get() = if (isLateExpressActive && _cartItems.value.isNotEmpty()) 2.50 else 0.0

    val cartDeliveryFee: Double
        get() {
            if (_cartItems.value.isEmpty()) return 0.0
            val sub = activeSubscription.value
            return if (sub != null && sub.isActive && sub.hasFreeDelivery) 0.0 else 1.99
        }

    val cartDiscount: Double
        get() {
            val sub = activeSubscription.value
            return if (sub != null && sub.isActive) cartSubtotal * 0.15 else 0.0
        }

    val cartTotal: Double
        get() = (cartSubtotal + cartDeliveryFee + cartLateFee - cartDiscount).coerceAtLeast(0.0)

    // Place Order
    fun placeOrder(paymentMethod: String, onOrderPlaced: (String) -> Unit) {
        val items = _cartItems.value.values.toList()
        if (items.isEmpty()) return

        val slot = _selectedMealSlot.value
        val isLate = isLateExpressActive
        val orderNum = (10000..99999).random()
        val orderId = "PB-$orderNum"

        val summary = items.joinToString(", ") { "${it.quantity}x ${it.menuItem.name}" }

        val order = OrderEntity(
            orderId = orderId,
            createdAt = System.currentTimeMillis(),
            mealType = slot.name,
            targetDate = if (slot == MealSlot.BREAKFAST) "Tomorrow" else "Today",
            deliveryTimeWindow = if (isLate) "1:15 PM - 1:45 PM (Express)" else slot.deliveryWindow,
            status = "CONFIRMED",
            itemsSummary = summary,
            subtotal = cartSubtotal,
            deliveryFee = cartDeliveryFee,
            lateFee = cartLateFee,
            discount = cartDiscount,
            totalAmount = cartTotal,
            isLateOrder = isLate,
            deliveryAddress = "Tech Park Tower B, Suite 402",
            deliveryDesk = _deliveryDeskNote.value,
            paymentMethod = paymentMethod,
            driverName = "Marcus Vance",
            driverPhone = "+1 (555) 782-9011",
            driverVehicle = "Eco-Electric Van #12",
            batchStopNumber = 3,
            totalBatchStops = 8,
            routeProgress = 0.15f
        )

        viewModelScope.launch {
            repository.insertOrder(order)
            val sub = activeSubscription.value
            if (sub != null && sub.isActive && paymentMethod.contains("Meal Pass", ignoreCase = true)) {
                repository.useSubscriptionMeal(sub.id)
            }
            clearCart()
            onOrderPlaced(orderId)
        }
    }

    // Issue Reporting & Support
    fun submitSupportTicket(
        orderId: String,
        issueType: String,
        subject: String,
        description: String,
        onSubmitted: () -> Unit
    ) {
        val ticketId = "TKT-" + (2000..9999).random()
        val ticket = SupportTicketEntity(
            ticketId = ticketId,
            orderId = orderId,
            issueType = issueType,
            subject = subject,
            description = description,
            status = "OPEN",
            priority = if (issueType == "MISSING_ITEM" || issueType == "LATE_DELIVERY") "URGENT" else "HIGH",
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.insertTicket(ticket)
            // Add automated resolution or response to chat
            _chatMessages.value = _chatMessages.value + ChatMessage(
                sender = "system",
                text = "🚨 Ticket #$ticketId opened for order $orderId ($subject). Our dispatcher team is notified and reviewing your batch immediately."
            )
            onSubmitted()
        }
    }

    // AI Chatbot Logic
    fun sendChatMessage(userText: String) {
        val userMsg = ChatMessage(sender = "user", text = userText)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            delay(600) // Realistic conversational response
            val lower = userText.lowercase()
            val replyText: String
            var canEscalate = false

            when {
                lower.contains("cut-off") || lower.contains("cutoff") || lower.contains("why") && lower.contains("time") -> {
                    replyText = "Our strict cut-off times (9:00 PM Breakfast, 9:00 AM Lunch, 3:00 PM Dinner) allow our partner kitchens to batch prep exact ingredient quantities. This completely eliminates food waste and enables grouped neighborhood deliveries with zero carbon congestion! 🌱"
                }
                lower.contains("missed") || lower.contains("late") && lower.contains("order") -> {
                    replyText = "If you miss the 9:00 AM or 3:00 PM cut-off, don't worry! We activate our Limited Express Menu with a small $2.50 late fee. These are fast-prep meals added to an express runner batch."
                }
                lower.contains("where") || lower.contains("track") || lower.contains("status") -> {
                    replyText = "You can view your driver's live route on our interactive tracking map! Orders move from 'Batch Confirmed' to 'Kitchen Prep' at the cut-off minute, then out for batch drop-offs."
                }
                lower.contains("missing") || lower.contains("damaged") || lower.contains("cold") || lower.contains("refund") -> {
                    replyText = "I'm so sorry to hear that! Would you like me to instantly raise a priority resolution ticket for our kitchen manager?"
                    canEscalate = true
                }
                lower.contains("ticket") || lower.contains("human") || lower.contains("agent") || lower.contains("help") -> {
                    replyText = "I can immediately generate a high-priority Support Ticket directly to our operations room, or you can tap below to chat directly with our on-call coordinator on WhatsApp!"
                    canEscalate = true
                }
                else -> {
                    replyText = "I'm here to assist with meal scheduling, our batch cut-off rules, pass discounts, or live delivery status. Would you like help with an active order?"
                    canEscalate = true
                }
            }

            _chatMessages.value = _chatMessages.value + ChatMessage(
                sender = "bot",
                text = replyText,
                canEscalateToTicket = canEscalate
            )
        }
    }

    // Admin & Kitchen Management Operations
    fun toggleMenuItemAvailability(id: Long, isAvailable: Boolean) {
        viewModelScope.launch {
            repository.updateItemAvailability(id, isAvailable)
        }
    }

    fun addNewMenuItem(
        name: String,
        price: Double,
        category: String,
        mealType: String,
        restaurantName: String,
        calories: Int,
        protein: Int,
        description: String
    ) {
        val newItem = MenuItemEntity(
            name = name,
            description = description,
            price = price,
            category = category,
            mealType = mealType,
            restaurantName = restaurantName,
            calories = calories,
            proteinGrams = protein,
            rating = 5.0,
            isAvailable = true,
            isExpressAvailable = true,
            iconCategory = "bowl"
        )
        viewModelScope.launch {
            repository.addMenuItem(newItem)
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    fun resolveTicket(ticketId: String, response: String, creditAmount: Double) {
        viewModelScope.launch {
            repository.resolveTicket(ticketId, "RESOLVED", response, creditAmount)
        }
    }

    fun purchaseMealPass(planName: String, totalMeals: Int) {
        val pass = MealSubscriptionEntity(
            planName = planName,
            remainingMeals = totalMeals,
            totalMeals = totalMeals,
            expiresDate = "30 Days from Today",
            discountPercent = 15,
            hasFreeDelivery = true,
            isActive = true
        )
        viewModelScope.launch {
            repository.activateSubscription(pass)
        }
    }

    // Background clock ticker & live courier progress animation
    private fun startClockAndRouteTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_isTimeSimulationActive.value) {
                    _simulatedTime.value = LocalTime.now()
                } else {
                    // Advance simulated time naturally by 1 second every real second
                    _simulatedTime.value = _simulatedTime.value.plusSeconds(1)
                }

                // Simulate slight movement on active out-for-delivery orders
                val orders = allOrders.value
                val activeOrder = orders.firstOrNull { it.status == "OUT_FOR_DELIVERY" }
                if (activeOrder != null && activeOrder.routeProgress < 0.98f) {
                    val nextProgress = (activeOrder.routeProgress + 0.005f).coerceAtMost(1.0f)
                    repository.updateRouteProgress(activeOrder.orderId, nextProgress)
                }
            }
        }
    }
}

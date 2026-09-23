package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AddressEntity
import com.example.data.local.MenuItemEntity
import com.example.data.local.MealSubscriptionEntity
import com.example.data.local.OrderEntity
import com.example.data.local.PreBiteDatabase
import com.example.data.local.SupportTicketEntity
import com.example.data.local.UserEntity
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
import kotlinx.coroutines.flow.flatMapLatest
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

    // --- Authentication & Current User State ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentAuthEmail = MutableStateFlow("alex@techcorp.com")

    val userAddresses: StateFlow<List<AddressEntity>> = _currentAuthEmail.flatMapLatest { email ->
        repository.getAddressesForUser(email)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _selectedAddress = MutableStateFlow<AddressEntity?>(null)
    val selectedAddress: StateFlow<AddressEntity?> = _selectedAddress.asStateFlow()

    init {
        val database = PreBiteDatabase.getDatabase(application)
        repository = PreBiteRepository(database.dao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            // Load initial customer user
            val user = repository.getUserByEmailSync("alex@techcorp.com")
            _currentUser.value = user
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

    // Active Screen in App: "home", "tracking", "pass", "support", "admin", "profile", "web_portal", "login"
    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Chatbot Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "bot",
                text = "👋 Hello! I'm your PreBite Meal Assistant. Ask me about batch scheduling, our 9 AM / 3 PM cut-offs, or report an issue with an active delivery."
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

    fun selectAddress(address: AddressEntity) {
        _selectedAddress.value = address
        _deliveryDeskNote.value = "${address.floorDesk} (${address.deliveryNotes})"
    }

    // --- Authentication & User Operations ---
    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmailSync(email.trim())
            if (user == null) {
                onResult(false, "No account found with $email. Please register first.")
                return@launch
            }
            if (user.passwordHash != password.trim()) {
                onResult(false, "Incorrect password. Try 'user123' or 'admin123'.")
                return@launch
            }
            _currentUser.value = user
            _currentAuthEmail.value = user.email
            _isLoggedIn.value = true
            if (user.role == "ADMIN") {
                _currentScreen.value = "admin"
            } else {
                _currentScreen.value = "home"
            }
            onResult(true, "Welcome back, ${user.fullName}!")
        }
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        phone: String,
        role: String,
        company: String,
        initialAddress: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val existing = repository.getUserByEmailSync(email.trim())
            if (existing != null) {
                onResult(false, "An account with this email already exists.")
                return@launch
            }
            val newUser = UserEntity(
                email = email.trim(),
                passwordHash = password.trim(),
                fullName = fullName.trim(),
                phone = phone.trim(),
                role = role,
                companyName = company.ifBlank { "Office Tower B" },
                dietaryPreference = "Balanced Clean Meals",
                walletBalance = 15.00
            )
            repository.registerUser(newUser)

            if (initialAddress.isNotBlank()) {
                repository.addAddress(
                    AddressEntity(
                        userEmail = newUser.email,
                        label = "Work / Primary",
                        addressLine = initialAddress.trim(),
                        floorDesk = "Reception / Desk",
                        isDefault = true
                    )
                )
            }

            _currentUser.value = newUser
            _currentAuthEmail.value = newUser.email
            _isLoggedIn.value = true
            _currentScreen.value = if (role == "ADMIN") "admin" else "home"
            onResult(true, "Account created successfully!")
        }
    }

    fun forgotPassword(email: String, newPassword: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmailSync(email.trim())
            if (user == null) {
                onResult(false, "No registered account found with email $email.")
                return@launch
            }
            repository.updatePassword(user.email, newPassword.trim())
            val updated = repository.getUserByEmailSync(user.email)
            _currentUser.value = updated
            _currentAuthEmail.value = user.email
            _isLoggedIn.value = true
            onResult(true, "Password reset successful! You are now logged in.")
        }
    }

    fun updateProfile(
        fullName: String,
        phone: String,
        company: String,
        dietary: String,
        onComplete: (Boolean) -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(
                fullName = fullName.trim(),
                phone = phone.trim(),
                companyName = company.trim(),
                dietaryPreference = dietary.trim()
            )
            repository.updateUser(updated)
            _currentUser.value = updated
            onComplete(true)
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUser.value = null
        _currentScreen.value = "login"
    }

    fun switchAccount(email: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmailSync(email)
            if (user != null) {
                _currentUser.value = user
                _currentAuthEmail.value = user.email
                _isLoggedIn.value = true
                _currentScreen.value = if (user.role == "ADMIN") "admin" else "home"
            }
        }
    }

    // --- Address Book Operations ---
    fun addAddress(
        label: String,
        addressLine: String,
        floorDesk: String,
        notes: String,
        isDefault: Boolean
    ) {
        val email = _currentUser.value?.email ?: _currentAuthEmail.value
        viewModelScope.launch {
            val address = AddressEntity(
                userEmail = email,
                label = label.ifBlank { "Work / Desk" },
                addressLine = addressLine.ifBlank { "Tech Center Plaza" },
                floorDesk = floorDesk.ifBlank { "Desk 402" },
                deliveryNotes = notes,
                isDefault = isDefault
            )
            repository.addAddress(address)
        }
    }

    fun deleteAddress(id: Long) {
        viewModelScope.launch {
            repository.deleteAddress(id)
        }
    }

    fun setDefaultAddress(id: Long) {
        val email = _currentUser.value?.email ?: _currentAuthEmail.value
        viewModelScope.launch {
            repository.setDefaultAddress(id, email)
        }
    }

    // --- Cart Operations ---
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

    val cartDiscount: Double
        get() {
            val sub = activeSubscription.value
            return if (sub != null && sub.isActive && sub.remainingMeals > 0) {
                cartSubtotal * (sub.discountPercent / 100.0)
            } else {
                0.0
            }
        }

    val cartDeliveryFee: Double
        get() {
            val sub = activeSubscription.value
            return if (sub != null && sub.isActive && sub.hasFreeDelivery) {
                0.0
            } else if (_cartItems.value.isNotEmpty()) {
                1.99
            } else {
                0.0
            }
        }

    val cartTotal: Double
        get() {
            val sub = cartSubtotal
            if (sub == 0.0) return 0.0
            return (sub - cartDiscount + cartDeliveryFee + cartLateFee).coerceAtLeast(0.0)
        }

    // Place Order
    fun placeOrder(
        paymentMethod: String,
        orderSource: String = "APP",
        onSuccess: (orderId: String) -> Unit
    ) {
        val items = _cartItems.value.values.toList()
        if (items.isEmpty()) return

        val orderNum = (10000..99999).random()
        val orderId = "PB-$orderNum"
        val summary = items.joinToString(", ") { "${it.quantity}x ${it.menuItem.name}" }
        val slot = _selectedMealSlot.value
        val user = _currentUser.value

        val newOrder = OrderEntity(
            orderId = orderId,
            customerEmail = user?.email ?: "alex@techcorp.com",
            customerName = user?.fullName ?: "Alex Chen",
            orderSource = orderSource,
            createdAt = System.currentTimeMillis(),
            mealType = slot.name,
            targetDate = if (slot == MealSlot.BREAKFAST) "Tomorrow" else "Today",
            deliveryTimeWindow = slot.deliveryWindow,
            status = "CONFIRMED",
            paymentStatus = "PAID",
            itemsSummary = summary,
            subtotal = cartSubtotal,
            deliveryFee = cartDeliveryFee,
            lateFee = cartLateFee,
            discount = cartDiscount,
            totalAmount = cartTotal,
            isLateOrder = isLateExpressActive,
            deliveryAddress = _selectedAddress.value?.addressLine ?: (user?.companyName ?: "Tech Park Tower B, Suite 402"),
            deliveryDesk = _deliveryDeskNote.value,
            paymentMethod = paymentMethod,
            driverName = "Marcus Vance",
            driverPhone = "+1 (555) 782-9011",
            driverVehicle = "Eco-Electric Van #12",
            batchStopNumber = 4,
            totalBatchStops = 8,
            routeProgress = 0.10f
        )

        viewModelScope.launch {
            repository.insertOrder(newOrder)
            val sub = activeSubscription.value
            if (sub != null && sub.isActive && paymentMethod == "PreBite Meal Pass") {
                repository.useSubscriptionMeal(sub.id)
            }
            clearCart()
            onSuccess(orderId)
        }
    }

    // Simulate Web Order placement from the Web Portal view
    fun simulateWebOrderPlacement(
        slot: MealSlot,
        dishName: String,
        amount: Double,
        deliveryDesk: String,
        onSuccess: (String) -> Unit
    ) {
        val orderNum = (80000..89999).random()
        val orderId = "PB-$orderNum"
        val user = _currentUser.value

        val webOrder = OrderEntity(
            orderId = orderId,
            customerEmail = user?.email ?: "alex@techcorp.com",
            customerName = user?.fullName ?: "Alex Chen",
            orderSource = "WEB",
            createdAt = System.currentTimeMillis(),
            mealType = slot.name,
            targetDate = "Today",
            deliveryTimeWindow = slot.deliveryWindow,
            status = "CONFIRMED",
            paymentStatus = "PENDING_VERIFICATION", // Web orders start as pending verification for admin review
            itemsSummary = "1x $dishName",
            subtotal = amount,
            deliveryFee = 1.99,
            lateFee = 0.0,
            discount = 0.0,
            totalAmount = amount + 1.99,
            isLateOrder = false,
            deliveryAddress = "Tech Park Tower B, Suite 402",
            deliveryDesk = deliveryDesk,
            paymentMethod = "Web Portal Razorpay UPI",
            driverName = "Marcus Vance",
            driverPhone = "+1 (555) 782-9011",
            driverVehicle = "Eco-Electric Van #12",
            batchStopNumber = 2,
            totalBatchStops = 7,
            routeProgress = 0.0f
        )

        viewModelScope.launch {
            repository.insertOrder(webOrder)
            onSuccess(orderId)
        }
    }

    // Purchase Meal Pass
    fun purchaseMealPass(planName: String, totalMeals: Int) {
        val expiresDate = "Nov 15, 2026"
        val newSub = MealSubscriptionEntity(
            planName = planName,
            userEmail = _currentUser.value?.email ?: "alex@techcorp.com",
            remainingMeals = totalMeals,
            totalMeals = totalMeals,
            expiresDate = expiresDate,
            discountPercent = 15,
            hasFreeDelivery = true,
            isActive = true
        )
        viewModelScope.launch {
            repository.activateSubscription(newSub)
        }
    }

    // Support Ticket submission
    fun submitSupportTicket(
        orderId: String,
        issueType: String,
        subject: String,
        description: String,
        onSubmitted: () -> Unit
    ) {
        val ticketId = "TKT-${(1000..9999).random()}"
        val user = _currentUser.value
        val ticket = SupportTicketEntity(
            ticketId = ticketId,
            orderId = orderId,
            customerEmail = user?.email ?: "alex@techcorp.com",
            issueType = issueType,
            subject = subject,
            description = description,
            status = "OPEN",
            priority = if (issueType == "MISSING_ITEM" || issueType == "LATE_DELIVERY") "HIGH" else "NORMAL"
        )
        viewModelScope.launch {
            repository.insertTicket(ticket)
            val current = _chatMessages.value.toMutableList()
            current.add(
                ChatMessage(
                    sender = "system",
                    text = "📋 Support Ticket #$ticketId logged for Order #$orderId. Our operations team is reviewing it."
                )
            )
            _chatMessages.value = current
            onSubmitted()
        }
    }

    // AI Chatbot logic
    fun sendChatMessage(userText: String) {
        val current = _chatMessages.value.toMutableList()
        current.add(ChatMessage(sender = "user", text = userText))
        _chatMessages.value = current

        val lower = userText.lowercase()
        val botResponse: ChatMessage = when {
            lower.contains("cut") || lower.contains("time") || lower.contains("why") || lower.contains("9 am") -> {
                ChatMessage(
                    sender = "bot",
                    text = "⏰ Strict cut-offs are the secret to our 25% lower prices and zero food waste! Breakfast cut-off is 9:00 PM (night before), Lunch is 9:00 AM, and Dinner is 3:00 PM. At 9:01 AM, our partner kitchens receive exact counts and bulk-cook fresh meals."
                )
            }
            lower.contains("missed") || lower.contains("late") || lower.contains("past") -> {
                ChatMessage(
                    sender = "bot",
                    text = "⚡ Missed the cut-off? Don't worry! Our 'Late Express Menu' is active for 45 minutes after cut-off with a small $2.50 express fee so you never go hungry."
                )
            }
            lower.contains("where") || lower.contains("status") || lower.contains("driver") || lower.contains("track") -> {
                ChatMessage(
                    sender = "bot",
                    text = "🚚 Marcus Vance in Eco-Van #12 is currently delivering the lunch batch to Tech Park Tower B (Stop 3 of 7). Estimated arrival: 12:18 PM to your 4th-floor pantry!",
                    canEscalateToTicket = false
                )
            }
            lower.contains("missing") || lower.contains("damaged") || lower.contains("cold") || lower.contains("refund") || lower.contains("issue") -> {
                ChatMessage(
                    sender = "bot",
                    text = "I apologize for the issue! You can tap below to raise an immediate ticket or claim an instant $5.00 PreBite wallet credit.",
                    canEscalateToTicket = true
                )
            }
            lower.contains("pass") || lower.contains("subscription") -> {
                ChatMessage(
                    sender = "bot",
                    text = "🎟️ PreBite Routine Passes save you 15% on every meal, give you $0 delivery fees, and reserve your priority kitchen prep slot daily!"
                )
            }
            else -> {
                ChatMessage(
                    sender = "bot",
                    text = "Thank you for reaching out! You can check your active batch in the Batches tab, or reach out to our WhatsApp hotline if you need urgent dispatch assistance.",
                    canEscalateToTicket = true
                )
            }
        }

        viewModelScope.launch {
            delay(500)
            val updated = _chatMessages.value.toMutableList()
            updated.add(botResponse)
            _chatMessages.value = updated
        }
    }

    // --- Admin Operations ---
    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    fun updateOrderPaymentStatus(orderId: String, paymentStatus: String) {
        viewModelScope.launch {
            repository.updateOrderPaymentStatus(orderId, paymentStatus)
        }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            repository.deleteOrder(orderId)
        }
    }

    fun resolveTicket(ticketId: String, adminResponse: String, credit: Double) {
        viewModelScope.launch {
            repository.resolveTicket(ticketId, "RESOLVED", adminResponse, credit)
        }
    }

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
        restaurant: String,
        calories: Int,
        protein: Int,
        description: String
    ) {
        val newItem = MenuItemEntity(
            name = name,
            description = description.ifBlank { "Freshly prepared with local ingredients for bulk batching." },
            price = price,
            mealType = mealType,
            category = category,
            restaurantName = restaurant.ifBlank { "Urban Greens Kitchen" },
            calories = calories,
            proteinGrams = protein,
            rating = 4.8,
            isAvailable = true,
            isExpressAvailable = true,
            iconCategory = when (category) {
                "Wraps" -> "wrap"
                "Salads" -> "salad"
                "Chef Thali" -> "thali"
                else -> "bowl"
            }
        )
        viewModelScope.launch {
            repository.addMenuItem(newItem)
        }
    }

    fun deleteMenuItem(id: Long) {
        viewModelScope.launch {
            repository.deleteMenuItem(id)
        }
    }

    private fun startClockAndRouteTicker() {
        viewModelScope.launch {
            while (true) {
                delay(3000)
                if (_isTimeSimulationActive.value) {
                    val current = _simulatedTime.value
                    _simulatedTime.value = current.plusSeconds(3)
                } else {
                    _simulatedTime.value = LocalTime.now()
                }
            }
        }
    }
}

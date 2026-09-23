package com.example.data.repository

import com.example.data.local.MenuItemEntity
import com.example.data.local.MealSubscriptionEntity
import com.example.data.local.OrderEntity
import com.example.data.local.PreBiteDao
import com.example.data.local.SupportTicketEntity
import kotlinx.coroutines.flow.Flow

class PreBiteRepository(private val dao: PreBiteDao) {

    val allMenuItems: Flow<List<MenuItemEntity>> = dao.getAllMenuItems()
    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()
    val allTickets: Flow<List<SupportTicketEntity>> = dao.getAllTickets()
    val activeSubscription: Flow<MealSubscriptionEntity?> = dao.getActiveSubscription()

    fun getMenuItemsByMeal(mealType: String): Flow<List<MenuItemEntity>> {
        return dao.getMenuItemsByMeal(mealType)
    }

    fun getOrderById(orderId: String): Flow<OrderEntity?> {
        return dao.getOrderById(orderId)
    }

    fun getTicketsForOrder(orderId: String): Flow<List<SupportTicketEntity>> {
        return dao.getTicketsForOrder(orderId)
    }

    suspend fun insertOrder(order: OrderEntity) {
        dao.insertOrder(order)
    }

    suspend fun updateOrderStatus(orderId: String, status: String) {
        dao.updateOrderStatus(orderId, status)
    }

    suspend fun updateRouteProgress(orderId: String, progress: Float) {
        dao.updateRouteProgress(orderId, progress)
    }

    suspend fun insertTicket(ticket: SupportTicketEntity) {
        dao.insertTicket(ticket)
    }

    suspend fun resolveTicket(ticketId: String, status: String, adminResponse: String, credit: Double) {
        dao.resolveTicket(ticketId, status, adminResponse, credit)
    }

    suspend fun addMenuItem(item: MenuItemEntity): Long {
        return dao.insertMenuItem(item)
    }

    suspend fun updateItemAvailability(id: Long, isAvailable: Boolean) {
        dao.updateItemAvailability(id, isAvailable)
    }

    suspend fun deleteMenuItem(id: Long) {
        dao.deleteMenuItem(id)
    }

    suspend fun activateSubscription(sub: MealSubscriptionEntity) {
        dao.insertSubscription(sub)
    }

    suspend fun useSubscriptionMeal(id: Long) {
        dao.decrementSubscriptionMeal(id)
    }

    suspend fun seedInitialDataIfEmpty() {
        if (dao.getMenuItemCount() > 0) return

        val initialMenu = listOf(
            // Breakfast
            MenuItemEntity(
                name = "Berry Protein Overnight Oats",
                description = "Rolled oats infused with vanilla almond milk, chia seeds, fresh berries & organic honey.",
                price = 8.50,
                mealType = "BREAKFAST",
                category = "Healthy Bowls",
                restaurantName = "Fuel Bento Co.",
                calories = 420,
                proteinGrams = 28,
                rating = 4.9,
                isAvailable = true,
                isExpressAvailable = true,
                iconCategory = "oats"
            ),
            MenuItemEntity(
                name = "Avocado & Sourdough Platter",
                description = "Hass avocado mash, poached free-range eggs, microgreens on artisanal sourdough.",
                price = 9.75,
                mealType = "BREAKFAST",
                category = "High Protein",
                restaurantName = "Wholesome Hearth",
                calories = 480,
                proteinGrams = 18,
                rating = 4.8,
                isAvailable = true,
                isExpressAvailable = false,
                iconCategory = "salad"
            ),
            MenuItemEntity(
                name = "Masala Egg & Cheese Multigrain Wrap",
                description = "Fluffy spiced egg scramble, aged cheddar, crisp peppers & mint chutney.",
                price = 7.90,
                mealType = "BREAKFAST",
                category = "Wraps",
                restaurantName = "Delhi Spice Hub",
                calories = 410,
                proteinGrams = 22,
                rating = 4.7,
                isAvailable = true,
                isExpressAvailable = true,
                iconCategory = "wrap"
            ),
            MenuItemEntity(
                name = "Matcha Chia Superfood Bowl",
                description = "Ceremonial matcha pudding topped with roasted pumpkin seeds, goji berries & coconut flakes.",
                price = 7.25,
                mealType = "BREAKFAST",
                category = "Healthy Bowls",
                restaurantName = "Urban Greens Kitchen",
                calories = 340,
                proteinGrams = 12,
                rating = 4.9,
                isAvailable = true,
                isExpressAvailable = true,
                iconCategory = "bowl"
            ),

            // Lunch
            MenuItemEntity(
                name = "Grilled Citrus Salmon Quinoa Bowl",
                description = "Wild-caught salmon fillet, tricolor quinoa, steamed edamame, cucumber ribbon & sesame ponzu.",
                price = 14.50,
                mealType = "LUNCH",
                category = "Healthy Bowls",
                restaurantName = "Urban Greens Kitchen",
                calories = 560,
                proteinGrams = 44,
                rating = 4.95,
                isAvailable = true,
                isExpressAvailable = true,
                iconCategory = "bowl"
            ),
            MenuItemEntity(
                name = "Paneer Tikka Power Bento",
                description = "Charcoal cottage cheese skewers, turmeric brown rice, spiced lentils and cucumber raita.",
                price = 12.80,
                mealType = "LUNCH",
                category = "Chef Thali",
                restaurantName = "Delhi Spice Hub",
                calories = 520,
                proteinGrams = 26,
                rating = 4.85,
                isAvailable = true,
                isExpressAvailable = true,
                iconCategory = "thali"
            ),
            MenuItemEntity(
                name = "Smoked Tofu Crisp Caesar Salad",
                description = "Crispy herb croutons, cashew-parmesan dressing, shaved radishes, baby romaine.",
                price = 11.50,
                mealType = "LUNCH",
                category = "Salads",
                restaurantName = "Urban Greens Kitchen",
                calories = 390,
                proteinGrams = 20,
                rating = 4.75,
                isAvailable = true,
                isExpressAvailable = true,
                iconCategory = "salad"
            ),
            MenuItemEntity(
                name = "High-Protein Herb Chicken & Sweet Potato",
                description = "Sous-vide chicken breast, roasted rosemary sweet potato wedges, sautéed kale & garlic dip.",
                price = 13.90,
                mealType = "LUNCH",
                category = "High Protein",
                restaurantName = "Fuel Bento Co.",
                calories = 610,
                proteinGrams = 48,
                rating = 4.90,
                isAvailable = true,
                isExpressAvailable = true,
                iconCategory = "bowl"
            ),
            MenuItemEntity(
                name = "Artisan Falafel Hummus Platter",
                description = "Baked herb falafels, smoked paprika hummus, pickled turnips, tabouli and warm pita.",
                price = 12.00,
                mealType = "LUNCH",
                category = "Wraps",
                restaurantName = "Wholesome Hearth",
                calories = 490,
                proteinGrams = 18,
                rating = 4.70,
                isAvailable = true,
                isExpressAvailable = false,
                iconCategory = "wrap"
            ),

            // Dinner
            MenuItemEntity(
                name = "Slow-Braised Teriyaki Mushroom Soba",
                description = "Buckwheat soba noodles, shiitake & king oyster mushrooms, bok choy, toasted sesame oil.",
                price = 13.50,
                mealType = "DINNER",
                category = "Healthy Bowls",
                restaurantName = "Fuel Bento Co.",
                calories = 460,
                proteinGrams = 22,
                rating = 4.85,
                isAvailable = true,
                isExpressAvailable = true,
                iconCategory = "bowl"
            ),
            MenuItemEntity(
                name = "Royal Dal Makhani & Saffron Thali",
                description = "Overnight simmered black lentils in slow-cooked churned butter, saffron rice, roti & salad.",
                price = 13.20,
                mealType = "DINNER",
                category = "Chef Thali",
                restaurantName = "Delhi Spice Hub",
                calories = 580,
                proteinGrams = 24,
                rating = 4.90,
                isAvailable = true,
                isExpressAvailable = true,
                iconCategory = "thali"
            ),
            MenuItemEntity(
                name = "Lemon Rosemary Grilled Chicken Breast",
                description = "Free-range chicken breast grilled with lemon zest, steamed broccoli florets and brown rice pilaf.",
                price = 14.20,
                mealType = "DINNER",
                category = "High Protein",
                restaurantName = "Wholesome Hearth",
                calories = 510,
                proteinGrams = 45,
                rating = 4.80,
                isAvailable = true,
                isExpressAvailable = true,
                iconCategory = "bowl"
            ),
            MenuItemEntity(
                name = "Roasted Squash & Chickpea Warm Bowl",
                description = "Spiced butternut squash, crunchy chickpeas, wilted spinach and tahini maple dressing.",
                price = 12.50,
                mealType = "DINNER",
                category = "Healthy Bowls",
                restaurantName = "Urban Greens Kitchen",
                calories = 430,
                proteinGrams = 19,
                rating = 4.75,
                isAvailable = true,
                isExpressAvailable = false,
                iconCategory = "bowl"
            )
        )
        dao.insertMenuItems(initialMenu)

        // Seed an Active Order in delivery progress
        val activeOrder = OrderEntity(
            orderId = "PB-88421",
            createdAt = System.currentTimeMillis() - 25 * 60 * 1000,
            mealType = "LUNCH",
            targetDate = "Today",
            deliveryTimeWindow = "12:00 PM - 12:45 PM",
            status = "OUT_FOR_DELIVERY",
            itemsSummary = "1x Grilled Citrus Salmon Quinoa Bowl, 1x Berry Protein Overnight Oats",
            subtotal = 23.00,
            deliveryFee = 0.0, // Free with Pass
            lateFee = 0.0,
            discount = 3.45,
            totalAmount = 19.55,
            isLateOrder = false,
            deliveryAddress = "Tech Park Tower B, Suite 402",
            deliveryDesk = "Drop at 4th Floor Pantry / Reception",
            paymentMethod = "PreBite Meal Pass",
            driverName = "Marcus Vance",
            driverPhone = "+1 (555) 782-9011",
            driverVehicle = "Eco-Electric Van #12",
            batchStopNumber = 3,
            totalBatchStops = 7,
            routeProgress = 0.58f
        )
        dao.insertOrder(activeOrder)

        // Seed a Completed past Breakfast order
        val pastOrder = OrderEntity(
            orderId = "PB-87390",
            createdAt = System.currentTimeMillis() - 26 * 3600 * 1000,
            mealType = "BREAKFAST",
            targetDate = "Yesterday",
            deliveryTimeWindow = "7:30 AM - 8:15 AM",
            status = "DELIVERED",
            itemsSummary = "2x Masala Egg & Cheese Multigrain Wrap",
            subtotal = 15.80,
            deliveryFee = 1.99,
            lateFee = 0.0,
            discount = 0.0,
            totalAmount = 17.79,
            isLateOrder = false,
            deliveryAddress = "Tech Park Tower B, Suite 402",
            deliveryDesk = "Drop at 4th Floor Pantry / Reception",
            paymentMethod = "Stripe Card •••• 4242",
            driverName = "Sarah Connor",
            driverPhone = "+1 (555) 234-8890",
            driverVehicle = "Cargo E-Bike #08",
            batchStopNumber = 5,
            totalBatchStops = 5,
            routeProgress = 1.0f
        )
        dao.insertOrder(pastOrder)

        // Seed a sample Support Ticket
        val sampleTicket = SupportTicketEntity(
            ticketId = "TKT-1042",
            orderId = "PB-87390",
            issueType = "PACKAGING",
            subject = "Paper bag seal was slightly opened",
            description = "The food was warm and fine, but the hygiene paper sticker was torn on delivery.",
            status = "RESOLVED",
            priority = "NORMAL",
            createdAt = System.currentTimeMillis() - 20 * 3600 * 1000,
            adminResponse = "Thank you for the feedback! We inspected the batch seal process and added $5.00 PreBite credit to your account.",
            resolutionCredit = 5.00
        )
        dao.insertTicket(sampleTicket)

        // Seed Active Meal Pass
        val activePass = MealSubscriptionEntity(
            planName = "Weekly Routine Pass",
            remainingMeals = 8,
            totalMeals = 10,
            expiresDate = "Oct 04, 2026",
            discountPercent = 15,
            hasFreeDelivery = true,
            isActive = true
        )
        dao.insertSubscription(activePass)
    }
}

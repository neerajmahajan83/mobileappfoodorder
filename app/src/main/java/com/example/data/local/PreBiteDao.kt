package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PreBiteDao {

    // --- Menu Items ---
    @Query("SELECT * FROM menu_items ORDER BY id ASC")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE mealType = :mealType OR mealType = 'ALL' ORDER BY id ASC")
    fun getMenuItemsByMeal(mealType: String): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItemEntity): Long

    @Query("UPDATE menu_items SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun updateItemAvailability(id: Long, isAvailable: Boolean)

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteMenuItem(id: Long)

    @Query("SELECT COUNT(*) FROM menu_items")
    suspend fun getMenuItemCount(): Int

    // --- Orders ---
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    fun getOrderById(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE mealType = :mealType ORDER BY createdAt DESC")
    fun getOrdersByMeal(mealType: String): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("UPDATE orders SET routeProgress = :progress WHERE orderId = :orderId")
    suspend fun updateRouteProgress(orderId: String, progress: Float)

    // --- Support Tickets ---
    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets WHERE orderId = :orderId ORDER BY createdAt DESC")
    fun getTicketsForOrder(orderId: String): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Query("UPDATE support_tickets SET status = :status, adminResponse = :adminResponse, resolutionCredit = :credit WHERE ticketId = :ticketId")
    suspend fun resolveTicket(ticketId: String, status: String, adminResponse: String, credit: Double)

    // --- Subscriptions ---
    @Query("SELECT * FROM subscriptions WHERE isActive = 1 LIMIT 1")
    fun getActiveSubscription(): Flow<MealSubscriptionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: MealSubscriptionEntity)

    @Query("UPDATE subscriptions SET remainingMeals = remainingMeals - 1 WHERE id = :id AND remainingMeals > 0")
    suspend fun decrementSubscriptionMeal(id: Long)
}

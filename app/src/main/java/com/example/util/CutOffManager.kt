package com.example.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class MealSlot(
    val title: String,
    val cutOffHour: Int,
    val cutOffMinute: Int,
    val deliveryWindow: String,
    val prepTimeDesc: String
) {
    BREAKFAST(
        title = "Breakfast",
        cutOffHour = 21, // 9:00 PM (Previous night)
        cutOffMinute = 0,
        deliveryWindow = "7:30 AM - 8:30 AM",
        prepTimeDesc = "Kitchen preps batch at 5:30 AM"
    ),
    LUNCH(
        title = "Lunch",
        cutOffHour = 9, // 9:00 AM
        cutOffMinute = 0,
        deliveryWindow = "12:00 PM - 1:00 PM",
        prepTimeDesc = "Kitchen starts bulk prep at 9:01 AM"
    ),
    DINNER(
        title = "Dinner",
        cutOffHour = 15, // 3:00 PM
        cutOffMinute = 0,
        deliveryWindow = "6:30 PM - 7:45 PM",
        prepTimeDesc = "Kitchen starts bulk prep at 3:01 PM"
    );

    val cutOffDisplay: String
        get() = when (this) {
            BREAKFAST -> "9:00 PM (Night Before)"
            LUNCH -> "9:00 AM (Morning)"
            DINNER -> "3:00 PM (Afternoon)"
        }
}

enum class CutOffStatus {
    OPEN,
    LATE_EXPRESS, // Cut-off missed, express menu active with late fee
    CLOSED
}

data class MealSlotStatusInfo(
    val slot: MealSlot,
    val status: CutOffStatus,
    val timeRemainingText: String,
    val secondsRemaining: Long,
    val bannerMessage: String,
    val lateFee: Double = if (status == CutOffStatus.LATE_EXPRESS) 2.50 else 0.0,
    val deliveryNotice: String
)

object CutOffManager {

    fun getSlotStatus(slot: MealSlot, currentTime: LocalTime): MealSlotStatusInfo {
        val currentSeconds = currentTime.toSecondOfDay()
        val cutOffSeconds = slot.cutOffHour * 3600 + slot.cutOffMinute * 60

        return when (slot) {
            MealSlot.LUNCH -> {
                // Lunch cut-off is 9:00 AM (32400 seconds)
                if (currentSeconds < cutOffSeconds) {
                    val remaining = cutOffSeconds - currentSeconds
                    val hours = remaining / 3600
                    val mins = (remaining % 3600) / 60
                    val secs = remaining % 60
                    val text = if (hours > 0) "${hours}h ${mins}m left" else "${mins}m ${secs}s left"
                    MealSlotStatusInfo(
                        slot = slot,
                        status = CutOffStatus.OPEN,
                        timeRemainingText = text,
                        secondsRemaining = remaining.toLong(),
                        bannerMessage = "⚡ $text to order Lunch! Bulk kitchen prep starts at 9:01 AM",
                        deliveryNotice = "Guaranteed batch delivery: ${slot.deliveryWindow}"
                    )
                } else if (currentSeconds < cutOffSeconds + 90 * 60) { // Up to 10:30 AM: Late Express
                    val lateMins = (currentSeconds - cutOffSeconds) / 60
                    MealSlotStatusInfo(
                        slot = slot,
                        status = CutOffStatus.LATE_EXPRESS,
                        timeRemainingText = "Cut-off missed by ${lateMins}m",
                        secondsRemaining = 0,
                        bannerMessage = "⚠️ 9:00 AM Cut-Off Passed! Limited Express Menu Active (+ $2.50 Late Fee)",
                        lateFee = 2.50,
                        deliveryNotice = "Express Batch: Delivery delayed to 1:00 PM - 1:30 PM"
                    )
                } else {
                    MealSlotStatusInfo(
                        slot = slot,
                        status = CutOffStatus.CLOSED,
                        timeRemainingText = "Closed for Today",
                        secondsRemaining = 0,
                        bannerMessage = "🔒 Lunch batch is already dispatched! Pre-order for tomorrow.",
                        deliveryNotice = "Delivering tomorrow: ${slot.deliveryWindow}"
                    )
                }
            }
            MealSlot.DINNER -> {
                // Dinner cut-off is 3:00 PM (54000 seconds)
                if (currentSeconds < cutOffSeconds) {
                    val remaining = cutOffSeconds - currentSeconds
                    val hours = remaining / 3600
                    val mins = (remaining % 3600) / 60
                    val secs = remaining % 60
                    val text = if (hours > 0) "${hours}h ${mins}m left" else "${mins}m ${secs}s left"
                    MealSlotStatusInfo(
                        slot = slot,
                        status = CutOffStatus.OPEN,
                        timeRemainingText = text,
                        secondsRemaining = remaining.toLong(),
                        bannerMessage = "🍽️ $text to lock in Dinner! Batch prepped fresh at 3:01 PM",
                        deliveryNotice = "Guaranteed batch delivery: ${slot.deliveryWindow}"
                    )
                } else if (currentSeconds < cutOffSeconds + 90 * 60) {
                    val lateMins = (currentSeconds - cutOffSeconds) / 60
                    MealSlotStatusInfo(
                        slot = slot,
                        status = CutOffStatus.LATE_EXPRESS,
                        timeRemainingText = "Cut-off missed by ${lateMins}m",
                        secondsRemaining = 0,
                        bannerMessage = "⚠️ 3:00 PM Cut-Off Passed! Express Dinner Active (+ $2.50 Late Fee)",
                        lateFee = 2.50,
                        deliveryNotice = "Express Batch: Delivery delayed to 7:45 PM - 8:15 PM"
                    )
                } else {
                    MealSlotStatusInfo(
                        slot = slot,
                        status = CutOffStatus.CLOSED,
                        timeRemainingText = "Closed for Today",
                        secondsRemaining = 0,
                        bannerMessage = "🔒 Dinner batches are in route! Pre-order for tomorrow evening.",
                        deliveryNotice = "Delivering tomorrow: ${slot.deliveryWindow}"
                    )
                }
            }
            MealSlot.BREAKFAST -> {
                // Breakfast cut-off is 9:00 PM (75600 seconds) for NEXT DAY morning delivery
                if (currentSeconds < cutOffSeconds) {
                    val remaining = cutOffSeconds - currentSeconds
                    val hours = remaining / 3600
                    val mins = (remaining % 3600) / 60
                    val secs = remaining % 60
                    val text = if (hours > 0) "${hours}h ${mins}m left" else "${mins}m ${secs}s left"
                    MealSlotStatusInfo(
                        slot = slot,
                        status = CutOffStatus.OPEN,
                        timeRemainingText = text,
                        secondsRemaining = remaining.toLong(),
                        bannerMessage = "☀️ Order by 9:00 PM tonight for tomorrow morning's batch!",
                        deliveryNotice = "Guaranteed morning delivery: ${slot.deliveryWindow}"
                    )
                } else {
                    MealSlotStatusInfo(
                        slot = slot,
                        status = CutOffStatus.CLOSED,
                        timeRemainingText = "Batch locked for tomorrow",
                        secondsRemaining = 0,
                        bannerMessage = "🔒 Breakfast batch for tomorrow locked in kitchen prep.",
                        deliveryNotice = "Next open batch: Morning after tomorrow"
                    )
                }
            }
        }
    }
}

package com.example.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

val OLD_SAMPLE_NUMBERS = setOf(
    "9824055511", "9898033221", "9825123456", "9879012345",
    "9723045678", "9909067890", "9825012345", "+91 98250 12345"
)

fun formatContactNumber(phone: String): String {
    val trimmed = phone.trim()
    if (trimmed.isBlank()) return ""
    if (trimmed.contains("XXXXX") || trimmed in OLD_SAMPLE_NUMBERS) {
        return "+91 XXXXX XX444"
    }
    val digits = trimmed.filter { it.isDigit() }
    return when {
        digits.length == 10 -> "+91 $digits"
        digits.length == 12 && digits.startsWith("91") -> "+91 ${digits.substring(2)}"
        else -> trimmed
    }
}


@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mobile: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    @get:Ignore
    val displayMobile: String
        get() = formatContactNumber(mobile)
}

@Entity(tableName = "events")
data class EventBooking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val customerMobile: String,
    val eventName: String,
    val eventDate: String, // e.g. "28 Sep 2026"
    val eventDateMillis: Long,
    val eventTime: String, // e.g. "10:00 AM"
    val eventLocation: String,
    val crewIdsCsv: String, // Comma-separated crew IDs e.g. "1,3"
    val crewNames: String, // Snapshot of names e.g. "Ramesh (Photographer), Suresh (Drone)"
    val dressCodeNote: String = "",
    val reminderEnabled: Boolean = false,
    val reminderPreset: String = "none", // "1_day", "12_hours", "6_hours", "3_hours", "1_hour", "custom"
    val reminderTimeMillis: Long? = null,
    val status: String = STATUS_UPCOMING, // "Upcoming" or "Completed"
    val createdAt: Long = System.currentTimeMillis()
) {
    @get:Ignore
    val displayCustomerMobile: String
        get() = formatContactNumber(customerMobile)

    companion object {
        const val STATUS_UPCOMING = "Upcoming"
        const val STATUS_COMPLETED = "Completed"
    }
}

@Entity(tableName = "crew_members")
data class CrewMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mobile: String,
    val role: String, // "Photographer", "Videographer", "Drone Pilot", "Editor"
    val cameraEquipment: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    @get:Ignore
    val displayMobile: String
        get() = formatContactNumber(mobile)
}

data class StudioProfile(
    val studioName: String = "Maruti Photo Studio",
    val ownerName: String = "Kishorbhai Patel",
    val mobileNumber: String = "+91 XXXXX XX444",
    val whatsAppNumber: String = "+91 XXXXX XX444",
    val address: String = "India",
    val logoUri: String = "",
    val lastBackupTime: Long = 0L
) {
    val displayMobile: String
        get() = formatContactNumber(mobileNumber)

    val displayWhatsApp: String
        get() = formatContactNumber(whatsAppNumber)
}

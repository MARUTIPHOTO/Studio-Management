package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mobile: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

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
)

data class StudioProfile(
    val studioName: String = "Maruti Photo Studio",
    val ownerName: String = "Kishorbhai Patel",
    val mobileNumber: String = "+91 98250 12345",
    val whatsAppNumber: String = "+91 98250 12345",
    val address: String = "India",
    val logoUri: String = "",
    val lastBackupTime: Long = 0L
)

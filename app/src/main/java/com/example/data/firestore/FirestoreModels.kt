package com.example.data.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreCustomer(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val mobile: String = "",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreEventBooking(
    @DocumentId val id: String = "",
    val userId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerMobile: String = "",
    val eventName: String = "",
    val eventDate: String = "",
    val eventDateMillis: Long = 0L,
    val eventTime: String = "",
    val eventLocation: String = "",
    val crewIdsCsv: String = "",
    val crewNames: String = "",
    val dressCodeNote: String = "",
    val reminderEnabled: Boolean = false,
    val reminderPreset: String = "none",
    val status: String = "Upcoming",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreCrewMember(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val mobile: String = "",
    val role: String = "",
    val cameraEquipment: String = "",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreStudioProfile(
    val studioName: String = "",
    val ownerName: String = "",
    val mobileNumber: String = "",
    val whatsAppNumber: String = "",
    val address: String = "",
    val logoUri: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

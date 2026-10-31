package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.CrewMember
import com.example.data.model.Customer
import com.example.data.model.EventBooking
import com.example.data.model.StudioProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSyncManager(private val context: Context) {
    private val firestore: FirebaseFirestore = try {
        val dbId = context.getString(R.string.firestore_database_id)
        if (dbId.isNotBlank() && dbId != "(default)") {
            FirebaseFirestore.getInstance(dbId)
        } else {
            FirebaseFirestore.getInstance()
        }
    } catch (e: Exception) {
        FirebaseFirestore.getInstance()
    }
    private val localDb = AppDatabase.getInstance(context)
    private val customerDao = localDb.customerDao()
    private val eventDao = localDb.eventDao()
    private val crewDao = localDb.crewDao()

    private fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    suspend fun syncAllFromCloud(): Result<Int> = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext Result.failure(Exception("User not authenticated"))
        try {
            var itemsSynced = 0

            // 1. Fetch Customers
            val customersSnapshot = firestore.collection("users").document(uid)
                .collection("customers")
                .get()
                .await()

            val cloudCustomers = customersSnapshot.documents.mapNotNull { doc ->
                val idLong = doc.id.toLongOrNull() ?: return@mapNotNull null
                val name = doc.getString("name") ?: ""
                val mobile = doc.getString("mobile") ?: ""
                val note = doc.getString("note") ?: ""
                Customer(
                    id = idLong,
                    name = name,
                    mobile = mobile,
                    note = note
                )
            }
            if (cloudCustomers.isNotEmpty()) {
                customerDao.insertAll(cloudCustomers)
                itemsSynced += cloudCustomers.size
            }

            // 2. Fetch Events
            val eventsSnapshot = firestore.collection("users").document(uid)
                .collection("events")
                .get()
                .await()

            val cloudEvents = eventsSnapshot.documents.mapNotNull { doc ->
                val idLong = doc.id.toLongOrNull() ?: return@mapNotNull null
                val customerIdLong = doc.getString("customerId")?.toLongOrNull() ?: 0L
                val customerName = doc.getString("customerName") ?: ""
                val customerMobile = doc.getString("customerMobile") ?: ""
                val eventName = doc.getString("eventName") ?: ""
                val eventDate = doc.getString("eventDate") ?: ""
                val eventDateMillis = doc.getLong("eventDateMillis") ?: 0L
                val eventTime = doc.getString("eventTime") ?: ""
                val eventLocation = doc.getString("eventLocation") ?: ""
                val crewIdsCsv = doc.getString("crewIdsCsv") ?: ""
                val crewNames = doc.getString("crewNames") ?: ""
                val dressCodeNote = doc.getString("dressCodeNote") ?: ""
                val reminderEnabled = doc.getBoolean("reminderEnabled") ?: false
                val reminderPreset = doc.getString("reminderPreset") ?: "none"
                val status = doc.getString("status") ?: EventBooking.STATUS_UPCOMING

                EventBooking(
                    id = idLong,
                    customerId = customerIdLong,
                    customerName = customerName,
                    customerMobile = customerMobile,
                    eventName = eventName,
                    eventDate = eventDate,
                    eventDateMillis = eventDateMillis,
                    eventTime = eventTime,
                    eventLocation = eventLocation,
                    crewIdsCsv = crewIdsCsv,
                    crewNames = crewNames,
                    dressCodeNote = dressCodeNote,
                    reminderEnabled = reminderEnabled,
                    reminderPreset = reminderPreset,
                    status = status
                )
            }
            if (cloudEvents.isNotEmpty()) {
                eventDao.insertAll(cloudEvents)
                itemsSynced += cloudEvents.size
            }

            // 3. Fetch Crew
            val crewSnapshot = firestore.collection("users").document(uid)
                .collection("crew")
                .get()
                .await()

            val cloudCrew = crewSnapshot.documents.mapNotNull { doc ->
                val idLong = doc.id.toLongOrNull() ?: return@mapNotNull null
                val name = doc.getString("name") ?: ""
                val mobile = doc.getString("mobile") ?: ""
                val role = doc.getString("role") ?: "Photographer"
                val cameraEquipment = doc.getString("cameraEquipment") ?: ""
                val note = doc.getString("note") ?: ""
                CrewMember(
                    id = idLong,
                    name = name,
                    mobile = mobile,
                    role = role,
                    cameraEquipment = cameraEquipment,
                    note = note
                )
            }
            if (cloudCrew.isNotEmpty()) {
                crewDao.insertAll(cloudCrew)
                itemsSynced += cloudCrew.size
            }

            // 4. Fetch User Profile
            val profileDoc = firestore.collection("users").document(uid).get().await()
            if (profileDoc.exists()) {
                val studioName = profileDoc.getString("studioName") ?: "Maruti Photo Studio"
                val ownerName = profileDoc.getString("ownerName") ?: "Kishorbhai Patel"
                val mobileNumber = profileDoc.getString("mobileNumber") ?: ""
                val whatsAppNumber = profileDoc.getString("whatsAppNumber") ?: ""
                val address = profileDoc.getString("address") ?: ""
                val logoUri = profileDoc.getString("logoUri") ?: ""

                val prefs = context.getSharedPreferences("maruti_studio_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("studio_name", studioName)
                    .putString("owner_name", ownerName)
                    .putString("mobile_number", mobileNumber)
                    .putString("whatsapp_number", whatsAppNumber)
                    .putString("address", address)
                    .putString("logo_uri", logoUri)
                    .putLong("last_backup_time", System.currentTimeMillis())
                    .apply()
            }

            Log.d(TAG, "Synced $itemsSynced items successfully from Firestore")
            Result.success(itemsSynced)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing from Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun saveCustomerToCloud(customer: Customer) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        try {
            val docRef = firestore.collection("users").document(uid)
                .collection("customers").document(customer.id.toString())

            val data = hashMapOf<String, Any>(
                "userId" to uid,
                "name" to customer.name,
                "mobile" to customer.mobile,
                "note" to customer.note,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save customer to cloud", e)
        }
    }

    suspend fun deleteCustomerFromCloud(customerId: Long) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        try {
            firestore.collection("users").document(uid)
                .collection("customers").document(customerId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete customer from cloud", e)
        }
    }

    suspend fun saveEventToCloud(event: EventBooking) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        try {
            val docRef = firestore.collection("users").document(uid)
                .collection("events").document(event.id.toString())

            val data = hashMapOf<String, Any>(
                "userId" to uid,
                "customerId" to event.customerId.toString(),
                "customerName" to event.customerName,
                "customerMobile" to event.customerMobile,
                "eventName" to event.eventName,
                "eventDate" to event.eventDate,
                "eventDateMillis" to event.eventDateMillis,
                "eventTime" to event.eventTime,
                "eventLocation" to event.eventLocation,
                "crewIdsCsv" to event.crewIdsCsv,
                "crewNames" to event.crewNames,
                "dressCodeNote" to event.dressCodeNote,
                "reminderEnabled" to event.reminderEnabled,
                "reminderPreset" to event.reminderPreset,
                "status" to event.status,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save event to cloud", e)
        }
    }

    suspend fun deleteEventFromCloud(eventId: Long) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        try {
            firestore.collection("users").document(uid)
                .collection("events").document(eventId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete event from cloud", e)
        }
    }

    suspend fun saveCrewToCloud(crew: CrewMember) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        try {
            val docRef = firestore.collection("users").document(uid)
                .collection("crew").document(crew.id.toString())

            val data = hashMapOf<String, Any>(
                "userId" to uid,
                "name" to crew.name,
                "mobile" to crew.mobile,
                "role" to crew.role,
                "cameraEquipment" to crew.cameraEquipment,
                "note" to crew.note,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save crew to cloud", e)
        }
    }

    suspend fun deleteCrewFromCloud(crewId: Long) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        try {
            firestore.collection("users").document(uid)
                .collection("crew").document(crewId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete crew from cloud", e)
        }
    }

    suspend fun saveProfileToCloud(profile: StudioProfile) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        try {
            val docRef = firestore.collection("users").document(uid)
            val data = hashMapOf<String, Any>(
                "studioName" to profile.studioName,
                "ownerName" to profile.ownerName,
                "mobileNumber" to profile.mobileNumber,
                "whatsAppNumber" to profile.whatsAppNumber,
                "address" to profile.address,
                "logoUri" to profile.logoUri,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save profile to cloud", e)
        }
    }

    companion object {
        private const val TAG = "FirestoreSyncManager"
    }
}

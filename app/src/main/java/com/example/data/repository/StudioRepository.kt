package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.model.CrewMember
import com.example.data.model.Customer
import com.example.data.model.EventBooking
import com.example.data.model.StudioProfile
import com.example.ui.strings.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StudioRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val customerDao = db.customerDao()
    private val eventDao = db.eventDao()
    private val crewDao = db.crewDao()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("maruti_studio_prefs", Context.MODE_PRIVATE)

    // Flow getters
    val customers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val events: Flow<List<EventBooking>> = eventDao.getAllEvents()
    val crewMembers: Flow<List<CrewMember>> = crewDao.getAllCrew()

    fun getEventsForCustomer(customerId: Long): Flow<List<EventBooking>> {
        return eventDao.getEventsForCustomer(customerId)
    }

    suspend fun getCustomerById(id: Long): Customer? = withContext(Dispatchers.IO) {
        customerDao.getCustomerById(id)
    }

    suspend fun findCustomerByMobile(mobile: String): Customer? = withContext(Dispatchers.IO) {
        val clean = normalizePhone(mobile)
        val direct = customerDao.findCustomerByMobile(clean)
        if (direct != null) return@withContext direct
        // Also check if any existing customer has matching normalized digits
        val all = customerDao.getAllCustomers()
        // direct lookup is primary
        direct
    }

    suspend fun getEventById(id: Long): EventBooking? = withContext(Dispatchers.IO) {
        eventDao.getEventById(id)
    }

    suspend fun getEventsOnDate(eventDate: String): List<EventBooking> = withContext(Dispatchers.IO) {
        eventDao.getEventsOnDate(eventDate.trim())
    }

    suspend fun insertCustomer(customer: Customer): Long = withContext(Dispatchers.IO) {
        customerDao.insertCustomer(customer.copy(mobile = normalizePhone(customer.mobile)))
    }

    suspend fun updateCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        val clean = customer.copy(mobile = normalizePhone(customer.mobile))
        customerDao.updateCustomer(clean)
        eventDao.updateCustomerInfoForEvents(clean.id, clean.name, clean.mobile)
    }

    suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        // Also remove events for this customer
        eventDao.deleteEventsForCustomer(customer.id)
        customerDao.deleteCustomer(customer)
    }

    suspend fun insertEvent(event: EventBooking): Long = withContext(Dispatchers.IO) {
        eventDao.insertEvent(event.copy(customerMobile = normalizePhone(event.customerMobile)))
    }

    suspend fun updateEvent(event: EventBooking) = withContext(Dispatchers.IO) {
        eventDao.updateEvent(event.copy(customerMobile = normalizePhone(event.customerMobile)))
    }

    suspend fun deleteEvent(event: EventBooking) = withContext(Dispatchers.IO) {
        eventDao.deleteEvent(event)
    }

    suspend fun findDuplicateEvents(
        customerId: Long,
        eventName: String,
        eventDate: String,
        eventTime: String
    ): List<EventBooking> = withContext(Dispatchers.IO) {
        eventDao.findDuplicateEvents(customerId, eventName.trim(), eventDate.trim(), eventTime.trim())
    }

    suspend fun findDuplicateEventsExcluding(
        customerId: Long,
        eventName: String,
        eventDate: String,
        eventTime: String,
        excludeEventId: Long
    ): List<EventBooking> = withContext(Dispatchers.IO) {
        eventDao.findDuplicateEventsExcluding(customerId, eventName.trim(), eventDate.trim(), eventTime.trim(), excludeEventId)
    }

    suspend fun insertCrew(crew: CrewMember): Long = withContext(Dispatchers.IO) {
        crewDao.insertCrew(crew.copy(mobile = normalizePhone(crew.mobile)))
    }

    suspend fun updateCrew(crew: CrewMember) = withContext(Dispatchers.IO) {
        crewDao.updateCrew(crew.copy(mobile = normalizePhone(crew.mobile)))
    }

    suspend fun deleteCrew(crew: CrewMember) = withContext(Dispatchers.IO) {
        // Remove crew ID from any existing event bookings
        try {
            val allEvents = eventDao.getAllEventsSync()
            allEvents.forEach { ev ->
                val ids = ev.crewIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
                if (ids.contains(crew.id)) {
                    val updatedIds = ids.filter { it != crew.id }
                    val updatedIdsStr = updatedIds.joinToString(",")
                    // Update crewNames snapshot
                    val updatedNames = ev.crewNames.split(", ")
                        .filter { !it.startsWith(crew.name) }
                        .joinToString(", ")
                    eventDao.updateEvent(ev.copy(crewIdsCsv = updatedIdsStr, crewNames = updatedNames))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        crewDao.deleteCrew(crew)
    }

    suspend fun findCrewByMobile(mobile: String): CrewMember? = withContext(Dispatchers.IO) {
        val clean = normalizePhone(mobile)
        crewDao.findCrewByMobile(clean)
    }

    companion object {
        fun normalizePhone(phone: String): String {
            val digits = phone.filter { it.isDigit() }
            return when {
                digits.length == 10 -> digits
                digits.length == 11 && digits.startsWith("0") -> digits.substring(1)
                digits.length == 12 && digits.startsWith("91") -> digits.substring(2)
                else -> if (digits.isNotBlank()) digits else phone.trim()
            }
        }
    }

    // Studio Profile
    fun getStudioProfile(): StudioProfile {
        return StudioProfile(
            studioName = prefs.getString("studio_name", "Maruti Photo Studio") ?: "Maruti Photo Studio",
            ownerName = prefs.getString("owner_name", "Kishorbhai Patel") ?: "Kishorbhai Patel",
            mobileNumber = prefs.getString("mobile_number", "9825012345") ?: "9825012345",
            whatsAppNumber = prefs.getString("whatsapp_number", "9825012345") ?: "9825012345",
            address = prefs.getString("address", "India") ?: "India",
            logoUri = prefs.getString("logo_uri", "") ?: "",
            lastBackupTime = prefs.getLong("last_backup_time", 0L)
        )
    }

    fun saveStudioProfile(profile: StudioProfile) {
        prefs.edit()
            .putString("studio_name", profile.studioName)
            .putString("owner_name", profile.ownerName)
            .putString("mobile_number", profile.mobileNumber)
            .putString("whatsapp_number", profile.whatsAppNumber)
            .putString("address", profile.address)
            .putString("logo_uri", profile.logoUri)
            .apply()
    }

    // Language
    fun getLanguage(): AppLanguage {
        val code = prefs.getString("app_language", AppLanguage.GUJARATI.code)
        return AppLanguage.entries.find { it.code == code } ?: AppLanguage.GUJARATI
    }

    fun saveLanguage(language: AppLanguage) {
        prefs.edit().putString("app_language", language.code).apply()
    }

    // Backup & Restore
    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val customerList = db.query("SELECT * FROM customers", null)
        val eventList = db.query("SELECT * FROM events", null)
        val crewList = db.query("SELECT * FROM crew_members", null)

        val customersArr = JSONArray()
        if (customerList.moveToFirst()) {
            do {
                val obj = JSONObject()
                obj.put("id", customerList.getLong(customerList.getColumnIndexOrThrow("id")))
                obj.put("name", customerList.getString(customerList.getColumnIndexOrThrow("name")))
                obj.put("mobile", customerList.getString(customerList.getColumnIndexOrThrow("mobile")))
                obj.put("note", customerList.getString(customerList.getColumnIndexOrThrow("note")))
                customersArr.put(obj)
            } while (customerList.moveToNext())
        }
        customerList.close()

        val eventsArr = JSONArray()
        if (eventList.moveToFirst()) {
            do {
                val obj = JSONObject()
                obj.put("id", eventList.getLong(eventList.getColumnIndexOrThrow("id")))
                obj.put("customerId", eventList.getLong(eventList.getColumnIndexOrThrow("customerId")))
                obj.put("customerName", eventList.getString(eventList.getColumnIndexOrThrow("customerName")))
                obj.put("customerMobile", eventList.getString(eventList.getColumnIndexOrThrow("customerMobile")))
                obj.put("eventName", eventList.getString(eventList.getColumnIndexOrThrow("eventName")))
                obj.put("eventDate", eventList.getString(eventList.getColumnIndexOrThrow("eventDate")))
                obj.put("eventDateMillis", eventList.getLong(eventList.getColumnIndexOrThrow("eventDateMillis")))
                obj.put("eventTime", eventList.getString(eventList.getColumnIndexOrThrow("eventTime")))
                obj.put("eventLocation", eventList.getString(eventList.getColumnIndexOrThrow("eventLocation")))
                obj.put("crewIdsCsv", eventList.getString(eventList.getColumnIndexOrThrow("crewIdsCsv")))
                obj.put("crewNames", eventList.getString(eventList.getColumnIndexOrThrow("crewNames")))
                obj.put("dressCodeNote", eventList.getString(eventList.getColumnIndexOrThrow("dressCodeNote")))
                obj.put("reminderEnabled", eventList.getInt(eventList.getColumnIndexOrThrow("reminderEnabled")) == 1)
                obj.put("reminderPreset", eventList.getString(eventList.getColumnIndexOrThrow("reminderPreset")))
                obj.put("status", eventList.getString(eventList.getColumnIndexOrThrow("status")))
                eventsArr.put(obj)
            } while (eventList.moveToNext())
        }
        eventList.close()

        val crewArr = JSONArray()
        if (crewList.moveToFirst()) {
            do {
                val obj = JSONObject()
                obj.put("id", crewList.getLong(crewList.getColumnIndexOrThrow("id")))
                obj.put("name", crewList.getString(crewList.getColumnIndexOrThrow("name")))
                obj.put("mobile", crewList.getString(crewList.getColumnIndexOrThrow("mobile")))
                obj.put("role", crewList.getString(crewList.getColumnIndexOrThrow("role")))
                obj.put("cameraEquipment", crewList.getString(crewList.getColumnIndexOrThrow("cameraEquipment")))
                obj.put("note", crewList.getString(crewList.getColumnIndexOrThrow("note")))
                crewArr.put(obj)
            } while (crewList.moveToNext())
        }
        crewList.close()

        val currentProfile = getStudioProfile()
        val profileObj = JSONObject().apply {
            put("studioName", currentProfile.studioName)
            put("ownerName", currentProfile.ownerName)
            put("mobileNumber", currentProfile.mobileNumber)
            put("whatsAppNumber", currentProfile.whatsAppNumber)
            put("address", currentProfile.address)
            put("logoUri", currentProfile.logoUri)
        }
        root.put("profile", profileObj)
        root.put("customers", customersArr)
        root.put("events", eventsArr)
        root.put("crew", crewArr)
        root.put("backupDate", System.currentTimeMillis())

        val backupStr = root.toString(2)
        prefs.edit()
            .putString("backup_snapshot", backupStr)
            .putLong("last_backup_time", System.currentTimeMillis())
            .apply()

        backupStr
    }

    suspend fun restoreBackup(): Boolean = withContext(Dispatchers.IO) {
        val backupStr = prefs.getString("backup_snapshot", null) ?: return@withContext false
        try {
            val root = JSONObject(backupStr)
            val customersArr = root.optJSONArray("customers") ?: JSONArray()
            val eventsArr = root.optJSONArray("events") ?: JSONArray()
            val crewArr = root.optJSONArray("crew") ?: JSONArray()

            val restoredCustomers = mutableListOf<Customer>()
            for (i in 0 until customersArr.length()) {
                val o = customersArr.getJSONObject(i)
                restoredCustomers.add(
                    Customer(
                        id = o.optLong("id", 0L),
                        name = o.optString("name"),
                        mobile = o.optString("mobile"),
                        note = o.optString("note")
                    )
                )
            }

            val restoredEvents = mutableListOf<EventBooking>()
            for (i in 0 until eventsArr.length()) {
                val o = eventsArr.getJSONObject(i)
                restoredEvents.add(
                    EventBooking(
                        id = o.optLong("id", 0L),
                        customerId = o.optLong("customerId"),
                        customerName = o.optString("customerName"),
                        customerMobile = o.optString("customerMobile"),
                        eventName = o.optString("eventName"),
                        eventDate = o.optString("eventDate"),
                        eventDateMillis = o.optLong("eventDateMillis", System.currentTimeMillis()),
                        eventTime = o.optString("eventTime"),
                        eventLocation = o.optString("eventLocation"),
                        crewIdsCsv = o.optString("crewIdsCsv"),
                        crewNames = o.optString("crewNames"),
                        dressCodeNote = o.optString("dressCodeNote"),
                        reminderEnabled = o.optBoolean("reminderEnabled"),
                        reminderPreset = o.optString("reminderPreset", "none"),
                        status = o.optString("status", EventBooking.STATUS_UPCOMING)
                    )
                )
            }

            val restoredCrew = mutableListOf<CrewMember>()
            for (i in 0 until crewArr.length()) {
                val o = crewArr.getJSONObject(i)
                restoredCrew.add(
                    CrewMember(
                        id = o.optLong("id", 0L),
                        name = o.optString("name"),
                        mobile = o.optString("mobile"),
                        role = o.optString("role"),
                        cameraEquipment = o.optString("cameraEquipment"),
                        note = o.optString("note")
                    )
                )
            }

            customerDao.deleteAll()
            eventDao.deleteAll()
            crewDao.deleteAll()

            customerDao.insertAll(restoredCustomers)
            eventDao.insertAll(restoredEvents)
            crewDao.insertAll(restoredCrew)

            val profileObj = root.optJSONObject("profile")
            if (profileObj != null) {
                val restoredProfile = StudioProfile(
                    studioName = profileObj.optString("studioName", "Maruti Photo Studio"),
                    ownerName = profileObj.optString("ownerName", "Kishorbhai Patel"),
                    mobileNumber = profileObj.optString("mobileNumber", "9825012345"),
                    whatsAppNumber = profileObj.optString("whatsAppNumber", "9825012345"),
                    address = profileObj.optString("address", "India"),
                    logoUri = profileObj.optString("logoUri", ""),
                    lastBackupTime = System.currentTimeMillis()
                )
                saveStudioProfile(restoredProfile)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Prepopulate realistic initial data if database is empty
    suspend fun prepopulateIfEmpty() = withContext(Dispatchers.IO) {
        val hasSampleInitialized = prefs.getBoolean("has_sample_initialized", false)
        if (hasSampleInitialized) return@withContext

        // Crew
        val crew1Id = crewDao.insertCrew(
            CrewMember(
                name = "Pravin Patel",
                mobile = "9825123456",
                role = "Photographer",
                cameraEquipment = "Sony A7 IV, 24-70mm GM II",
                note = "Lead Portrait & Candid Specialist"
            )
        )
        val crew2Id = crewDao.insertCrew(
            CrewMember(
                name = "Mahesh Gohil",
                mobile = "9879012345",
                role = "Videographer",
                cameraEquipment = "Sony FX3, Ronin RS3 Pro",
                note = "Cinematic video & Gimbal operator"
            )
        )
        val crew3Id = crewDao.insertCrew(
            CrewMember(
                name = "Dipen Shah",
                mobile = "9723045678",
                role = "Drone Pilot",
                cameraEquipment = "DJI Mavic 3 Pro, DJI Avata",
                note = "Aerial 4K coverage expert"
            )
        )
        crewDao.insertCrew(
            CrewMember(
                name = "Chetan Parmar",
                mobile = "9909067890",
                role = "Editor",
                cameraEquipment = "Mac Studio M2 Max, Premiere & DaVinci",
                note = "Album design & video colorist"
            )
        )

        // Customer
        val custId1 = customerDao.insertCustomer(
            Customer(
                name = "Rajesh Patel",
                mobile = "9824055511",
                note = "Royal Party Plot booking, sister's wedding celebrations"
            )
        )
        val custId2 = customerDao.insertCustomer(
            Customer(
                name = "Sanjaybhai Vaghani",
                mobile = "9898033221",
                note = "Daughter's reception at Sarthana Convention Hall"
            )
        )

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 2)
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val date1 = dateFormat.format(cal.time)
        val date1Millis = cal.timeInMillis

        cal.add(Calendar.DAY_OF_YEAR, 1)
        val date2 = dateFormat.format(cal.time)
        val date2Millis = cal.timeInMillis

        // Events for Rajesh Patel (Haldi, Wedding)
        eventDao.insertEvent(
            EventBooking(
                customerId = custId1,
                customerName = "Rajesh Patel",
                customerMobile = "9824055511",
                eventName = "Haldi",
                eventDate = date1,
                eventDateMillis = date1Millis,
                eventTime = "10:00 AM",
                eventLocation = "Gaypagla, India",
                crewIdsCsv = "$crew1Id,$crew2Id",
                crewNames = "Pravin Patel (Photographer), Mahesh Gohil (Videographer)",
                dressCodeNote = "Yellow Traditional Dress, Haldi Splash Entry",
                reminderEnabled = true,
                reminderPreset = "1_day",
                status = EventBooking.STATUS_UPCOMING
            )
        )

        eventDao.insertEvent(
            EventBooking(
                customerId = custId1,
                customerName = "Rajesh Patel",
                customerMobile = "9824055511",
                eventName = "Wedding",
                eventDate = date2,
                eventDateMillis = date2Millis,
                eventTime = "12:00 PM",
                eventLocation = "Royal Heritage Resort, India",
                crewIdsCsv = "$crew1Id,$crew2Id,$crew3Id",
                crewNames = "Pravin Patel, Mahesh Gohil, Dipen Shah (Drone)",
                dressCodeNote = "Royal Traditional Sherwani, Drone Entry Coverage",
                reminderEnabled = true,
                reminderPreset = "12_hours",
                status = EventBooking.STATUS_UPCOMING
            )
        )

        // Event for Sanjaybhai Vaghani
        eventDao.insertEvent(
            EventBooking(
                customerId = custId2,
                customerName = "Sanjaybhai Vaghani",
                customerMobile = "9898033221",
                eventName = "Reception",
                eventDate = date2,
                eventDateMillis = date2Millis,
                eventTime = "07:30 PM",
                eventLocation = "Sarthana Nature Park Banquet, India",
                crewIdsCsv = "$crew1Id,$crew2Id",
                crewNames = "Pravin Patel (Photographer), Mahesh Gohil (Videographer)",
                dressCodeNote = "Formal Suit / Indo-Western, LED Wall Photo Stage",
                reminderEnabled = true,
                reminderPreset = "6_hours",
                status = EventBooking.STATUS_UPCOMING
            )
        )

        prefs.edit().putBoolean("has_sample_initialized", true).apply()
    }
}

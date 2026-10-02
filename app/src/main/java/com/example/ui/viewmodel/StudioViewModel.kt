package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CrewMember
import com.example.data.model.Customer
import com.example.data.model.EventBooking
import com.example.data.model.StudioProfile
import com.example.data.repository.StudioRepository
import com.example.ui.strings.AppLanguage
import com.example.ui.strings.AppStrings
import com.example.ui.strings.StringsDefinition
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

sealed class Screen {
    object Splash : Screen()
    object Home : Screen()
    object AddCustomer : Screen()
    data class EditCustomer(val customerId: Long) : Screen()
    data class CustomerDetails(val customerId: Long) : Screen()
    data class EventBookingFlow(val preselectedCustomerId: Long? = null) : Screen()
    data class EventDetails(val eventId: Long) : Screen()
    data class EditEvent(val eventId: Long) : Screen()
    object CrewHome : Screen()
    object AddCrew : Screen()
    data class EditCrew(val crewId: Long) : Screen()
    object Settings : Screen()
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StudioRepository(application)

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _screenStack = mutableListOf<Screen>()

    private val _language = MutableStateFlow(repository.getLanguage())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _studioProfile = MutableStateFlow(repository.getStudioProfile())
    val studioProfile: StateFlow<StudioProfile> = _studioProfile.asStateFlow()

    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery: StateFlow<String> = _customerSearchQuery.asStateFlow()

    private val _crewSearchQuery = MutableStateFlow("")
    val crewSearchQuery: StateFlow<String> = _crewSearchQuery.asStateFlow()

    // Database flows
    val allCustomers = repository.customers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allEvents = repository.events.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allCrew = repository.crewMembers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Filtered Customers based on search
    val filteredCustomers = combine(allCustomers, _customerSearchQuery) { customers, query ->
        if (query.isBlank()) {
            customers
        } else {
            val q = query.trim().lowercase()
            customers.filter {
                it.name.lowercase().contains(q) || it.mobile.contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Crew based on search
    val filteredCrew = combine(allCrew, _crewSearchQuery) { crewList, query ->
        if (query.isBlank()) {
            crewList
        } else {
            val q = query.trim().lowercase()
            crewList.filter {
                it.name.lowercase().contains(q) ||
                        it.mobile.contains(q) ||
                        it.role.lowercase().contains(q) ||
                        it.cameraEquipment.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Toast & status messages
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Dialog & Flow States
    var newlySavedCustomer: Customer? = null
    var newlySavedEvent: EventBooking? = null

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    val strings: StringsDefinition
        get() = AppStrings.get(_language.value)

    fun navigateTo(screen: Screen, clearStack: Boolean = false) {
        if (clearStack) {
            _screenStack.clear()
        } else {
            _screenStack.add(_currentScreen.value)
        }
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (_screenStack.isNotEmpty()) {
            val previous = _screenStack.removeAt(_screenStack.size - 1)
            _currentScreen.value = previous
            return true
        } else if (_currentScreen.value != Screen.Home) {
            _currentScreen.value = Screen.Home
            return true
        }
        return false
    }

    fun onBottomNavSelected(index: Int) {
        when (index) {
            0 -> navigateTo(Screen.Home, clearStack = true)
            1 -> navigateTo(Screen.EventBookingFlow(), clearStack = true)
            2 -> navigateTo(Screen.CrewHome, clearStack = true)
            3 -> navigateTo(Screen.Settings, clearStack = true)
        }
    }

    fun updateCustomerSearch(query: String) {
        _customerSearchQuery.value = query
    }

    fun updateCrewSearch(query: String) {
        _crewSearchQuery.value = query
    }

    // Customer operations
    fun normalizeMobile(mobile: String): String = StudioRepository.normalizePhone(mobile)

    fun isValidMobile(mobile: String): Boolean {
        val clean = normalizeMobile(mobile)
        return clean.length == 10 && clean.all { it.isDigit() }
    }

    fun saveCustomer(
        name: String,
        mobile: String,
        note: String,
        onSuccess: (Customer) -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanName = name.trim()
        val cleanMobile = normalizeMobile(mobile)

        if (cleanName.isBlank()) {
            onError(strings.errCustomerNameRequired)
            return
        }
        if (!isValidMobile(cleanMobile)) {
            onError(strings.errValidMobileRequired)
            return
        }

        viewModelScope.launch {
            try {
                val existing = repository.findCustomerByMobile(cleanMobile)
                if (existing != null) {
                    onError("આ મોબાઈલ નંબર સાથે ગ્રાહક '${existing.name}' પહેલેથી સેવ છે.")
                    return@launch
                }
                val customer = Customer(name = cleanName, mobile = cleanMobile, note = note.trim())
                val id = repository.insertCustomer(customer)
                val saved = customer.copy(id = id)
                newlySavedCustomer = saved
                onSuccess(saved)
            } catch (e: Exception) {
                onError(strings.errSaveFailed)
            }
        }
    }

    fun updateCustomer(
        id: Long,
        name: String,
        mobile: String,
        note: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanName = name.trim()
        val cleanMobile = normalizeMobile(mobile)

        if (cleanName.isBlank()) {
            onError(strings.errCustomerNameRequired)
            return
        }
        if (!isValidMobile(cleanMobile)) {
            onError(strings.errValidMobileRequired)
            return
        }

        viewModelScope.launch {
            try {
                val duplicate = repository.findCustomerByMobile(cleanMobile)
                if (duplicate != null && duplicate.id != id) {
                    onError("આ મોબાઈલ નંબર સાથે અન્ય ગ્રાહક '${duplicate.name}' પહેલેથી સેવ છે.")
                    return@launch
                }
                val existing = repository.getCustomerById(id)
                if (existing != null) {
                    repository.updateCustomer(existing.copy(name = cleanName, mobile = cleanMobile, note = note.trim()))
                    onSuccess()
                } else {
                    onError("Customer not found")
                }
            } catch (e: Exception) {
                onError(strings.errSaveFailed)
            }
        }
    }

    fun deleteCustomer(customer: Customer, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            onDeleted()
        }
    }

    // Event operations
    fun checkEventWarnings(
        customerId: Long,
        eventName: String,
        eventDate: String,
        eventDateMillis: Long,
        eventTime: String,
        onPastWarning: () -> Unit,
        onDuplicateWarning: () -> Unit,
        onClear: () -> Unit
    ) {
        val now = System.currentTimeMillis()
        var effectiveMillis = eventDateMillis
        if (effectiveMillis <= 0L && eventDate.isNotBlank()) {
            try {
                val sdf1 = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                val d = sdf1.parse(eventDate.trim())
                if (d != null) effectiveMillis = d.time
            } catch (e: Exception) {
                try {
                    val sdf2 = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
                    val d2 = sdf2.parse(eventDate.trim())
                    if (d2 != null) effectiveMillis = d2.time
                } catch (e2: Exception) {}
            }
        }
        val isPast = effectiveMillis > 0L && effectiveMillis < (now - 24 * 60 * 60 * 1000)

        viewModelScope.launch {
            val duplicates = repository.findDuplicateEvents(customerId, eventName, eventDate, eventTime)
            if (duplicates.isNotEmpty()) {
                onDuplicateWarning()
            } else if (isPast) {
                onPastWarning()
            } else {
                onClear()
            }
        }
    }

    fun checkEventWarningsExcluding(
        customerId: Long,
        eventName: String,
        eventDate: String,
        eventDateMillis: Long,
        eventTime: String,
        excludeEventId: Long,
        onPastWarning: () -> Unit,
        onDuplicateWarning: () -> Unit,
        onClear: () -> Unit
    ) {
        val now = System.currentTimeMillis()
        var effectiveMillis = eventDateMillis
        if (effectiveMillis <= 0L && eventDate.isNotBlank()) {
            try {
                val sdf1 = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                val d = sdf1.parse(eventDate.trim())
                if (d != null) effectiveMillis = d.time
            } catch (e: Exception) {
                try {
                    val sdf2 = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
                    val d2 = sdf2.parse(eventDate.trim())
                    if (d2 != null) effectiveMillis = d2.time
                } catch (e2: Exception) {}
            }
        }
        val isPast = effectiveMillis > 0L && effectiveMillis < (now - 24 * 60 * 60 * 1000)

        viewModelScope.launch {
            val duplicates = repository.findDuplicateEventsExcluding(customerId, eventName, eventDate, eventTime, excludeEventId)
            if (duplicates.isNotEmpty()) {
                onDuplicateWarning()
            } else if (isPast) {
                onPastWarning()
            } else {
                onClear()
            }
        }
    }

    suspend fun getCrewBookingsOnDate(eventDate: String, excludeEventId: Long? = null): Map<Long, String> {
        val events = repository.getEventsOnDate(eventDate).filter { it.id != excludeEventId }
        val busyMap = mutableMapOf<Long, String>()
        events.forEach { ev ->
            val ids = ev.crewIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
            ids.forEach { crewId ->
                busyMap[crewId] = "${ev.eventName} (${ev.eventTime})"
            }
        }
        return busyMap
    }

    suspend fun checkCrewConflicts(
        eventDate: String,
        selectedCrewMembers: List<CrewMember>,
        excludeEventId: Long? = null
    ): List<String> {
        if (selectedCrewMembers.isEmpty() || eventDate.isBlank()) return emptyList()
        val busyMap = getCrewBookingsOnDate(eventDate, excludeEventId)
        val conflictList = mutableListOf<String>()
        selectedCrewMembers.forEach { crew ->
            if (busyMap.containsKey(crew.id)) {
                conflictList.add("${crew.name} (${busyMap[crew.id]})")
            }
        }
        return conflictList
    }

    fun saveEvent(
        event: EventBooking,
        onSuccess: (EventBooking) -> Unit,
        onError: (String) -> Unit
    ) {
        if (event.customerId <= 0) {
            onError(strings.errCustomerRequired)
            return
        }
        if (event.eventName.isBlank()) {
            onError(strings.errEventNameRequired)
            return
        }
        if (event.eventDate.isBlank()) {
            onError(strings.errEventDateRequired)
            return
        }
        if (event.eventTime.isBlank()) {
            onError(strings.errEventTimeRequired)
            return
        }
        if (event.eventLocation.isBlank()) {
            onError(strings.errEventLocationRequired)
            return
        }
        if (event.crewNames.isBlank()) {
            onError(strings.errAtLeastOneCrewRequired)
            return
        }

        viewModelScope.launch {
            try {
                val id = repository.insertEvent(event)
                val saved = event.copy(id = id)
                newlySavedEvent = saved
                onSuccess(saved)
            } catch (e: Exception) {
                onError(strings.errSaveFailed)
            }
        }
    }

    fun updateEvent(
        event: EventBooking,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateEvent(event)
                onSuccess()
            } catch (e: Exception) {
                onError(strings.errSaveFailed)
            }
        }
    }

    fun toggleEventStatus(event: EventBooking) {
        val newStatus = if (event.status == EventBooking.STATUS_COMPLETED) {
            EventBooking.STATUS_UPCOMING
        } else {
            EventBooking.STATUS_COMPLETED
        }
        viewModelScope.launch {
            repository.updateEvent(event.copy(status = newStatus))
        }
    }

    fun deleteEvent(event: EventBooking, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            onDeleted()
        }
    }

    // Crew operations
    fun checkCrewMobileDuplicate(mobile: String, excludeId: Long? = null, onDuplicate: () -> Unit, onClear: () -> Unit) {
        val cleanMobile = normalizeMobile(mobile)
        viewModelScope.launch {
            val existing = repository.findCrewByMobile(cleanMobile)
            if (existing != null && existing.id != excludeId) {
                onDuplicate()
            } else {
                onClear()
            }
        }
    }

    fun saveCrew(
        name: String,
        mobile: String,
        role: String,
        cameraEquipment: String,
        note: String,
        onSuccess: (CrewMember) -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanName = name.trim()
        val cleanMobile = normalizeMobile(mobile)

        if (cleanName.isBlank()) {
            onError("Crew member name is required.")
            return
        }
        if (!isValidMobile(cleanMobile)) {
            onError(strings.errValidMobileRequired)
            return
        }

        viewModelScope.launch {
            try {
                val crew = CrewMember(
                    name = cleanName,
                    mobile = cleanMobile,
                    role = role,
                    cameraEquipment = cameraEquipment.trim(),
                    note = note.trim()
                )
                val id = repository.insertCrew(crew)
                onSuccess(crew.copy(id = id))
            } catch (e: Exception) {
                onError(strings.errSaveFailed)
            }
        }
    }

    fun updateCrew(
        crew: CrewMember,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanName = crew.name.trim()
        val cleanMobile = normalizeMobile(crew.mobile)

        if (cleanName.isBlank()) {
            onError("Crew member name is required.")
            return
        }
        if (!isValidMobile(cleanMobile)) {
            onError(strings.errValidMobileRequired)
            return
        }

        viewModelScope.launch {
            try {
                val duplicate = repository.findCrewByMobile(cleanMobile)
                if (duplicate != null && duplicate.id != crew.id) {
                    onError("આ મોબાઈલ નંબર સાથે અન્ય ક્રૂ મેમ્બર '${duplicate.name}' પહેલેથી સેવ છે.")
                    return@launch
                }
                repository.updateCrew(crew.copy(name = cleanName, mobile = cleanMobile, note = crew.note.trim(), cameraEquipment = crew.cameraEquipment.trim()))
                onSuccess()
            } catch (e: Exception) {
                onError(strings.errSaveFailed)
            }
        }
    }

    fun deleteCrew(crew: CrewMember, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteCrew(crew)
            onDeleted()
        }
    }

    fun showUserMessage(message: String) {
        viewModelScope.launch {
            _userMessage.emit(message)
        }
    }

    // Settings
    fun saveStudioProfile(profile: StudioProfile) {
        repository.saveStudioProfile(profile)
        _studioProfile.value = profile
        viewModelScope.launch {
            _userMessage.emit(strings.settingsSavedToast)
        }
    }

    fun setLanguage(language: AppLanguage) {
        repository.saveLanguage(language)
        _language.value = language
    }

    fun backupData(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.createBackupJson()
            _studioProfile.value = repository.getStudioProfile()
            onComplete(json)
        }
    }

    fun restoreData(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.restoreBackup()
            if (success) {
                _studioProfile.value = repository.getStudioProfile()
            }
            onResult(success)
        }
    }

    // Intent actions for Call, WhatsApp, Navigation
    fun openDialer(mobile: String) {
        try {
            val digits = mobile.filter { it.isDigit() }
            val clean = when {
                digits.length == 10 -> digits
                digits.length == 12 && digits.startsWith("91") -> digits.substring(2)
                digits.length == 11 && digits.startsWith("0") -> digits.substring(1)
                else -> digits
            }
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            showUserMessage("Unable to open dialer")
        }
    }

    fun openWhatsApp(mobile: String, prefillText: String = "") {
        try {
            val digits = mobile.filter { it.isDigit() }
            val clean = when {
                digits.length == 10 -> "91$digits"
                digits.length == 11 && digits.startsWith("0") -> "91" + digits.substring(1)
                digits.length == 12 && digits.startsWith("91") -> digits
                else -> digits
            }
            val uri = if (prefillText.isNotBlank()) {
                Uri.parse("https://wa.me/$clean?text=${Uri.encode(prefillText)}")
            } else {
                Uri.parse("https://wa.me/$clean")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            viewModelScope.launch {
                _userMessage.emit("વોટ્સએપ ખોલી શકાયું નથી. કૃપા કરીને મોબાઈલ નંબર ચકાસો.")
            }
        }
    }

    fun openNavigation(location: String) {
        try {
            val uri = Uri.parse("geo:0,0?q=" + Uri.encode(location))
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareText(text: String, title: String = "Share Event Call Sheet") {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val shareIntent = Intent.createChooser(sendIntent, title).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(shareIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCallSheetForEvent(event: EventBooking): String {
        val custEvents = allEvents.value.filter { it.customerId == event.customerId }
        return com.example.util.CallSheetHelper.generateEventCallSheet(
            profile = _studioProfile.value,
            mainEvent = event,
            allCustomerEvents = custEvents,
            allCrew = allCrew.value
        )
    }

    fun getCallSheetForCustomer(customerId: Long): String {
        val custEvents = allEvents.value.filter { it.customerId == customerId }
        val mainEvent = custEvents.firstOrNull() ?: return ""
        return com.example.util.CallSheetHelper.generateEventCallSheet(
            profile = _studioProfile.value,
            mainEvent = mainEvent,
            allCustomerEvents = custEvents,
            allCrew = allCrew.value
        )
    }
}

package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CrewMember
import com.example.data.model.Customer
import com.example.data.model.EventBooking
import com.example.ui.strings.StringsDefinition
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioDarkText
import com.example.ui.theme.StudioGreenDark
import com.example.ui.theme.StudioGreenLight
import com.example.ui.theme.StudioGreenSuccess
import com.example.ui.theme.StudioLightBorder
import com.example.ui.theme.StudioRedDark
import com.example.ui.theme.StudioRedLight
import com.example.ui.theme.StudioRedPrimary
import com.example.ui.theme.StudioSecondaryGray
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.StudioViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EventBookingScreen(
    preselectedCustomerId: Long? = null,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = viewModel.strings
    val customers by viewModel.allCustomers.collectAsState()
    val allCrew by viewModel.allCrew.collectAsState()

    // Customer selection state
    var selectedCustomerId by remember(preselectedCustomerId) {
        mutableStateOf(preselectedCustomerId ?: customers.firstOrNull()?.id)
    }

    // Event fields state
    var eventName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var eventDateMillis by remember { mutableLongStateOf(0L) }
    var eventTime by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }
    val selectedCrewMembers = remember { mutableStateListOf<CrewMember>() }
    var dressCodeNote by remember { mutableStateOf("") }

    // Reminder state
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderPreset by remember { mutableStateOf("1_day") } // "1_day", "12_hours", "6_hours", "3_hours", "1_hour", "custom"

    // Dialog / Modal states
    var showCrewPickerModal by remember { mutableStateOf(false) }
    var showPreviewModal by remember { mutableStateOf(false) }
    var showSuccessModal by remember { mutableStateOf(false) }
    var showCallSheetModal by remember { mutableStateOf(false) }
    var recentlySavedEvent by remember { mutableStateOf<EventBooking?>(null) }
    var showPastWarningDialog by remember { mutableStateOf(false) }
    var showDuplicateWarningDialog by remember { mutableStateOf(false) }
    var showCrewConflictDialog by remember { mutableStateOf(false) }
    var crewConflictMessage by remember { mutableStateOf("") }
    var busyCrewMap by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }

    LaunchedEffect(eventDate) {
        if (eventDate.isNotBlank()) {
            busyCrewMap = viewModel.getCrewBookingsOnDate(eventDate)
        } else {
            busyCrewMap = emptyMap()
        }
    }

    // Validation Errors
    var customerError by remember { mutableStateOf<String?>(null) }
    var eventNameError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var crewError by remember { mutableStateOf<String?>(null) }

    val selectedCustomer = customers.find { it.id == selectedCustomerId }

    BackHandler {
        viewModel.navigateBack()
    }

    // Date Picker Setup
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            eventDateMillis = cal.timeInMillis
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            eventDate = sdf.format(cal.time)
            dateError = null
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Time Picker Setup (12-hour AM/PM format)
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val amPm = if (hourOfDay < 12) "AM" else "PM"
            val hour12 = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
            eventTime = String.format(Locale.ENGLISH, "%02d:%02d %s", hour12, minute, amPm)
            timeError = null
        },
        10, 0, false
    )

    fun clearEventFieldsOnly() {
        eventName = ""
        eventDate = ""
        eventDateMillis = 0L
        eventTime = ""
        eventLocation = ""
        selectedCrewMembers.clear()
        dressCodeNote = ""
        reminderEnabled = false
        reminderPreset = "1_day"
        eventNameError = null
        dateError = null
        timeError = null
        locationError = null
        crewError = null
    }

    fun executeEventSave() {
        val cust = selectedCustomer ?: return
        val crewIds = selectedCrewMembers.map { it.id }.joinToString(",")
        val crewNamesStr = selectedCrewMembers.map { "${it.name} (${it.role})" }.joinToString(", ")

        val eventToSave = EventBooking(
            customerId = cust.id,
            customerName = cust.name,
            customerMobile = cust.mobile,
            eventName = eventName.trim(),
            eventDate = eventDate.trim(),
            eventDateMillis = if (eventDateMillis > 0) eventDateMillis else System.currentTimeMillis(),
            eventTime = eventTime.trim(),
            eventLocation = eventLocation.trim(),
            crewIdsCsv = crewIds,
            crewNames = crewNamesStr,
            dressCodeNote = dressCodeNote.trim(),
            reminderEnabled = reminderEnabled,
            reminderPreset = if (reminderEnabled) reminderPreset else "none",
            status = EventBooking.STATUS_UPCOMING
        )

        viewModel.saveEvent(
            event = eventToSave,
            onSuccess = { saved ->
                recentlySavedEvent = saved
                showPreviewModal = false
                showSuccessModal = true
            },
            onError = { err ->
                locationError = err
            }
        )
    }

    fun validateAndProceed() {
        var hasError = false
        if (selectedCustomer == null) {
            customerError = strings.errCustomerRequired
            hasError = true
        }
        if (eventName.trim().isBlank()) {
            eventNameError = strings.errEventNameRequired
            hasError = true
        }
        if (eventDate.trim().isBlank()) {
            dateError = strings.errEventDateRequired
            hasError = true
        }
        if (eventTime.trim().isBlank()) {
            timeError = strings.errEventTimeRequired
            hasError = true
        }
        if (eventLocation.trim().isBlank()) {
            locationError = strings.errEventLocationRequired
            hasError = true
        }
        if (selectedCrewMembers.isEmpty()) {
            crewError = strings.errAtLeastOneCrewRequired
            hasError = true
        }

        if (hasError) return

        fun proceedWithCrewCheck() {
            val conflictingCrew = selectedCrewMembers.filter { busyCrewMap.containsKey(it.id) }
            if (conflictingCrew.isNotEmpty()) {
                val conflictSummary = conflictingCrew.joinToString("\n• ") { "${it.name}: ${busyCrewMap[it.id]}" }
                crewConflictMessage = "નીચે મુજબના ક્રૂ આ તારીખે બીજી ઇવેન્ટમાં બુક છે:\n• $conflictSummary\n\nશું તમે આ ક્રૂ સાથે બુકિંગ ચાલુ રાખવા માંગો છો?"
                showCrewConflictDialog = true
            } else {
                showPreviewModal = true
            }
        }

        // Check for Warnings: Past Date or Duplicate Event
        viewModel.checkEventWarnings(
            customerId = selectedCustomer!!.id,
            eventName = eventName,
            eventDate = eventDate,
            eventDateMillis = eventDateMillis,
            eventTime = eventTime,
            onPastWarning = { showPastWarningDialog = true },
            onDuplicateWarning = { showDuplicateWarningDialog = true },
            onClear = { proceedWithCrewCheck() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.eventBookingTitle,
                        fontWeight = FontWeight.Bold,
                        color = StudioDarkText
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("event_booking_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    StudioAppSmallTag(modifier = Modifier.padding(end = 12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = StudioBackground,
        modifier = modifier.testTag("event_booking_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer Selector Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                CustomerSelectorCard(
                    selectedCustomer = selectedCustomer,
                    allCustomers = customers,
                    strings = strings,
                    onCustomerSelected = {
                        selectedCustomerId = it.id
                        customerError = null
                    },
                    onAddNewCustomer = {
                        viewModel.navigateTo(Screen.AddCustomer)
                    },
                    error = customerError
                )
            }

            // Event Name with Quick Suggestion Chips
            item {
                Text(
                    text = strings.eventNameLabel,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = StudioDarkText
                )
                Spacer(modifier = Modifier.height(6.dp))
                StudioInputField(
                    value = eventName,
                    onValueChange = {
                        if (it.length <= 100) {
                            eventName = it
                            if (eventNameError != null) eventNameError = null
                        }
                    },
                    placeholder = strings.eventNameHint,
                    isError = eventNameError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "event_name_input"
                )
                if (eventNameError != null) {
                    Text(eventNameError!!, color = StudioRedPrimary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }

                // Quick Event Name Suggestions
                Spacer(modifier = Modifier.height(8.dp))
                val quickEventNames = listOf("Wedding (લગ્ન)", "Haldi (હલ્દી)", "Reception (રીસેપ્શન)", "Birthday (જન્મદિવસ)", "Sangeet (સંગીત)", "Ring Ceremony (સગાઈ)", "Pre-Wedding")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickEventNames.forEach { chipName ->
                        val cleanName = chipName.split(" ").first()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (eventName.contains(cleanName)) StudioRedLight else StudioSurfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (eventName.contains(cleanName)) StudioRedPrimary else StudioLightBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    eventName = cleanName
                                    eventNameError = null
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chipName,
                                fontSize = 12.sp,
                                color = if (eventName.contains(cleanName)) StudioRedDark else StudioDarkText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Event Date & Time Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date Picker Trigger
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.eventDateLabel,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = StudioDarkText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        StudioClickableBox(
                            onClick = { datePickerDialog.show() }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (eventDate.isNotBlank()) eventDate else "તારીખ પસંદ કરો",
                                    fontSize = 14.sp,
                                    color = if (eventDate.isNotBlank()) StudioDarkText else StudioSecondaryGray,
                                    fontWeight = if (eventDate.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Pick Date",
                                    tint = StudioRedPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (dateError != null) {
                            Text(dateError!!, color = StudioRedPrimary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }

                    // Time Picker Trigger
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.eventTimeLabel,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = StudioDarkText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        StudioClickableBox(
                            onClick = { timePickerDialog.show() }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (eventTime.isNotBlank()) eventTime else "સમય પસંદ કરો",
                                    fontSize = 14.sp,
                                    color = if (eventTime.isNotBlank()) StudioDarkText else StudioSecondaryGray,
                                    fontWeight = if (eventTime.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Pick Time",
                                    tint = StudioRedPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (timeError != null) {
                            Text(timeError!!, color = StudioRedPrimary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }

            // Event Location with Suggestions
            item {
                Text(
                    text = strings.eventLocationLabel,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = StudioDarkText
                )
                Spacer(modifier = Modifier.height(6.dp))
                StudioInputField(
                    value = eventLocation,
                    onValueChange = {
                        eventLocation = it
                        if (locationError != null) locationError = null
                    },
                    placeholder = strings.eventLocationHint,
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = StudioRedPrimary)
                    },
                    isError = locationError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "event_location_input"
                )
                if (locationError != null) {
                    Text(locationError!!, color = StudioRedPrimary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }

                // Quick Location Suggestions
                Spacer(modifier = Modifier.height(8.dp))
                val locationChips = listOf("India", "Royal Party Plot", "Heritage Resort", "Grand Banquet")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    locationChips.forEach { loc ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioSurfaceVariant)
                                .border(1.dp, StudioLightBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    eventLocation = loc
                                    locationError = null
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(loc, fontSize = 12.sp, color = StudioDarkText)
                        }
                    }
                }
            }

            // Crew Section (Multi-select)
            item {
                Text(
                    text = strings.crewLabel + " *",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = StudioDarkText
                )
                Spacer(modifier = Modifier.height(6.dp))

                StudioClickableBox(
                    onClick = { showCrewPickerModal = true }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            if (selectedCrewMembers.isEmpty()) {
                                Text(
                                    text = "👥 " + strings.selectCrewBtn,
                                    fontSize = 14.sp,
                                    color = StudioSecondaryGray
                                )
                            } else {
                                Text(
                                    text = "👥 ${selectedCrewMembers.size} ${strings.selectedCrewCount}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioRedDark
                                )
                                Text(
                                    text = selectedCrewMembers.joinToString(", ") { "${it.name} (${it.role})" },
                                    fontSize = 12.sp,
                                    color = StudioDarkText,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Select Crew",
                            tint = StudioSecondaryGray
                        )
                    }
                }
                if (crewError != null) {
                    Text(crewError!!, color = StudioRedPrimary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }

            // Dress Code / Special Note
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.dressCodeNoteLabel,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = StudioDarkText
                    )
                    Text(
                        text = "${dressCodeNote.length}/1000",
                        fontSize = 12.sp,
                        color = StudioSecondaryGray
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                StudioInputField(
                    value = dressCodeNote,
                    onValueChange = {
                        if (it.length <= 1000) dressCodeNote = it
                    },
                    placeholder = strings.dressCodePlaceholder,
                    singleLine = false,
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "dress_code_note_input"
                )

                // Quick Dress Code suggestions
                Spacer(modifier = Modifier.height(8.dp))
                val dressSuggestions = listOf("Black Dress", "Traditional Dress", "Bride & Groom Entry", "Special Camera Setup", "LED Wall Photo Stage")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dressSuggestions.forEach { suggestion ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioSurfaceVariant)
                                .border(1.dp, StudioLightBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    dressCodeNote = if (dressCodeNote.isBlank()) suggestion else "$dressCodeNote, $suggestion"
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(suggestion, fontSize = 11.sp, color = StudioDarkText)
                        }
                    }
                }
            }

            // Event Reminder Section (SCREEN 08)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = StudioRedPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.eventReminderHeader,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioDarkText
                                )
                            }
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { reminderEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = StudioRedPrimary
                                ),
                                modifier = Modifier.testTag("reminder_switch")
                            )
                        }

                        if (reminderEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "રીમાઇન્ડર ક્યારે મેળવવું:",
                                fontSize = 12.sp,
                                color = StudioSecondaryGray,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val presets = listOf(
                                "1_day" to strings.reminder1Day,
                                "12_hours" to strings.reminder12Hours,
                                "6_hours" to strings.reminder6Hours,
                                "3_hours" to strings.reminder3Hours,
                                "1_hour" to strings.reminder1Hour,
                                "custom" to strings.reminderCustom
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                presets.forEach { (key, label) ->
                                    val isSelected = reminderPreset == key
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) StudioRedPrimary else Color.White)
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) StudioRedPrimary else StudioLightBorder,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { reminderPreset = key }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else StudioDarkText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Action: Save Event Trigger Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { validateAndProceed() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_event_trigger_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioGreenSuccess,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.saveEventBtn,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Modal 1: Crew Selector Modal (SCREEN 07)
    if (showCrewPickerModal) {
        CrewPickerModal(
            allCrew = allCrew,
            initialSelected = selectedCrewMembers,
            busyCrewMap = busyCrewMap,
            strings = strings,
            onDone = { selected ->
                selectedCrewMembers.clear()
                selectedCrewMembers.addAll(selected)
                if (selectedCrewMembers.isNotEmpty()) {
                    crewError = null
                }
                showCrewPickerModal = false
            },
            onDismiss = { showCrewPickerModal = false },
            onAddNewCrew = {
                showCrewPickerModal = false
                viewModel.navigateTo(Screen.AddCrew)
            }
        )
    }

    // Modal 2: Event Summary Preview Before Final Save (SCREEN 09)
    if (showPreviewModal && selectedCustomer != null) {
        EventPreviewModal(
            customer = selectedCustomer,
            eventName = eventName,
            eventDate = eventDate,
            eventTime = eventTime,
            eventLocation = eventLocation,
            crewNames = selectedCrewMembers.joinToString(", ") { "${it.name} (${it.role})" },
            dressCode = dressCodeNote,
            reminderInfo = if (reminderEnabled) reminderPreset.replace("_", " ") else "OFF",
            strings = strings,
            onConfirmSave = { executeEventSave() },
            onCancel = { showPreviewModal = false }
        )
    }

    // Modal 3: Save Event Success Modal (SCREEN 12 & 13 Multiple Event Flow)
    if (showSuccessModal && recentlySavedEvent != null) {
        SaveEventSuccessModal(
            event = recentlySavedEvent!!,
            strings = strings,
            onSendWhatsAppCallSheet = {
                showCallSheetModal = true
            },
            onAddAnotherEvent = {
                // IMPORTANT: Keep the same customer automatically selected!
                // Clear ONLY event-specific fields!
                clearEventFieldsOnly()
                showSuccessModal = false
            },
            onDone = {
                showSuccessModal = false
                viewModel.navigateTo(Screen.Home, clearStack = true)
            }
        )
    }

    // Call Sheet Dialog from Booking Screen
    if (showCallSheetModal && recentlySavedEvent != null) {
        val callSheetText = viewModel.getCallSheetForEvent(recentlySavedEvent!!)
        val assignedCrewIds = recentlySavedEvent!!.crewIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
        val assignedCrewMembers = allCrew.filter { it.id in assignedCrewIds }

        EventCallSheetModal(
            callSheetText = callSheetText,
            customerMobile = recentlySavedEvent!!.customerMobile,
            customerName = recentlySavedEvent!!.customerName,
            assignedCrew = assignedCrewMembers,
            onSendWhatsApp = { mobile, text ->
                viewModel.openWhatsApp(mobile, text)
            },
            onShareGeneral = { text ->
                viewModel.shareText(text)
            },
            onDismiss = { showCallSheetModal = false }
        )
    }

    // Warning Dialog 1: Past Event Warning (Section 23)
    if (showPastWarningDialog) {
        WarningAlertDialog(
            title = "Past Event Warning",
            message = strings.pastEventWarningMsg,
            continueText = strings.continueBtn,
            cancelText = strings.cancelBtn,
            onContinue = {
                showPastWarningDialog = false
                showPreviewModal = true
            },
            onCancel = { showPastWarningDialog = false }
        )
    }

    // Warning Dialog 2: Duplicate Event Warning (Section 24)
    if (showDuplicateWarningDialog) {
        WarningAlertDialog(
            title = strings.duplicateEventTitle,
            message = strings.duplicateEventMsg,
            continueText = strings.continueBtn,
            cancelText = strings.cancelBtn,
            onContinue = {
                showDuplicateWarningDialog = false
                showPreviewModal = true
            },
            onCancel = { showDuplicateWarningDialog = false }
        )
    }
}

@Composable
fun CustomerSelectorCard(
    selectedCustomer: Customer?,
    allCustomers: List<Customer>,
    strings: StringsDefinition,
    onCustomerSelected: (Customer) -> Unit,
    onAddNewCustomer: () -> Unit,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (error != null) StudioRedPrimary else StudioLightBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = strings.customerSectionHeader,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = StudioDarkText
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Selector Box
            StudioClickableBox(
                onClick = { expanded = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (selectedCustomer != null) {
                        Column {
                            Text(
                                text = selectedCustomer.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = StudioDarkText
                            )
                            Text(
                                text = "📱 ${selectedCustomer.mobile}",
                                fontSize = 12.sp,
                                color = StudioSecondaryGray
                            )
                        }
                    } else {
                        Text(
                            text = strings.selectCustomerHint,
                            fontSize = 14.sp,
                            color = StudioSecondaryGray
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Select Customer",
                        tint = StudioSecondaryGray
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color.White)
                ) {
                    allCustomers.forEach { customer ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(customer.name, fontWeight = FontWeight.SemiBold, color = StudioDarkText)
                                    Text("📱 ${customer.mobile}", fontSize = 12.sp, color = StudioSecondaryGray)
                                }
                            },
                            onClick = {
                                onCustomerSelected(customer)
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (error != null) {
                Text(error, color = StudioRedPrimary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // + Add New Customer Shortcut
            TextButton(
                onClick = onAddNewCustomer,
                modifier = Modifier
                    .align(Alignment.End)
                    .testTag("event_booking_add_new_cust_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = StudioRedPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = strings.addNewCustomerBtn,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioRedPrimary
                )
            }
        }
    }
}

// SCREEN 07 - CREW SELECTOR MODAL
@Composable
fun CrewPickerModal(
    allCrew: List<CrewMember>,
    initialSelected: List<CrewMember>,
    busyCrewMap: Map<Long, String> = emptyMap(),
    strings: StringsDefinition,
    onDone: (List<CrewMember>) -> Unit,
    onDismiss: () -> Unit,
    onAddNewCrew: () -> Unit
) {
    val selected = remember { mutableStateListOf<CrewMember>().apply { addAll(initialSelected) } }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCrew = allCrew.filter {
        searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.role.contains(searchQuery, ignoreCase = true) ||
                it.mobile.contains(searchQuery)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.selectCrewModalTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = StudioDarkText
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Search Crew inside Picker
                StudioInputField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = strings.searchCrewHint,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredCrew.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(strings.noCrewYet, color = StudioSecondaryGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onAddNewCrew,
                                colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary)
                            ) {
                                Text(strings.addCrewBtn)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredCrew, key = { it.id }) { crew ->
                            val isChecked = selected.any { it.id == crew.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChecked) StudioRedLight else StudioSurfaceVariant)
                                    .clickable {
                                        if (isChecked) {
                                            selected.removeAll { it.id == crew.id }
                                        } else {
                                            selected.add(crew)
                                        }
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            if (!selected.any { it.id == crew.id }) selected.add(crew)
                                        } else {
                                            selected.removeAll { it.id == crew.id }
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = StudioRedPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = crew.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = StudioDarkText
                                    )
                                    Text(
                                        text = "${crew.role} • ${crew.mobile}",
                                        fontSize = 12.sp,
                                        color = StudioSecondaryGray
                                    )
                                    if (busyCrewMap.containsKey(crew.id)) {
                                        Text(
                                            text = "⚠️ આ તારીખે બુક છે: ${busyCrewMap[crew.id]}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = StudioRedDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDone(selected) },
                enabled = selected.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("crew_picker_done_btn")
            ) {
                Text(strings.doneBtn, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onAddNewCrew,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(strings.addCrewBtn, color = StudioRedPrimary)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

// SCREEN 09 – EVENT PREVIEW / SUMMARY MODAL
@Composable
fun EventPreviewModal(
    customer: Customer,
    eventName: String,
    eventDate: String,
    eventTime: String,
    eventLocation: String,
    crewNames: String,
    dressCode: String,
    reminderInfo: String,
    strings: StringsDefinition,
    onConfirmSave: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = strings.eventSummaryTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = StudioDarkText
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PreviewRowItem("Customer:", customer.name)
                PreviewRowItem("Event:", eventName)
                PreviewRowItem("Date:", eventDate)
                PreviewRowItem("Time:", eventTime)
                PreviewRowItem("Location:", eventLocation)
                PreviewRowItem("Crew:", crewNames)
                if (dressCode.isNotBlank()) {
                    PreviewRowItem("Dress Code:", dressCode)
                }
                PreviewRowItem("Reminder:", reminderInfo)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmSave,
                colors = ButtonDefaults.buttonColors(containerColor = StudioGreenSuccess),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("preview_confirm_save_btn")
            ) {
                Text(strings.saveEventBtn, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(strings.cancelBtn, color = StudioSecondaryGray)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
fun PreviewRowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = StudioSecondaryGray,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = StudioDarkText,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

// SCREEN 12 & 13 – SAVE EVENT SUCCESS SCREEN / MODAL
@Composable
fun SaveEventSuccessModal(
    event: EventBooking,
    strings: StringsDefinition,
    onSendWhatsAppCallSheet: () -> Unit,
    onAddAnotherEvent: () -> Unit,
    onDone: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDone,
        icon = {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(StudioGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = StudioGreenSuccess,
                    modifier = Modifier.size(38.dp)
                )
            }
        },
        title = {
            Text(
                text = strings.eventSavedSuccessTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = StudioDarkText
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = event.customerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = StudioDarkText
                )
                Text(
                    text = "🎉 ${event.eventName}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StudioRedDark
                )
                Text(
                    text = "📅 ${event.eventDate} • ⏰ ${event.eventTime}",
                    fontSize = 13.sp,
                    color = StudioSecondaryGray
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 💬 Send WhatsApp Call Sheet
                Button(
                    onClick = onSendWhatsAppCallSheet,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioGreenSuccess),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("send_call_sheet_after_save_btn")
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WhatsApp Call Sheet મોકલો",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // ➕ Add Another Event (Keeps the same customer automatically selected!)
                Button(
                    onClick = onAddAnotherEvent,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("add_another_event_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "➕ ${strings.addAnotherEventBtn}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // ✔ Done
                OutlinedButton(
                    onClick = onDone,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("event_save_done_btn")
                ) {
                    Text(
                        text = "✔ ${strings.doneBtn}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = StudioDarkText
                    )
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

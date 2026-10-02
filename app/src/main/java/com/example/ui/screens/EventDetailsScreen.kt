package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CrewMember
import com.example.data.model.EventBooking
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: Long,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val strings = viewModel.strings
    val allEvents by viewModel.allEvents.collectAsState()
    val allCrew by viewModel.allCrew.collectAsState()
    val event = allEvents.find { it.id == eventId }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showCallCrewDialog by remember { mutableStateOf(false) }
    var showCallSheetModal by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.eventDetailsTitle,
                        fontWeight = FontWeight.Bold,
                        color = StudioDarkText
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("event_details_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    StudioAppSmallTag(modifier = Modifier.padding(end = 4.dp))
                    event?.let {
                        IconButton(
                            onClick = { showCallSheetModal = true },
                            modifier = Modifier.testTag("event_details_whatsapp_icon")
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "WhatsApp Call Sheet", tint = StudioGreenDark)
                        }
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.EditEvent(it.id)) },
                            modifier = Modifier.testTag("event_details_edit_icon")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = StudioDarkText)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = StudioBackground,
        modifier = modifier.testTag("event_details_screen")
    ) { padding ->
        if (event == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Event not found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    // Event Hero Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎉 ${event.eventName}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioRedDark
                                )

                                // Simple Mark Done button
                                val isDone = event.status == EventBooking.STATUS_COMPLETED
                                OutlinedButton(
                                    onClick = { viewModel.toggleEventStatus(event) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (isDone) StudioGreenDark else StudioRedPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("mark_done_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isDone) strings.statusCompleted else strings.markDoneBtn,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Customer row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .clickable { viewModel.navigateTo(Screen.CustomerDetails(event.customerId)) }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(StudioRedLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = event.customerName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = StudioRedDark
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = event.customerName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = StudioDarkText
                                    )
                                    Text(
                                        text = "📱 ${event.customerMobile}",
                                        fontSize = 13.sp,
                                        color = StudioSecondaryGray
                                    )
                                }
                                IconButton(onClick = { viewModel.openDialer(event.customerMobile) }) {
                                    Icon(Icons.Default.Call, contentDescription = "Call Customer", tint = StudioGreenDark)
                                }
                            }
                        }
                    }
                }

                // Event Details Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            EventDetailInfoRow("📅 Date", event.eventDate)
                            EventDetailInfoRow("⏰ Time", event.eventTime)
                            EventDetailInfoRow("📍 Location", event.eventLocation)
                            EventDetailInfoRow("👥 Crew", event.crewNames.ifBlank { "Not assigned" })

                            if (event.dressCodeNote.isNotBlank()) {
                                EventDetailInfoRow("👕 Dress Code / Note", event.dressCodeNote)
                            }

                            EventDetailInfoRow(
                                "🔔 Reminder",
                                if (event.reminderEnabled) "ON (${event.reminderPreset.replace("_", " ")})" else "OFF"
                            )
                        }
                    }
                }

                // WhatsApp Call Sheet Action Button (STUDIO MANAGER - EVENT CALL SHEET)
                item {
                    Button(
                        onClick = { showCallSheetModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("event_whatsapp_callsheet_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioGreenSuccess)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WhatsApp Event Call Sheet (કોલ શીટ)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Action Buttons: Navigate & Call Crew
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Navigate Button
                        Button(
                            onClick = { viewModel.openNavigation(event.eventLocation) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("event_navigate_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary)
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.navigateBtn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Call Crew Button
                        Button(
                            onClick = { showCallCrewDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("event_call_crew_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StudioGreenSuccess)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.callCrewBtn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                // Edit & Delete Action Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.EditEvent(event.id)) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("event_details_edit_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.editEventBtn, color = StudioDarkText, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("event_details_delete_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioRedPrimary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(StudioRedPrimary))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = StudioRedPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.deleteEventBtn, color = StudioRedPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    // Call Crew Dialog (List of assigned crew members to call)
    if (showCallCrewDialog && event != null) {
        val assignedCrewIds = event.crewIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
        val assignedCrewMembers = allCrew.filter { it.id in assignedCrewIds }

        AlertDialog(
            onDismissRequest = { showCallCrewDialog = false },
            title = {
                Text(
                    text = strings.callCrewBtn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = StudioDarkText
                )
            },
            text = {
                if (assignedCrewMembers.isEmpty()) {
                    Text("No crew members assigned or available.", color = StudioSecondaryGray)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        assignedCrewMembers.forEach { crew ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StudioSurfaceVariant)
                                    .clickable {
                                        showCallCrewDialog = false
                                        viewModel.openDialer(crew.mobile)
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(crew.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StudioDarkText)
                                    Text("${crew.role} • ${crew.mobile}", fontSize = 12.sp, color = StudioSecondaryGray)
                                }
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = StudioGreenDark)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCallCrewDialog = false }) {
                    Text(strings.cancelBtn, color = StudioDarkText)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // Delete Event Confirmation Dialog (SCREEN 12)
    if (showDeleteConfirm && event != null) {
        SimpleConfirmationDialog(
            title = strings.deleteConfirmationTitle,
            message = "${strings.deleteEventConfirmMsg} (${event.eventName})",
            confirmText = strings.actionDelete,
            cancelText = strings.cancelBtn,
            onConfirm = {
                viewModel.deleteEvent(event) {
                    viewModel.navigateBack()
                }
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false },
            isDestructive = true
        )
    }

    // Interactive WhatsApp Call Sheet Dialog
    if (showCallSheetModal && event != null) {
        val callSheetText = viewModel.getCallSheetForEvent(event)
        val assignedCrewIds = event.crewIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
        val assignedCrewMembers = allCrew.filter { it.id in assignedCrewIds }

        EventCallSheetModal(
            callSheetText = callSheetText,
            customerMobile = event.customerMobile,
            customerName = event.customerName,
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
}

@Composable
fun EventDetailInfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = StudioSecondaryGray
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = StudioDarkText
        )
    }
}

// SCREEN 11 – EDIT EVENT SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    eventId: Long,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = viewModel.strings
    val allEvents by viewModel.allEvents.collectAsState()
    val allCrew by viewModel.allCrew.collectAsState()
    val event = allEvents.find { it.id == eventId }

    var eventName by remember(event) { mutableStateOf(event?.eventName ?: "") }
    var eventDate by remember(event) { mutableStateOf(event?.eventDate ?: "") }
    var eventDateMillis by remember(event) { mutableLongStateOf(event?.eventDateMillis ?: 0L) }
    var eventTime by remember(event) { mutableStateOf(event?.eventTime ?: "") }
    var eventLocation by remember(event) { mutableStateOf(event?.eventLocation ?: "") }
    var dressCodeNote by remember(event) { mutableStateOf(event?.dressCodeNote ?: "") }
    var reminderEnabled by remember(event) { mutableStateOf(event?.reminderEnabled ?: false) }
    var reminderPreset by remember(event) { mutableStateOf(event?.reminderPreset ?: "1_day") }

    val selectedCrewMembers = remember(event, allCrew) {
        val list = mutableStateListOf<CrewMember>()
        if (event != null) {
            val assignedIds = event.crewIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
            list.addAll(allCrew.filter { it.id in assignedIds })
        }
        list
    }

    var showCrewPickerModal by remember { mutableStateOf(false) }
    var busyCrewMap by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
    var showDuplicateWarningDialog by remember { mutableStateOf(false) }
    var showPastWarningDialog by remember { mutableStateOf(false) }

    LaunchedEffect(eventDate) {
        if (eventDate.isNotBlank()) {
            busyCrewMap = viewModel.getCrewBookingsOnDate(eventDate, eventId)
        } else {
            busyCrewMap = emptyMap()
        }
    }

    var eventNameError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var crewError by remember { mutableStateOf<String?>(null) }

    BackHandler {
        viewModel.navigateBack()
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.editEventTitle,
                        fontWeight = FontWeight.Bold,
                        color = StudioDarkText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = StudioBackground,
        modifier = modifier.testTag("edit_event_screen")
    ) { padding ->
        if (event == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Event not found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer Info Banner
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Customer: ${event.customerName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Mobile: ${event.customerMobile}", fontSize = 12.sp, color = StudioSecondaryGray)
                        }
                    }
                }

                // Event Name
                item {
                    Text(text = strings.eventNameLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    StudioInputField(
                        value = eventName,
                        onValueChange = {
                            eventName = it
                            if (eventNameError != null) eventNameError = null
                        },
                        isError = eventNameError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "edit_event_name_input"
                    )
                    if (eventNameError != null) {
                        Text(eventNameError!!, color = StudioRedPrimary, fontSize = 12.sp)
                    }
                }

                // Date & Time
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Date Box
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = strings.eventDateLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            StudioClickableBox(
                                onClick = { datePickerDialog.show() }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(eventDate, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = StudioDarkText)
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = StudioRedPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Time Box
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = strings.eventTimeLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            StudioClickableBox(
                                onClick = { timePickerDialog.show() }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(eventTime, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = StudioDarkText)
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = StudioRedPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Location
                item {
                    Text(text = strings.eventLocationLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    StudioInputField(
                        value = eventLocation,
                        onValueChange = {
                            eventLocation = it
                            if (locationError != null) locationError = null
                        },
                        isError = locationError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "edit_event_location_input"
                    )
                }

                // Crew
                item {
                    Text(text = strings.crewLabel + " *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    StudioClickableBox(
                        onClick = { showCrewPickerModal = true }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (selectedCrewMembers.isEmpty()) strings.selectCrewBtn else selectedCrewMembers.joinToString(", ") { it.name },
                                fontSize = 14.sp,
                                color = if (selectedCrewMembers.isEmpty()) StudioSecondaryGray else StudioDarkText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = StudioSecondaryGray
                            )
                        }
                    }
                    if (crewError != null) {
                        Text(crewError!!, color = StudioRedPrimary, fontSize = 12.sp)
                    }
                }

                // Dress Code / Note
                item {
                    Text(text = strings.dressCodeNoteLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    StudioInputField(
                        value = dressCodeNote,
                        onValueChange = { if (it.length <= 1000) dressCodeNote = it },
                        singleLine = false,
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "edit_dress_code_input"
                    )
                }

                // Reminder
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔔 " + strings.eventReminderHeader, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Switch(
                                    checked = reminderEnabled,
                                    onCheckedChange = { reminderEnabled = it },
                                    colors = SwitchDefaults.colors(checkedTrackColor = StudioRedPrimary)
                                )
                            }
                            if (reminderEnabled) {
                                Spacer(modifier = Modifier.height(10.dp))
                                val presets = listOf(
                                    "1_day" to strings.reminder1Day,
                                    "12_hours" to strings.reminder12Hours,
                                    "6_hours" to strings.reminder6Hours,
                                    "3_hours" to strings.reminder3Hours,
                                    "1_hour" to strings.reminder1Hour,
                                    "custom" to strings.reminderCustom
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                // Save Changes Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            var hasError = false
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

                            if (!hasError) {
                                fun executeUpdate() {
                                    val crewIds = selectedCrewMembers.map { it.id }.joinToString(",")
                                    val crewNamesStr = selectedCrewMembers.map { "${it.name} (${it.role})" }.joinToString(", ")
                                    val updated = event.copy(
                                        eventName = eventName.trim(),
                                        eventDate = eventDate.trim(),
                                        eventDateMillis = if (eventDateMillis > 0) eventDateMillis else event.eventDateMillis,
                                        eventTime = eventTime.trim(),
                                        eventLocation = eventLocation.trim(),
                                        crewIdsCsv = crewIds,
                                        crewNames = crewNamesStr,
                                        dressCodeNote = dressCodeNote.trim(),
                                        reminderEnabled = reminderEnabled,
                                        reminderPreset = if (reminderEnabled) reminderPreset else "none"
                                    )
                                    viewModel.updateEvent(
                                        event = updated,
                                        onSuccess = { viewModel.navigateBack() },
                                        onError = { locationError = it }
                                    )
                                }

                                viewModel.checkEventWarningsExcluding(
                                    customerId = event.customerId,
                                    eventName = eventName,
                                    eventDate = eventDate,
                                    eventDateMillis = eventDateMillis,
                                    eventTime = eventTime,
                                    excludeEventId = event.id,
                                    onPastWarning = { showPastWarningDialog = true },
                                    onDuplicateWarning = { showDuplicateWarningDialog = true },
                                    onClear = { executeUpdate() }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("edit_event_save_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioGreenSuccess)
                    ) {
                        Text(strings.saveChangesBtn, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    if (showPastWarningDialog) {
        WarningAlertDialog(
            title = "Past Event Warning",
            message = strings.pastEventWarningMsg,
            continueText = strings.continueBtn,
            cancelText = strings.cancelBtn,
            onContinue = {
                showPastWarningDialog = false
                val crewIds = selectedCrewMembers.map { it.id }.joinToString(",")
                val crewNamesStr = selectedCrewMembers.map { "${it.name} (${it.role})" }.joinToString(", ")
                if (event != null) {
                    val updated = event.copy(
                        eventName = eventName.trim(),
                        eventDate = eventDate.trim(),
                        eventDateMillis = if (eventDateMillis > 0) eventDateMillis else event.eventDateMillis,
                        eventTime = eventTime.trim(),
                        eventLocation = eventLocation.trim(),
                        crewIdsCsv = crewIds,
                        crewNames = crewNamesStr,
                        dressCodeNote = dressCodeNote.trim(),
                        reminderEnabled = reminderEnabled,
                        reminderPreset = if (reminderEnabled) reminderPreset else "none"
                    )
                    viewModel.updateEvent(
                        event = updated,
                        onSuccess = { viewModel.navigateBack() },
                        onError = { locationError = it }
                    )
                }
            },
            onCancel = { showPastWarningDialog = false }
        )
    }

    if (showDuplicateWarningDialog) {
        WarningAlertDialog(
            title = strings.duplicateEventTitle,
            message = strings.duplicateEventMsg,
            continueText = strings.continueBtn,
            cancelText = strings.cancelBtn,
            onContinue = {
                showDuplicateWarningDialog = false
                val crewIds = selectedCrewMembers.map { it.id }.joinToString(",")
                val crewNamesStr = selectedCrewMembers.map { "${it.name} (${it.role})" }.joinToString(", ")
                if (event != null) {
                    val updated = event.copy(
                        eventName = eventName.trim(),
                        eventDate = eventDate.trim(),
                        eventDateMillis = if (eventDateMillis > 0) eventDateMillis else event.eventDateMillis,
                        eventTime = eventTime.trim(),
                        eventLocation = eventLocation.trim(),
                        crewIdsCsv = crewIds,
                        crewNames = crewNamesStr,
                        dressCodeNote = dressCodeNote.trim(),
                        reminderEnabled = reminderEnabled,
                        reminderPreset = if (reminderEnabled) reminderPreset else "none"
                    )
                    viewModel.updateEvent(
                        event = updated,
                        onSuccess = { viewModel.navigateBack() },
                        onError = { locationError = it }
                    )
                }
            },
            onCancel = { showDuplicateWarningDialog = false }
        )
    }

    if (showCrewPickerModal) {
        CrewPickerModal(
            allCrew = allCrew,
            initialSelected = selectedCrewMembers,
            busyCrewMap = busyCrewMap,
            strings = strings,
            onDone = { list ->
                selectedCrewMembers.clear()
                selectedCrewMembers.addAll(list)
                showCrewPickerModal = false
            },
            onDismiss = { showCrewPickerModal = false },
            onAddNewCrew = {
                showCrewPickerModal = false
                viewModel.navigateTo(Screen.AddCrew)
            }
        )
    }
}

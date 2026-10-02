package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import com.example.ui.strings.StringsDefinition
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val strings = viewModel.strings
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    var savedCustomer by remember { mutableStateOf<Customer?>(null) }

    BackHandler {
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.addCustomerTitle,
                        fontWeight = FontWeight.Bold,
                        color = StudioDarkText
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("add_customer_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = StudioDarkText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = StudioBackground,
        modifier = modifier.testTag("add_customer_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (savedCustomer != null) {
                // Post-Save Success Card with Add Event & Done buttons
                CustomerSaveSuccessView(
                    customer = savedCustomer!!,
                    strings = strings,
                    onAddEvent = {
                        viewModel.navigateTo(Screen.EventBookingFlow(preselectedCustomerId = savedCustomer!!.id))
                    },
                    onDone = {
                        viewModel.navigateTo(Screen.Home, clearStack = true)
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        // Customer Name Field
                        Text(
                            text = strings.customerNameLabel,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = StudioDarkText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        StudioInputField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (nameError != null) nameError = null
                            },
                            placeholder = strings.customerNameHint,
                            isError = nameError != null,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_name_input")
                        )
                        if (nameError != null) {
                            Text(
                                text = nameError!!,
                                color = StudioRedPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                    }

                    item {
                        // Mobile Number Field
                        Text(
                            text = strings.mobileNumberLabel,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = StudioDarkText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        StudioInputField(
                            value = mobile,
                            onValueChange = {
                                if (it.length <= 15) {
                                    mobile = it.filter { char -> char.isDigit() || char == '+' }
                                    if (mobileError != null) mobileError = null
                                }
                            },
                            placeholder = strings.mobileNumberHint,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            isError = mobileError != null,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_mobile_input")
                        )
                        if (mobileError != null) {
                            Text(
                                text = mobileError!!,
                                color = StudioRedPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                    }

                    item {
                        // Optional Note (Max 1000 chars)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.noteLabel,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = StudioDarkText
                            )
                            Text(
                                text = "${note.length}/1000",
                                fontSize = 12.sp,
                                color = StudioSecondaryGray
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        StudioInputField(
                            value = note,
                            onValueChange = {
                                if (it.length <= 1000) note = it
                            },
                            placeholder = strings.noteHint,
                            singleLine = false,
                            minLines = 4,
                            maxLines = 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_note_input")
                        )
                    }
                }

                // Bottom Action: Large Green Button
                Button(
                    onClick = {
                        var hasError = false
                        if (name.trim().isBlank()) {
                            nameError = strings.errCustomerNameRequired
                            hasError = true
                        }
                        if (!viewModel.isValidMobile(mobile)) {
                            mobileError = strings.errValidMobileRequired
                            hasError = true
                        }

                        if (!hasError) {
                            viewModel.saveCustomer(
                                name = name,
                                mobile = mobile,
                                note = note,
                                onSuccess = { savedCustomer = it },
                                onError = { mobileError = it }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(52.dp)
                        .testTag("save_customer_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioGreenSuccess,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.saveCustomerBtn,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerSaveSuccessView(
    customer: Customer,
    strings: StringsDefinition,
    onAddEvent: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(StudioGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = StudioGreenSuccess,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = strings.customerSavedSuccess,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = StudioDarkText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${customer.name} • ${customer.mobile}",
            fontSize = 14.sp,
            color = StudioSecondaryGray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Large Add Event Button
        Button(
            onClick = onAddEvent,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("success_add_event_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StudioRedPrimary,
                contentColor = Color.White
            )
        ) {
            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.addEventBtn,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Done Button
        OutlinedButton(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("success_done_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioDarkText),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(StudioLightBorder))
        ) {
            Text(
                text = strings.doneBtn,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCustomerScreen(
    customerId: Long,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val strings = viewModel.strings
    val customers by viewModel.allCustomers.collectAsState()
    val customer = customers.find { it.id == customerId }

    var name by remember(customer) { mutableStateOf(customer?.name ?: "") }
    var mobile by remember(customer) { mutableStateOf(customer?.mobile ?: "") }
    var note by remember(customer) { mutableStateOf(customer?.note ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }

    BackHandler {
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.editCustomerTitle,
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
        modifier = modifier.testTag("edit_customer_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.customerNameLabel,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    StudioInputField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (nameError != null) nameError = null
                        },
                        isError = nameError != null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_customer_name_input")
                    )
                    if (nameError != null) {
                        Text(nameError!!, color = StudioRedPrimary, fontSize = 12.sp)
                    }
                }

                item {
                    Text(
                        text = strings.mobileNumberLabel,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    StudioInputField(
                        value = mobile,
                        onValueChange = {
                            mobile = it.filter { c -> c.isDigit() || c == '+' }
                            if (mobileError != null) mobileError = null
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = mobileError != null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_customer_mobile_input")
                    )
                    if (mobileError != null) {
                        Text(mobileError!!, color = StudioRedPrimary, fontSize = 12.sp)
                    }
                }

                item {
                    Text(
                        text = strings.noteLabel,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    StudioInputField(
                        value = note,
                        onValueChange = { if (it.length <= 1000) note = it },
                        singleLine = false,
                        minLines = 4,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_customer_note_input")
                    )
                }
            }

            // Buttons: Save Changes and Cancel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("edit_customer_cancel_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(strings.cancelBtn, color = StudioSecondaryGray)
                }

                Button(
                    onClick = {
                        var hasError = false
                        if (name.trim().isBlank()) {
                            nameError = strings.errCustomerNameRequired
                            hasError = true
                        }
                        if (!viewModel.isValidMobile(mobile)) {
                            mobileError = strings.errValidMobileRequired
                            hasError = true
                        }

                        if (!hasError) {
                            viewModel.updateCustomer(
                                id = customerId,
                                name = name,
                                mobile = mobile,
                                note = note,
                                onSuccess = { viewModel.navigateBack() },
                                onError = { mobileError = it }
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                        .testTag("edit_customer_save_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioGreenSuccess)
                ) {
                    Text(strings.saveChangesBtn, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsScreen(
    customerId: Long,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val strings = viewModel.strings
    val customers by viewModel.allCustomers.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val allCrew by viewModel.allCrew.collectAsState()
    val customer = customers.find { it.id == customerId }
    val customerEvents = allEvents.filter { it.customerId == customerId }

    var eventToDelete by remember { mutableStateOf<EventBooking?>(null) }
    var showCallSheetModal by remember { mutableStateOf(false) }
    var showDeleteCustomerConfirm by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.customerDetailsTitle,
                        fontWeight = FontWeight.Bold,
                        color = StudioDarkText
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("customer_details_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    StudioAppSmallTag(modifier = Modifier.padding(end = 4.dp))
                    customer?.let {
                        if (customerEvents.isNotEmpty()) {
                            IconButton(
                                onClick = { showCallSheetModal = true },
                                modifier = Modifier.testTag("customer_details_whatsapp_sheet_icon")
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = "WhatsApp Call Sheet", tint = StudioGreenDark)
                            }
                        }
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.EditCustomer(it.id)) },
                            modifier = Modifier.testTag("customer_details_edit_action")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Customer", tint = StudioDarkText)
                        }
                        IconButton(
                            onClick = { showDeleteCustomerConfirm = true },
                            modifier = Modifier.testTag("customer_details_delete_action")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Customer", tint = StudioRedPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = StudioBackground,
        modifier = modifier.testTag("customer_details_screen")
    ) { padding ->
        if (customer == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Customer not found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // Customer Profile Header Card
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(StudioRedLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = customer.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = StudioRedDark
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = customer.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioDarkText
                                    )
                                    Text(
                                        text = "📱 ${customer.mobile}",
                                        fontSize = 14.sp,
                                        color = StudioSecondaryGray
                                    )
                                }
                            }

                            if (customer.note.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "📝 ${customer.note}",
                                    fontSize = 13.sp,
                                    color = StudioDarkText
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action buttons: Call, WhatsApp, Edit
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.openDialer(customer.mobile) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .testTag("cust_details_call_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioGreenDark)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.actionCall, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        if (customerEvents.isNotEmpty()) {
                                            showCallSheetModal = true
                                        } else {
                                            val msg = "નમસ્તે ${customer.name}, સ્ટુડિયોમાંથી સંપર્ક કરું છું."
                                            viewModel.openWhatsApp(customer.mobile, msg)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(40.dp)
                                        .testTag("cust_details_whatsapp_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StudioGreenSuccess)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (customerEvents.isNotEmpty()) "Call Sheet" else strings.actionWhatsApp,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.navigateTo(Screen.EditCustomer(customer.id)) },
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .height(40.dp)
                                        .testTag("cust_details_edit_btn"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.actionEdit, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // WhatsApp Call Sheet Banner Button
                if (customerEvents.isNotEmpty()) {
                    item {
                        Button(
                            onClick = { showCallSheetModal = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("cust_details_callsheet_banner_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StudioGreenDark)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WhatsApp Call Sheet (${customerEvents.size} Functions)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Events Section Header + "+ Add Another Event"
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.customerEventsHeader,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioDarkText
                        )

                        Button(
                            onClick = {
                                viewModel.navigateTo(Screen.EventBookingFlow(preselectedCustomerId = customer.id))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("details_add_another_event_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.addAnotherEventBtn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Events List
                if (customerEvents.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = strings.noEventsBooked,
                                    fontSize = 14.sp,
                                    color = StudioSecondaryGray
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        viewModel.navigateTo(Screen.EventBookingFlow(preselectedCustomerId = customer.id))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary)
                                ) {
                                    Text(strings.addEventBtn, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(customerEvents, key = { it.id }) { event ->
                        CustomerEventItemCard(
                            event = event,
                            strings = strings,
                            onView = { viewModel.navigateTo(Screen.EventDetails(event.id)) },
                            onEdit = { viewModel.navigateTo(Screen.EditEvent(event.id)) },
                            onDelete = { eventToDelete = event },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    eventToDelete?.let { event ->
        SimpleConfirmationDialog(
            title = strings.deleteConfirmationTitle,
            message = "${strings.deleteEventConfirmMsg} (${event.eventName})",
            confirmText = strings.actionDelete,
            cancelText = strings.cancelBtn,
            onConfirm = {
                viewModel.deleteEvent(event)
                eventToDelete = null
            },
            onDismiss = { eventToDelete = null },
            isDestructive = true
        )
    }

    if (showCallSheetModal && customer != null && customerEvents.isNotEmpty()) {
        val callSheetText = viewModel.getCallSheetForCustomer(customer.id)
        val allAssignedIds = customerEvents.flatMap { ev ->
            ev.crewIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
        }.distinct()
        val assignedCrewMembers = allCrew.filter { it.id in allAssignedIds }

        EventCallSheetModal(
            callSheetText = callSheetText,
            customerMobile = customer.mobile,
            customerName = customer.name,
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

    if (showDeleteCustomerConfirm && customer != null) {
        SimpleConfirmationDialog(
            title = strings.deleteConfirmationTitle,
            message = "${strings.deleteCustomerConfirmMsg} (${customer.name})",
            confirmText = strings.actionDelete,
            cancelText = strings.cancelBtn,
            onConfirm = {
                viewModel.deleteCustomer(customer) {
                    viewModel.navigateBack()
                }
                showDeleteCustomerConfirm = false
            },
            onDismiss = { showDeleteCustomerConfirm = false },
            isDestructive = true
        )
    }
}

@Composable
fun CustomerEventItemCard(
    event: EventBooking,
    strings: StringsDefinition,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, StudioLightBorder, RoundedCornerShape(12.dp))
            .clickable { onView() }
            .testTag("event_item_card_${event.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎉 ${event.eventName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioRedDark
                )

                // Status Badge
                val isCompleted = event.status == EventBooking.STATUS_COMPLETED
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isCompleted) StudioGreenLight else StudioRedLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isCompleted) strings.statusCompleted else strings.statusUpcoming,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) StudioGreenDark else StudioRedDark
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📅 ${event.eventDate}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = StudioDarkText
                )
                Text(
                    text = "⏰ ${event.eventTime}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = StudioDarkText
                )
            }

            Text(
                text = "📍 ${event.eventLocation}",
                fontSize = 13.sp,
                color = StudioSecondaryGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (event.crewNames.isNotBlank()) {
                Text(
                    text = "👥 ${event.crewNames}",
                    fontSize = 12.sp,
                    color = StudioSecondaryGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (event.dressCodeNote.isNotBlank()) {
                Text(
                    text = "👕 ${event.dressCodeNote}",
                    fontSize = 12.sp,
                    color = StudioDarkText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (event.reminderEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = if (event.reminderEnabled) StudioRedPrimary else StudioSecondaryGray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (event.reminderEnabled) "Reminder: ${event.reminderPreset.replace("_", " ")}" else "Reminder OFF",
                    fontSize = 11.sp,
                    color = if (event.reminderEnabled) StudioRedPrimary else StudioSecondaryGray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Actions: View, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onView) {
                    Text(strings.actionView, color = StudioDarkText, fontSize = 12.sp)
                }
                TextButton(onClick = onEdit) {
                    Text(strings.actionEdit, color = StudioDarkText, fontSize = 12.sp)
                }
                TextButton(onClick = onDelete) {
                    Text(strings.actionDelete, color = StudioRedPrimary, fontSize = 12.sp)
                }
            }
        }
    }
}

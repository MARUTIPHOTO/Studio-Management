package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import com.example.ui.strings.StringsDefinition
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun HomeScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val strings = viewModel.strings
    val profile by viewModel.studioProfile.collectAsState()
    val customers by viewModel.filteredCustomers.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val allCrew by viewModel.allCrew.collectAsState()
    val searchQuery by viewModel.customerSearchQuery.collectAsState()

    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    var callSheetCustomer by remember { mutableStateOf<Customer?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .testTag("home_screen")
    ) {
        // Header
        StudioHeader(
            profile = profile,
            onCallClick = { viewModel.openDialer(profile.mobileNumber) }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Main Shortcut Buttons
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // + Add Customer Button
                    Button(
                        onClick = { viewModel.navigateTo(Screen.AddCustomer) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("home_add_customer_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioRedPrimary,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.addCustomerBtn,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // + Event Booking Button
                    Button(
                        onClick = { viewModel.navigateTo(Screen.EventBookingFlow()) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("home_event_booking_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioRedLight,
                            contentColor = StudioRedDark
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = StudioRedDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.eventBookingBtn,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioRedDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Search Bar
            item {
                StudioInputField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateCustomerSearch(it) },
                    placeholder = strings.searchCustomerHint,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = StudioSecondaryGray
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateCustomerSearch("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = StudioSecondaryGray
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "customer_search_input",
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Customer List or Empty State
            if (customers.isEmpty()) {
                item {
                    EmptyCustomerState(
                        strings = strings,
                        onAddCustomer = { viewModel.navigateTo(Screen.AddCustomer) }
                    )
                }
            } else {
                items(customers, key = { it.id }) { customer ->
                    // Find latest event for this customer
                    val customerEvents = allEvents.filter { it.customerId == customer.id }
                    val latestEvent = customerEvents.maxByOrNull { it.eventDateMillis }

                    CustomerCard(
                        customer = customer,
                        latestEvent = latestEvent,
                        eventCount = customerEvents.size,
                        strings = strings,
                        onView = { viewModel.navigateTo(Screen.CustomerDetails(customer.id)) },
                        onEdit = { viewModel.navigateTo(Screen.EditCustomer(customer.id)) },
                        onDelete = { customerToDelete = customer },
                        onCall = { viewModel.openDialer(customer.mobile) },
                        onWhatsApp = {
                            if (customerEvents.isNotEmpty()) {
                                callSheetCustomer = customer
                            } else {
                                val msg = "નમસ્તે ${customer.name}, સ્ટુડિયોમાં આપનું સ્વાગત છે."
                                viewModel.openWhatsApp(customer.mobile, msg)
                            }
                        },
                        onEvents = { viewModel.navigateTo(Screen.CustomerDetails(customer.id)) },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    customerToDelete?.let { customer ->
        SimpleConfirmationDialog(
            title = strings.deleteConfirmationTitle,
            message = "${strings.deleteCustomerConfirmMsg} (${customer.name})",
            confirmText = strings.actionDelete,
            cancelText = strings.cancelBtn,
            onConfirm = {
                viewModel.deleteCustomer(customer)
                customerToDelete = null
            },
            onDismiss = { customerToDelete = null },
            isDestructive = true
        )
    }

    // Call Sheet Dialog for Customer from HomeScreen
    callSheetCustomer?.let { customer ->
        val customerEvents = allEvents.filter { it.customerId == customer.id }
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
            onDismiss = { callSheetCustomer = null }
        )
    }
}

@Composable
fun CustomerCard(
    customer: Customer,
    latestEvent: EventBooking?,
    eventCount: Int,
    strings: StringsDefinition,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onEvents: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, StudioLightBorder, RoundedCornerShape(12.dp))
            .clickable { onView() }
            .testTag("customer_card_${customer.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Name, Mobile and Action Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(StudioRedLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = customer.name.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = StudioRedDark
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = customer.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = StudioDarkText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📱 ${customer.mobile}",
                                fontSize = 13.sp,
                                color = StudioSecondaryGray,
                                fontWeight = FontWeight.Medium
                            )
                            if (eventCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(StudioRedLight)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$eventCount ${strings.customerEventsHeader}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioRedDark
                                    )
                                }
                            }
                        }
                    }
                }

                // 3-dot overflow menu for Edit / Delete
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("customer_menu_button_${customer.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = StudioSecondaryGray
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.actionView, color = StudioDarkText) },
                            leadingIcon = {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = StudioSecondaryGray)
                            },
                            onClick = {
                                menuExpanded = false
                                onView()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.actionEdit, color = StudioDarkText) },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = StudioSecondaryGray)
                            },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.actionDelete, color = StudioRedPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = StudioRedPrimary)
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Latest Event Information Box
            if (latestEvent != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioSurfaceVariant)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎉 ${latestEvent.eventName}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioRedDark
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "📅 ${latestEvent.eventDate}",
                                fontSize = 12.sp,
                                color = StudioDarkText,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "⏰ ${latestEvent.eventTime}",
                                fontSize = 12.sp,
                                color = StudioSecondaryGray
                            )
                            Text(
                                text = "📍 ${latestEvent.eventLocation}",
                                fontSize = 12.sp,
                                color = StudioSecondaryGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (latestEvent.crewNames.isNotBlank()) {
                            Text(
                                text = "👥 ${latestEvent.crewNames}",
                                fontSize = 11.sp,
                                color = StudioSecondaryGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "📅 ${strings.noEventsBooked}",
                        fontSize = 12.sp,
                        color = StudioSecondaryGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fast Action Row: Call, WhatsApp, Events
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Call Button
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("customer_call_btn_${customer.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioGreenDark),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(StudioGreenSuccess))
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = StudioGreenDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = strings.actionCall,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioGreenDark
                    )
                }

                // WhatsApp Button
                Button(
                    onClick = onWhatsApp,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(38.dp)
                        .testTag("customer_whatsapp_btn_${customer.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioGreenSuccess,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = strings.actionWhatsApp,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Events View Shortcut
                OutlinedButton(
                    onClick = onEvents,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(38.dp)
                        .testTag("customer_events_btn_${customer.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioRedPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(StudioRedPrimary))
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "Events",
                        tint = StudioRedPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = strings.actionEvents,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioRedPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCustomerState(
    strings: StringsDefinition,
    onAddCustomer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(StudioRedLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = StudioRedPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = strings.noCustomersYet,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = StudioDarkText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = strings.noCustomersSub,
            fontSize = 14.sp,
            color = StudioSecondaryGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddCustomer,
            colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .height(48.dp)
                .testTag("empty_state_add_customer_btn")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(strings.addCustomerBtn, fontWeight = FontWeight.Bold)
        }
    }
}

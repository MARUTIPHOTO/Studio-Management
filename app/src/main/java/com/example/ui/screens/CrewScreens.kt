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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CrewMember
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

data class CrewRoleItem(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val category: String
)

val CREW_ROLE_CATEGORIES = listOf(
    "Photography & Videography" to listOf(
        CrewRoleItem("Traditional Photographer", "ફોટોગ્રાફી", "📸", "Photography & Videography"),
        CrewRoleItem("Traditional Videographer", "વિડિયોગ્રાફી", "🎥", "Photography & Videography"),
        CrewRoleItem("Cinematographer", "સિનેમેટિક વિડિયો", "🎬", "Photography & Videography"),
        CrewRoleItem("Candid Photographer", "કેન્ડિડ ફોટોગ્રાફી", "📸", "Photography & Videography"),
        CrewRoleItem("Photo Editor", "ફોટો એડિટિંગ", "🖥️", "Photography & Videography"),
        CrewRoleItem("Video Editor", "વિડિયો એડિટિંગ", "🎞️", "Photography & Videography")
    ),
    "Supporting Crew" to listOf(
        CrewRoleItem("Drone Operator", "ડ્રોન શૂટ", "🚁", "Supporting Crew"),
        CrewRoleItem("Live/LED Operator", "લાઇવ / LED", "🎤", "Supporting Crew"),
        CrewRoleItem("Light Man", "લાઇટિંગ", "💡", "Supporting Crew"),
        CrewRoleItem("Assistant Photographer", "ફોટોગ્રાફર આસિસ્ટન્ટ", "👤", "Supporting Crew"),
        CrewRoleItem("Assistant Videographer", "વિડિયોગ્રાફર આસિસ્ટન્ટ", "🎥", "Supporting Crew"),
        CrewRoleItem("Event Coordinator", "ઇવેન્ટ મેનેજમેન્ટ", "📋", "Supporting Crew"),
        CrewRoleItem("Driver", "ટ્રાન્સપોર્ટ", "🚗", "Supporting Crew"),
        CrewRoleItem("Other", "પોતાની Role લખી શકાય", "✍️", "Supporting Crew")
    )
)

val ALL_CREW_ROLES: List<CrewRoleItem> = CREW_ROLE_CATEGORIES.flatMap { it.second }

@Composable
fun CrewRoleSelectionDialog(
    selectedRole: String,
    customRole: String,
    onRoleSelected: (role: String, customRoleText: String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedRole) }
    var currentCustomRole by remember { mutableStateOf(customRole) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Select Crew Role",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = StudioDarkText
                        )
                        Text(
                            text = "ક્રૂ માટે રોલ પસંદ કરો",
                            fontSize = 13.sp,
                            color = StudioSecondaryGray
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = StudioSecondaryGray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = StudioLightBorder)

                // Scrollable list of categories and roles
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 440.dp)
                        .padding(horizontal = 14.dp)
                ) {
                    CREW_ROLE_CATEGORIES.forEach { (categoryName, roleItems) ->
                        item(key = "cat_$categoryName") {
                            val catEmoji = if (categoryName.startsWith("Photography")) "📸" else "🎥"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StudioSurfaceVariant)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$catEmoji $categoryName",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioRedDark
                                )
                            }
                        }

                        items(roleItems, key = { it.title }) { item ->
                            val isSelected = currentSelection == item.title
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) StudioRedLight else Color.Transparent)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            currentSelection = item.title
                                        }
                                        .padding(horizontal = 10.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.emoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = if (isSelected) StudioRedPrimary else StudioDarkText
                                        )
                                        Text(
                                            text = item.subtitle,
                                            fontSize = 12.sp,
                                            color = StudioSecondaryGray
                                        )
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { currentSelection = item.title },
                                        colors = RadioButtonDefaults.colors(selectedColor = StudioRedPrimary)
                                    )
                                }

                                // If "Other" is selected, show an inline input right here in the dialog!
                                if (item.title == "Other" && isSelected) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 12.dp, end = 12.dp, bottom = 10.dp)
                                    ) {
                                        Text(
                                            text = "✍️ પોતાની Role અહીં લખો (Type custom role):",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StudioRedPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        StudioInputField(
                                            value = currentCustomRole,
                                            onValueChange = { currentCustomRole = it },
                                            placeholder = "દા.ત. Crane Operator, Album Designer...",
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            testTag = "dialog_other_role_input"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        onRoleSelected(currentSelection, currentCustomRole)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary)
                ) {
                    Text("OK / Done (પસંદ કરો)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrewHomeScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val strings = viewModel.strings
    val crewList by viewModel.filteredCrew.collectAsState()
    val searchQuery by viewModel.crewSearchQuery.collectAsState()
    var crewToDelete by remember { mutableStateOf<CrewMember?>(null) }

    BackHandler {
        viewModel.navigateBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .testTag("crew_home_screen")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = strings.crewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = StudioDarkText
                )
            },
            actions = {
                StudioAppSmallTag(modifier = Modifier.padding(end = 12.dp))
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Top + Add Crew Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.navigateTo(Screen.AddCrew) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("crew_home_add_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.addCrewBtn,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Search Bar
            item {
                StudioInputField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateCrewSearch(it) },
                    placeholder = strings.searchCrewHint,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = StudioSecondaryGray)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateCrewSearch("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = StudioSecondaryGray)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "crew_search_input",
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Crew Cards List or Empty State
            if (crewList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(StudioRedLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = StudioRedPrimary, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(strings.noCrewYet, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StudioDarkText)
                    }
                }
            } else {
                items(crewList, key = { it.id }) { crew ->
                    CrewCardItem(
                        crew = crew,
                        strings = strings,
                        onEdit = { viewModel.navigateTo(Screen.EditCrew(crew.id)) },
                        onDelete = { crewToDelete = crew },
                        onCall = { viewModel.openDialer(crew.mobile) },
                        onWhatsApp = {
                            val msg = "નમસ્તે ${crew.name}, સ્ટુડિયોમાંથી ઇવેન્ટ શિડ્યુલ બાબતે સંપર્ક કરું છું."
                            viewModel.openWhatsApp(crew.mobile, msg)
                        },
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
    crewToDelete?.let { crew ->
        SimpleConfirmationDialog(
            title = strings.deleteConfirmationTitle,
            message = "${strings.deleteCrewConfirmMsg} (${crew.name})",
            confirmText = strings.actionDelete,
            cancelText = strings.cancelBtn,
            onConfirm = {
                viewModel.deleteCrew(crew)
                crewToDelete = null
            },
            onDismiss = { crewToDelete = null },
            isDestructive = true
        )
    }
}

@Composable
fun CrewCardItem(
    crew: CrewMember,
    strings: StringsDefinition,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, StudioLightBorder, RoundedCornerShape(12.dp))
            .testTag("crew_card_${crew.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        val roleItem = ALL_CREW_ROLES.find { it.title.equals(crew.role, ignoreCase = true) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Avatar, Name, Role badge, and Edit/Delete icons
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(StudioRedLight),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when {
                            crew.role.contains("Video", ignoreCase = true) || crew.role.contains("Cinematograph", ignoreCase = true) -> Icons.Default.Videocam
                            crew.role.contains("Drone", ignoreCase = true) -> Icons.Default.Flight
                            crew.role.contains("Edit", ignoreCase = true) -> Icons.Default.Movie
                            crew.role.contains("Light", ignoreCase = true) -> Icons.Default.FlashOn
                            crew.role.contains("Driver", ignoreCase = true) -> Icons.Default.DirectionsCar
                            crew.role.contains("Coordinator", ignoreCase = true) || crew.role.contains("Live", ignoreCase = true) -> Icons.Default.Assignment
                            else -> Icons.Default.PhotoCamera
                        }
                        Icon(imageVector = icon, contentDescription = null, tint = StudioRedDark, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = crew.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = StudioDarkText
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StudioSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            val roleBadgeText = if (roleItem != null) "${roleItem.emoji} ${crew.role}" else crew.role
                            Text(
                                text = roleBadgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioRedPrimary
                            )
                        }
                    }
                }

                // Edit and Delete Icon buttons
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = StudioSecondaryGray, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StudioRedPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📱 ${crew.mobile}",
                fontSize = 13.sp,
                color = StudioDarkText,
                fontWeight = FontWeight.Medium
            )

            if (crew.cameraEquipment.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📷 ${crew.cameraEquipment}",
                    fontSize = 12.sp,
                    color = StudioSecondaryGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (crew.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📝 ${crew.note}",
                    fontSize = 12.sp,
                    color = StudioSecondaryGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Call & WhatsApp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("crew_call_btn_${crew.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioGreenDark),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(StudioGreenSuccess))
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = StudioGreenDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.actionCall, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StudioGreenDark)
                }

                Button(
                    onClick = onWhatsApp,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(38.dp)
                        .testTag("crew_whatsapp_btn_${crew.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioGreenSuccess)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.actionWhatsApp, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// SCREEN 14 – ADD CREW SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCrewScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val strings = viewModel.strings
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Traditional Photographer") }
    var customRole by remember { mutableStateOf("") }
    var cameraEquipment by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var showRoleDialog by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    var customRoleError by remember { mutableStateOf<String?>(null) }
    var showDuplicateMobileWarning by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateBack()
    }

    fun submitSave() {
        val finalRole = if (selectedRole == "Other") {
            if (customRole.trim().isNotBlank()) customRole.trim() else "Other"
        } else selectedRole

        viewModel.saveCrew(
            name = name,
            mobile = mobile,
            role = finalRole,
            cameraEquipment = cameraEquipment,
            note = note,
            onSuccess = {
                viewModel.navigateBack()
            },
            onError = { mobileError = it }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.addCrewTitle, fontWeight = FontWeight.Bold, color = StudioDarkText) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = StudioBackground,
        modifier = modifier.testTag("add_crew_screen")
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
                    // Crew Name *
                    Text(text = strings.crewNameLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    StudioInputField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (nameError != null) nameError = null
                        },
                        placeholder = strings.crewNameHint,
                        isError = nameError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "add_crew_name_input"
                    )
                    if (nameError != null) {
                        Text(nameError!!, color = StudioRedPrimary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                item {
                    // Mobile Number *
                    Text(text = strings.mobileNumberLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    StudioInputField(
                        value = mobile,
                        onValueChange = {
                            mobile = it.filter { c -> c.isDigit() || c == '+' }
                            if (mobileError != null) mobileError = null
                        },
                        placeholder = strings.mobileNumberHint,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = mobileError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "add_crew_mobile_input"
                    )
                    if (mobileError != null) {
                        Text(mobileError!!, color = StudioRedPrimary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                item {
                    // Role * (Category wise dialog)
                    Text(text = strings.crewRoleLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    val currentRoleItem = ALL_CREW_ROLES.find { it.title == selectedRole }
                    StudioClickableBox(
                        onClick = { showRoleDialog = true },
                        modifier = Modifier.testTag("add_crew_role_selector")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                val displayEmoji = if (selectedRole == "Other") "✍️" else (currentRoleItem?.emoji ?: "👤")
                                Text(displayEmoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    val displayText = if (selectedRole == "Other" && customRole.isNotBlank()) {
                                        customRole
                                    } else {
                                        selectedRole
                                    }
                                    val displaySub = if (selectedRole == "Other") {
                                        if (customRole.isNotBlank()) "પોતાની Role (Custom Role)" else "પોતાની Role લખી શકાય"
                                    } else {
                                        currentRoleItem?.subtitle ?: ""
                                    }
                                    Text(
                                        text = displayText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StudioDarkText
                                    )
                                    if (displaySub.isNotBlank()) {
                                        Text(
                                            text = displaySub,
                                            fontSize = 12.sp,
                                            color = StudioSecondaryGray
                                        )
                                    }
                                }
                            }
                            Text("▼", fontSize = 11.sp, color = StudioSecondaryGray)
                        }
                    }

                    // If "Other" is selected, allow typing custom role
                    if (selectedRole == "Other") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "✍️ Enter Custom Role / પોતાની Role લખો *",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = StudioRedPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        StudioInputField(
                            value = customRole,
                            onValueChange = {
                                customRole = it
                                if (customRoleError != null) customRoleError = null
                            },
                            placeholder = "e.g. Crane Operator, Album Designer...",
                            isError = customRoleError != null,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "add_crew_custom_role_input"
                        )
                        if (customRoleError != null) {
                            Text(customRoleError!!, color = StudioRedPrimary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }

                item {
                    // Camera / Equipment Details (Optional)
                    Text(text = strings.cameraEquipmentLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    StudioInputField(
                        value = cameraEquipment,
                        onValueChange = { cameraEquipment = it },
                        placeholder = strings.cameraEquipmentHint,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "add_crew_camera_input"
                    )
                }

                item {
                    // Note (Optional)
                    Text(text = strings.noteLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    StudioInputField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = strings.noteHint,
                        singleLine = false,
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "add_crew_note_input"
                    )
                }
            }

            // Save Crew Button
            Button(
                onClick = {
                    var hasError = false
                    if (name.trim().isBlank()) {
                        nameError = "Crew member name is required."
                        hasError = true
                    }
                    if (!viewModel.isValidMobile(mobile)) {
                        mobileError = strings.errValidMobileRequired
                        hasError = true
                    }
                    if (selectedRole == "Other" && customRole.trim().isBlank()) {
                        customRoleError = "Please enter custom role"
                        hasError = true
                    }

                    if (!hasError) {
                        // Check if mobile already exists (Section 25)
                        viewModel.checkCrewMobileDuplicate(
                            mobile = mobile,
                            onDuplicate = { showDuplicateMobileWarning = true },
                            onClear = { submitSave() }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(52.dp)
                    .testTag("save_crew_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StudioGreenSuccess)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.saveCrewBtn, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    if (showDuplicateMobileWarning) {
        WarningAlertDialog(
            title = "Mobile Already Exists",
            message = strings.crewMobileExistsMsg,
            continueText = strings.continueBtn,
            cancelText = strings.cancelBtn,
            onContinue = {
                showDuplicateMobileWarning = false
                submitSave()
            },
            onCancel = { showDuplicateMobileWarning = false }
        )
    }

    if (showRoleDialog) {
        CrewRoleSelectionDialog(
            selectedRole = selectedRole,
            customRole = customRole,
            onRoleSelected = { chosenRole, chosenCustomRole ->
                selectedRole = chosenRole
                if (chosenRole == "Other") {
                    customRole = chosenCustomRole
                }
                showRoleDialog = false
            },
            onDismiss = { showRoleDialog = false }
        )
    }
}

// SCREEN 15 – EDIT CREW SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCrewScreen(
    crewId: Long,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val strings = viewModel.strings
    val allCrew by viewModel.allCrew.collectAsState()
    val crew = allCrew.find { it.id == crewId }

    val predefinedRole = ALL_CREW_ROLES.find { it.title.equals(crew?.role, ignoreCase = true) }
    var name by remember(crew) { mutableStateOf(crew?.name ?: "") }
    var mobile by remember(crew) { mutableStateOf(crew?.mobile ?: "") }
    var selectedRole by remember(crew) {
        mutableStateOf(predefinedRole?.title ?: if (crew?.role?.isNotBlank() == true) "Other" else "Traditional Photographer")
    }
    var customRole by remember(crew) {
        mutableStateOf(if (predefinedRole == null && crew?.role?.isNotBlank() == true && crew.role != "Other") crew.role else "")
    }
    var cameraEquipment by remember(crew) { mutableStateOf(crew?.cameraEquipment ?: "") }
    var note by remember(crew) { mutableStateOf(crew?.note ?: "") }

    var showRoleDialog by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    var customRoleError by remember { mutableStateOf<String?>(null) }

    BackHandler {
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.editCrewTitle, fontWeight = FontWeight.Bold, color = StudioDarkText) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = StudioBackground,
        modifier = modifier.testTag("edit_crew_screen")
    ) { padding ->
        if (crew == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Crew not found")
            }
        } else {
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
                        Text(text = strings.crewNameLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        StudioInputField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (nameError != null) nameError = null
                            },
                            isError = nameError != null,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "edit_crew_name_input"
                        )
                        if (nameError != null) {
                            Text(nameError!!, color = StudioRedPrimary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }

                    item {
                        Text(text = strings.mobileNumberLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "edit_crew_mobile_input"
                        )
                        if (mobileError != null) {
                            Text(mobileError!!, color = StudioRedPrimary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }

                    item {
                        Text(text = strings.crewRoleLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        val currentRoleItem = ALL_CREW_ROLES.find { it.title == selectedRole }
                        StudioClickableBox(
                            onClick = { showRoleDialog = true },
                            modifier = Modifier.testTag("edit_crew_role_selector")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val displayEmoji = if (selectedRole == "Other") "✍️" else (currentRoleItem?.emoji ?: "👤")
                                    Text(displayEmoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        val displayText = if (selectedRole == "Other" && customRole.isNotBlank()) {
                                            customRole
                                        } else {
                                            selectedRole
                                        }
                                        val displaySub = if (selectedRole == "Other") {
                                            if (customRole.isNotBlank()) "પોતાની Role (Custom Role)" else "પોતાની Role લખી શકાય"
                                        } else {
                                            currentRoleItem?.subtitle ?: ""
                                        }
                                        Text(
                                            text = displayText,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = StudioDarkText
                                        )
                                        if (displaySub.isNotBlank()) {
                                            Text(
                                                text = displaySub,
                                                fontSize = 12.sp,
                                                color = StudioSecondaryGray
                                            )
                                        }
                                    }
                                }
                                Text("▼", fontSize = 11.sp, color = StudioSecondaryGray)
                            }
                        }

                        // If "Other" is selected, allow editing custom role
                        if (selectedRole == "Other") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "✍️ પોતાની Role અહીં લખો (Enter Custom Role) *",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = StudioRedPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            StudioInputField(
                                value = customRole,
                                onValueChange = {
                                    customRole = it
                                    if (customRoleError != null) customRoleError = null
                                },
                                placeholder = "દા.ત. Crane Operator, Album Designer...",
                                isError = customRoleError != null,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "edit_crew_custom_role_input"
                            )
                            if (customRoleError != null) {
                                Text(customRoleError!!, color = StudioRedPrimary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }

                    item {
                        Text(text = strings.cameraEquipmentLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        StudioInputField(
                            value = cameraEquipment,
                            onValueChange = { cameraEquipment = it },
                            placeholder = strings.cameraEquipmentHint,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "edit_crew_camera_input"
                        )
                    }

                    item {
                        Text(text = strings.noteLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        StudioInputField(
                            value = note,
                            onValueChange = { note = it },
                            placeholder = strings.noteHint,
                            singleLine = false,
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "edit_crew_note_input"
                        )
                    }
                }

                // Save Changes Button
                Button(
                    onClick = {
                        if (name.trim().isBlank()) {
                            nameError = "Crew member name is required."
                            return@Button
                        }
                        if (!viewModel.isValidMobile(mobile)) {
                            mobileError = strings.errValidMobileRequired
                            return@Button
                        }
                        if (selectedRole == "Other" && customRole.trim().isBlank()) {
                            customRoleError = "Please enter custom role"
                            return@Button
                        }

                        val finalRole = if (selectedRole == "Other") {
                            if (customRole.trim().isNotBlank()) customRole.trim() else "Other"
                        } else selectedRole

                        viewModel.updateCrew(
                            crew = crew.copy(
                                name = name.trim(),
                                mobile = mobile.trim(),
                                role = finalRole,
                                cameraEquipment = cameraEquipment.trim(),
                                note = note.trim()
                            ),
                            onSuccess = { viewModel.navigateBack() },
                            onError = { mobileError = it }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(52.dp)
                        .testTag("edit_crew_save_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioGreenSuccess)
                ) {
                    Text(strings.saveChangesBtn, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    if (showRoleDialog) {
        CrewRoleSelectionDialog(
            selectedRole = selectedRole,
            customRole = customRole,
            onRoleSelected = { chosenRole, chosenCustomRole ->
                selectedRole = chosenRole
                if (chosenRole == "Other") {
                    customRole = chosenCustomRole
                }
                showRoleDialog = false
            },
            onDismiss = { showRoleDialog = false }
        )
    }
}

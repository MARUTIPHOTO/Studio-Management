package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudioProfile
import com.example.ui.strings.AppLanguage
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
import com.example.ui.viewmodel.StudioViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val strings = viewModel.strings
    val profile by viewModel.studioProfile.collectAsState()
    val currentLang by viewModel.language.collectAsState()

    var studioName by remember(profile) { mutableStateOf(profile.studioName) }
    var ownerName by remember(profile) { mutableStateOf(profile.ownerName) }
    var mobileNumber by remember(profile) { mutableStateOf(profile.mobileNumber) }
    var whatsAppNumber by remember(profile) { mutableStateOf(profile.whatsAppNumber) }
    var address by remember(profile) { mutableStateOf(profile.address) }
    var logoUri by remember(profile) { mutableStateOf(profile.logoUri) }

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                val logoFile = java.io.File(context.filesDir, "studio_logo.jpg")
                logoFile.outputStream().use { output ->
                    inputStream?.copyTo(output)
                }
                val newLogoPath = logoFile.absolutePath
                logoUri = newLogoPath
                // Auto-save immediately so it reflects everywhere
                viewModel.saveStudioProfile(profile.copy(logoUri = newLogoPath))
            } catch (e: Exception) {
                val fallbackUri = selectedUri.toString()
                logoUri = fallbackUri
                viewModel.saveStudioProfile(profile.copy(logoUri = fallbackUri))
            }
        }
    }

    var showRestoreConfirm by remember { mutableStateOf(false) }
    var backupStatusMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.settingsTitle,
                        fontWeight = FontWeight.Bold,
                        color = StudioDarkText
                    )
                },
                actions = {
                    StudioAppSmallTag(modifier = Modifier.padding(end = 12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = StudioBackground,
        modifier = modifier.testTag("settings_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Section 1: Studio Profile
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StudioLogoBadge(logoUri = logoUri, size = 52, iconSize = 26)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = strings.studioProfileHeader,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioDarkText
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    StudioAppSmallTag()
                                }
                                Text(
                                    text = "Logo / ફોટો સેટ કરો (હોમ પેજ પર દેખાશે)",
                                    fontSize = 12.sp,
                                    color = StudioSecondaryGray
                                )
                            }
                        }

                        // Logo Upload / Change Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioRedPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioRedPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (logoUri.isNotBlank()) "Change Logo (ફોટો બદલો)" else "Set Logo (ફોટો સેટ કરો)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (logoUri.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        logoUri = ""
                                        viewModel.saveStudioProfile(profile.copy(logoUri = ""))
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioSecondaryGray),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = StudioSecondaryGray
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remove", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Studio Name
                        Text(text = strings.studioNameLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        StudioInputField(
                            value = studioName,
                            onValueChange = { studioName = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "settings_studio_name_input"
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Owner Name
                        Text(text = strings.ownerNameLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        StudioInputField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "settings_owner_name_input"
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Mobile Number
                        Text(text = strings.mobileNumberLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        StudioInputField(
                            value = mobileNumber,
                            onValueChange = { mobileNumber = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "settings_mobile_input"
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // WhatsApp Number
                        Text(text = strings.whatsAppNumberLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        StudioInputField(
                            value = whatsAppNumber,
                            onValueChange = { whatsAppNumber = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "settings_whatsapp_input"
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Full Address
                        Text(text = strings.fullAddressLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        StudioInputField(
                            value = address,
                            onValueChange = { address = it },
                            singleLine = false,
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "settings_address_input"
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Save Settings Button
                        Button(
                            onClick = {
                                viewModel.saveStudioProfile(
                                    profile.copy(
                                        studioName = studioName.trim(),
                                        ownerName = ownerName.trim(),
                                        mobileNumber = mobileNumber.trim(),
                                        whatsAppNumber = whatsAppNumber.trim(),
                                        address = address.trim(),
                                        logoUri = logoUri
                                    )
                                )
                                scope.launch {
                                    snackbarHostState.showSnackbar(strings.settingsSavedToast)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_settings_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StudioGreenSuccess)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.saveSettingsBtn, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            // Section 2: Language (ગુજરાતી, हिन्दी, English)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = StudioRedPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.languageHeader,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioDarkText
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        AppLanguage.entries.forEach { lang ->
                            val isSelected = currentLang == lang
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) StudioRedLight else Color.Transparent)
                                    .clickable { viewModel.setLanguage(lang) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setLanguage(lang) },
                                    colors = RadioButtonDefaults.colors(selectedColor = StudioRedPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = lang.displayName,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) StudioRedDark else StudioDarkText
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Data (Backup / Restore) - SCREEN 17
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = StudioRedPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.dataHeader,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioDarkText
                            )
                        }

                        val lastBackupText = if (profile.lastBackupTime > 0) {
                            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
                            sdf.format(Date(profile.lastBackupTime))
                        } else {
                            strings.neverBackedUp
                        }

                        Text(
                            text = "${strings.lastBackupLabel} $lastBackupText",
                            fontSize = 13.sp,
                            color = StudioSecondaryGray
                        )

                        if (backupStatusMsg != null) {
                            Text(
                                text = backupStatusMsg!!,
                                fontSize = 12.sp,
                                color = StudioGreenDark,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Backup Data Button
                            Button(
                                onClick = {
                                    viewModel.backupData {
                                        backupStatusMsg = "Backup created successfully!"
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("backup_data_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary)
                            ) {
                                Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.backupDataBtn, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            // Restore Data Button
                            OutlinedButton(
                                onClick = { showRestoreConfirm = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("restore_data_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioDarkText)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.restoreDataBtn, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section 4: Offline Status Indicator Notice (Section 30)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = StudioGreenDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = strings.offlineIndicatorMsg,
                            fontSize = 12.sp,
                            color = StudioSecondaryGray,
                            lineHeight = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // Restore Confirmation Dialog (SCREEN 17)
    if (showRestoreConfirm) {
        SimpleConfirmationDialog(
            title = "Restore Data Confirmation",
            message = strings.restoreConfirmationMsg,
            confirmText = strings.restoreBtn,
            cancelText = strings.cancelBtn,
            onConfirm = {
                viewModel.restoreData { success ->
                    backupStatusMsg = if (success) "Data successfully restored!" else "No backup snapshot found."
                }
                showRestoreConfirm = false
            },
            onDismiss = { showRestoreConfirm = false },
            isDestructive = false
        )
    }
}

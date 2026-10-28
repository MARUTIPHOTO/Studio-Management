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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.res.stringResource
import com.example.R
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
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.StudioViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun getLocalizedResString(
    context: android.content.Context,
    lang: AppLanguage,
    @StringRes resId: Int,
    fallback: String
): String {
    return try {
        val locale = java.util.Locale(lang.code)
        val config = android.content.res.Configuration(context.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        val str = context.createConfigurationContext(config).resources.getString(resId)
        if (str.isNotBlank()) str else fallback
    } catch (e: Exception) {
        fallback
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.language.collectAsState()
    val strings = remember(currentLang) { com.example.ui.strings.AppStrings.get(currentLang) }
    val profile by viewModel.studioProfile.collectAsState()
    val isAdMobBannerEnabled by viewModel.isAdMobBannerEnabled.collectAsState()
    val context = LocalContext.current

    var studioName by remember { mutableStateOf(profile.studioName) }
    var ownerName by remember { mutableStateOf(profile.ownerName) }
    var mobileNumber by remember { mutableStateOf(profile.mobileNumber) }
    var whatsAppNumber by remember { mutableStateOf(profile.whatsAppNumber) }
    var address by remember { mutableStateOf(profile.address) }
    var logoUri by remember { mutableStateOf(profile.logoUri) }
    var showSaveSuccessBanner by remember { mutableStateOf(false) }

    // Dynamic string resolution based on current selected language preference
    val studioProfileHeader = remember(currentLang) {
        getLocalizedResString(context, currentLang, R.string.studio_profile_header, strings.studioProfileHeader)
    }
    val studioLogoSubtitle = remember(currentLang) {
        getLocalizedResString(context, currentLang, R.string.studio_logo_subtitle, strings.studioLogoSubtitle)
    }
    val setLogoText = remember(currentLang) {
        getLocalizedResString(context, currentLang, R.string.set_logo, strings.setLogoBtn)
    }
    val changeLogoText = remember(currentLang) {
        getLocalizedResString(context, currentLang, R.string.change_logo, strings.changeLogoBtn)
    }
    val removeLogoText = remember(currentLang) {
        getLocalizedResString(context, currentLang, R.string.remove_logo, strings.removeLogoBtn)
    }
    val studioNameLabel = remember(currentLang) {
        getLocalizedResString(context, currentLang, R.string.studio_name_label, strings.studioNameLabel)
    }
    val ownerNameLabel = remember(currentLang) {
        getLocalizedResString(context, currentLang, R.string.owner_name_label, strings.ownerNameLabel)
    }
    val mobileNumberLabel = remember(currentLang) {
        getLocalizedResString(context, currentLang, R.string.mobile_number_label, strings.mobileNumberLabel)
    }
    val whatsAppNumberLabel = remember(currentLang) {
        getLocalizedResString(context, currentLang, R.string.whatsapp_number_label, strings.whatsAppNumberLabel)
    }
    val fullAddressLabel = remember(currentLang) {
        getLocalizedResString(context, currentLang, R.string.full_address_label, strings.fullAddressLabel)
    }
    val saveSettingsBtnText = remember(currentLang) {
        getLocalizedResString(context, currentLang, R.string.save_settings_btn, strings.saveSettingsBtn)
    }
    val settingsSavedMsg = remember(currentLang) {
        getLocalizedResString(context, currentLang, R.string.settings_saved_toast, strings.settingsSavedToast)
    }

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
                val currentEditedProfile = profile.copy(
                    studioName = studioName.trim().ifBlank { profile.studioName },
                    ownerName = ownerName.trim().ifBlank { profile.ownerName },
                    mobileNumber = mobileNumber.trim().ifBlank { profile.mobileNumber },
                    whatsAppNumber = whatsAppNumber.trim().ifBlank { profile.whatsAppNumber },
                    address = address.trim().ifBlank { profile.address },
                    logoUri = newLogoPath
                )
                viewModel.saveStudioProfile(currentEditedProfile)
            } catch (e: Exception) {
                val fallbackUri = selectedUri.toString()
                logoUri = fallbackUri
                val currentEditedProfile = profile.copy(
                    studioName = studioName.trim().ifBlank { profile.studioName },
                    ownerName = ownerName.trim().ifBlank { profile.ownerName },
                    mobileNumber = mobileNumber.trim().ifBlank { profile.mobileNumber },
                    whatsAppNumber = whatsAppNumber.trim().ifBlank { profile.whatsAppNumber },
                    address = address.trim().ifBlank { profile.address },
                    logoUri = fallbackUri
                )
                viewModel.saveStudioProfile(currentEditedProfile)
            }
        }
    }

    var showRestoreConfirm by remember { mutableStateOf(false) }
    var backupStatusMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUser by viewModel.currentUser.collectAsState()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("settings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = StudioDarkText
                        )
                    }
                },
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
        key(currentLang) {
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
                                            text = studioProfileHeader,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StudioDarkText
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        StudioAppSmallTag()
                                    }
                                    Text(
                                        text = studioLogoSubtitle,
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
                                        text = if (logoUri.isNotBlank()) changeLogoText else setLogoText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (logoUri.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            logoUri = ""
                                            val currentEditedProfile = profile.copy(
                                                studioName = studioName.trim().ifBlank { profile.studioName },
                                                ownerName = ownerName.trim().ifBlank { profile.ownerName },
                                                mobileNumber = mobileNumber.trim().ifBlank { profile.mobileNumber },
                                                whatsAppNumber = whatsAppNumber.trim().ifBlank { profile.whatsAppNumber },
                                                address = address.trim().ifBlank { profile.address },
                                                logoUri = ""
                                            )
                                            viewModel.saveStudioProfile(currentEditedProfile)
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
                                        Text(removeLogoText, fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Studio Name
                            Text(text = studioNameLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            StudioInputField(
                                value = studioName,
                                onValueChange = {
                                    studioName = it
                                    showSaveSuccessBanner = false
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "settings_studio_name_input"
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Owner Name
                            Text(text = ownerNameLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            StudioInputField(
                                value = ownerName,
                                onValueChange = {
                                    ownerName = it
                                    showSaveSuccessBanner = false
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "settings_owner_name_input"
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Mobile Number
                            Text(text = mobileNumberLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            StudioInputField(
                                value = mobileNumber,
                                onValueChange = {
                                    mobileNumber = it
                                    showSaveSuccessBanner = false
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "settings_mobile_input"
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // WhatsApp Number
                            Text(text = whatsAppNumberLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            StudioInputField(
                                value = whatsAppNumber,
                                onValueChange = {
                                    whatsAppNumber = it
                                    showSaveSuccessBanner = false
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "settings_whatsapp_input"
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Full Address
                            Text(text = fullAddressLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            StudioInputField(
                                value = address,
                                onValueChange = {
                                    address = it
                                    showSaveSuccessBanner = false
                                },
                                singleLine = false,
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "settings_address_input"
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Save Settings Button
                            Button(
                                onClick = {
                                    val finalStudioName = studioName.trim().ifBlank { "Maruti Photo Studio" }
                                    val updated = profile.copy(
                                        studioName = finalStudioName,
                                        ownerName = ownerName.trim(),
                                        mobileNumber = mobileNumber.trim(),
                                        whatsAppNumber = whatsAppNumber.trim(),
                                        address = address.trim(),
                                        logoUri = logoUri
                                    )
                                    viewModel.saveStudioProfile(updated)
                                    showSaveSuccessBanner = true
                                    viewModel.showUserMessage(settingsSavedMsg)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(settingsSavedMsg)
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
                                Text(saveSettingsBtnText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            if (showSaveSuccessBanner) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(StudioGreenLight)
                                        .border(1.dp, StudioGreenSuccess.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StudioGreenSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = settingsSavedMsg,
                                        color = StudioGreenDark,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
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
                                    .clickable {
                                        viewModel.setLanguage(lang)
                                        val msg = when (lang) {
                                            AppLanguage.GUJARATI -> "ભાષા બદલાઈ ગઈ: ગુજરાતી"
                                            AppLanguage.HINDI -> "भाषा बदल गई: हिन्दी"
                                            AppLanguage.ENGLISH -> "Language changed to English"
                                        }
                                        viewModel.showUserMessage(msg)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setLanguage(lang)
                                        val msg = when (lang) {
                                            AppLanguage.GUJARATI -> "ભાષા બદલાઈ ગઈ: ગુજરાતી"
                                            AppLanguage.HINDI -> "भाषा बदल गई: हिन्दी"
                                            AppLanguage.ENGLISH -> "Language changed to English"
                                        }
                                        viewModel.showUserMessage(msg)
                                    },
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

            // Section 4: Cloud Account & Firestore Cloud Sync
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cloud_account_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = StudioGreenDark
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ક્લાઉડ એકાઉન્ટ અને સિંક (Cloud Account)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioDarkText
                            )
                        }

                        if (currentUser != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StudioSurfaceVariant)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(StudioRedPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (currentUser?.displayName?.take(1) ?: currentUser?.email?.take(1) ?: "U").uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentUser?.displayName ?: "Studio Owner",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = StudioDarkText
                                    )
                                    Text(
                                        text = currentUser?.email ?: "",
                                        fontSize = 12.sp,
                                        color = StudioSecondaryGray
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(StudioGreenDark)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Firestore સિંક સક્રિય (Cloud Sync Active)",
                                            fontSize = 11.sp,
                                            color = StudioGreenDark,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.syncFromCloud() },
                                    enabled = !isCloudSyncing,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("sync_cloud_now_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StudioGreenDark)
                                ) {
                                    if (isCloudSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("સિંક...", fontSize = 13.sp)
                                    } else {
                                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("હમણાં સિંક કરો", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { viewModel.signOut() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("sign_out_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioRedPrimary)
                                ) {
                                    Text("સાઇન આઉટ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StudioRedPrimary)
                                }
                            }
                        } else {
                            Text(
                                text = "હાલમાં કોઈ એકાઉન્ટ લૉગિન નથી. ડેટા કાયમી સાચવવા લૉગિન કરો.",
                                fontSize = 13.sp,
                                color = StudioSecondaryGray
                            )
                            Button(
                                onClick = { viewModel.navigateTo(Screen.SignIn) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary)
                            ) {
                                Text("Google દ્વારા સાઇન ઇન કરો", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section: WhatsApp Call Sheet Privacy (Customer Phone Protection)
            item {
                val showCustomerPhonePref by viewModel.showCustomerPhoneInWhatsApp.collectAsState()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("whatsapp_privacy_settings_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (showCustomerPhonePref) StudioGreenDark else StudioRedPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "WhatsApp કૉલ શીટ ગોપનીયતા",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioDarkText
                                    )
                                    Text(
                                        text = "મેસેજમાં ગ્રાહકનો મોબાઇલ નંબર બતાવવો કે છુપાવવો",
                                        fontSize = 11.sp,
                                        color = StudioSecondaryGray
                                    )
                                }
                            }
                            Switch(
                                checked = showCustomerPhonePref,
                                onCheckedChange = { viewModel.setShowCustomerPhoneInWhatsApp(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = StudioGreenDark,
                                    checkedTrackColor = StudioGreenLight,
                                    uncheckedThumbColor = StudioRedPrimary,
                                    uncheckedTrackColor = StudioRedLight
                                ),
                                modifier = Modifier.testTag("whatsapp_privacy_phone_switch")
                            )
                        }

                        Text(
                            text = if (showCustomerPhonePref)
                                "✓ અત્યારે મેસેજમાં ગ્રાહકનો મોબાઇલ નંબર દેખાશે. જરૂર મુજબ શેર કરતી વખતે પણ તમે તેને હાઈડ/બ્લોક કરી શકો છો."
                            else
                                "🔒 અત્યારે મેસેજમાં ગ્રાહકનો મોબાઇલ નંબર છુપાવેલ રહેશે ([સુરક્ષા માટે છુપાવેલ / Protected]). ક્રૂ મેમ્બર્સ સીધો કસ્ટમરનો સંપર્ક કરી શકશે નહીં.",
                            fontSize = 12.sp,
                            color = if (showCustomerPhonePref) StudioGreenDark else StudioRedDark,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Section 5: Google AdMob Settings & Live Banner Preview
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admob_settings_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = StudioRedPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = strings.admobSettingsHeader,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioDarkText
                                    )
                                    Text(
                                        text = strings.admobSettingsSub,
                                        fontSize = 11.sp,
                                        color = StudioSecondaryGray
                                    )
                                }
                            }

                            // Active badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StudioGreenLight)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isAdMobBannerEnabled) "Active" else "Disabled",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAdMobBannerEnabled) StudioGreenDark else StudioSecondaryGray
                                )
                            }
                        }

                        // Banner Preview Toggle Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioSurfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.admobBannerToggleLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StudioDarkText
                                )
                                Text(
                                    text = strings.admobTestModeStatus,
                                    fontSize = 11.sp,
                                    color = StudioGreenDark
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isAdMobBannerEnabled,
                                onCheckedChange = { checked ->
                                    viewModel.setAdMobBannerEnabled(checked)
                                    val msg = if (checked) "AdMob બેનર પ્રીવ્યૂ ચાલુ કર્યું" else "AdMob બેનર પ્રીવ્યૂ બંધ કર્યું"
                                    viewModel.showUserMessage(msg)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = StudioRedPrimary,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray
                                ),
                                modifier = Modifier.testTag("admob_banner_toggle_switch")
                            )
                        }

                        // Ad IDs display
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFAFAFA))
                                .border(0.5.dp, StudioLightBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = strings.admobBannerAdUnitLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioDarkText
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ca-app-pub-3940256099942544/6300978111",
                                    fontSize = 10.sp,
                                    color = StudioSecondaryGray
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = strings.admobAppIdLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioDarkText
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ca-app-pub-3940256099942544~3347511713",
                                    fontSize = 10.sp,
                                    color = StudioSecondaryGray
                                )
                            }
                        }

                        // Live AdMob Banner Preview
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = strings.admobLivePreviewLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioDarkText
                            )

                            AdMobBanner(
                                adUnitId = "ca-app-pub-3940256099942544/6300978111",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                            )
                        }

                        // Policy note
                        Text(
                            text = strings.admobPolicyNote,
                            fontSize = 11.sp,
                            color = StudioSecondaryGray,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Section 6: Privacy Policy Card (પ્રાઈવસી પોલિસી)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(Screen.PrivacyPolicy) }
                        .testTag("privacy_policy_card"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(StudioGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = StudioGreenSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.privacyPolicyTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioDarkText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = strings.privacyPolicySub,
                                fontSize = 11.sp,
                                color = StudioSecondaryGray
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Open",
                            tint = StudioSecondaryGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(70.dp))
            }
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

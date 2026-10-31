package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import android.util.Log
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.StudioProfile
import com.example.ui.strings.StringsDefinition
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioDarkText
import com.example.ui.theme.StudioGreenDark
import com.example.ui.theme.StudioGreenLight
import com.example.ui.theme.StudioGreenSuccess
import com.example.ui.theme.StudioLightBorder
import com.example.ui.theme.StudioRedDark
import com.example.ui.theme.StudioRedLight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.ui.theme.StudioRedPrimary
import com.example.ui.theme.StudioSecondaryGray
import com.example.ui.theme.StudioSurfaceVariant

@Composable
fun StudioAppSmallTag(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StudioRedLight)
            .border(0.5.dp, StudioRedPrimary.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "⚡ Studio Management",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = StudioRedPrimary,
            maxLines = 1
        )
    }
}

@Composable
fun StudioLogoBadge(
    modifier: Modifier = Modifier,
    logoUri: String = "",
    size: Int = 42,
    iconSize: Int = 22
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(1.5.dp, StudioRedPrimary.copy(alpha = 0.4f), CircleShape)
            .background(StudioRedLight),
        contentAlignment = Alignment.Center
    ) {
        if (logoUri.isNotBlank()) {
            AsyncImage(
                model = logoUri,
                contentDescription = "Studio Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Studio Logo",
                tint = StudioRedPrimary,
                modifier = Modifier.size(iconSize.dp)
            )
        }
    }
}

@Composable
fun StudioHeader(
    profile: StudioProfile,
    onCallClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("studio_header"),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = StudioBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSettingsClick() }
            ) {
                // Outer Home page studio logo image set by user
                StudioLogoBadge(logoUri = profile.logoUri, size = 46, iconSize = 24)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.studioName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioDarkText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        StudioAppSmallTag()
                    }
                    Text(
                        text = profile.address.ifBlank { "India" },
                        fontSize = 12.sp,
                        color = StudioSecondaryGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onCallClick,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(StudioRedLight)
                        .testTag("header_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call Studio Owner",
                        tint = StudioRedPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(StudioSurfaceVariant)
                        .testTag("header_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = StudioDarkText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StudioBottomBar(
    currentTabIndex: Int,
    strings: StringsDefinition,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.8.dp, color = StudioLightBorder)
            .testTag("bottom_nav_bar")
    ) {
        val items = listOf(
            Triple(strings.navHome, Icons.Default.Home, 0),
            Triple(strings.navEventBooking, Icons.Default.CalendarMonth, 1),
            Triple(strings.navCrew, Icons.Default.Group, 2),
            Triple(strings.navSettings, Icons.Default.Settings, 3)
        )

        items.forEach { (label, icon, index) ->
            val isSelected = currentTabIndex == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = StudioRedPrimary,
                    selectedTextColor = StudioRedPrimary,
                    indicatorColor = StudioRedLight,
                    unselectedIconColor = StudioSecondaryGray,
                    unselectedTextColor = StudioSecondaryGray
                ),
                modifier = Modifier.testTag("nav_tab_$index")
            )
        }
    }
}

@Composable
fun SimpleConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = true
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = StudioDarkText
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                color = StudioDarkText,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) StudioRedPrimary else StudioGreenSuccess,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("dialog_confirm_button")
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("dialog_cancel_button")
            ) {
                Text(cancelText, color = StudioSecondaryGray)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
fun WarningAlertDialog(
    title: String,
    message: String,
    continueText: String,
    cancelText: String,
    onContinue: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = StudioRedPrimary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = StudioDarkText
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                color = StudioDarkText,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("warning_continue_button")
            ) {
                Text(continueText, color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("warning_cancel_button")
            ) {
                Text(cancelText, color = StudioSecondaryGray)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

/**
 * Standard Studio Input Field:
 * - Permanent RED border (#E53935) visible whether focused or unfocused
 * - 2px (2.dp) border width
 * - 7px (7.dp) border radius
 * - Clean white background
 * - Height: 48-52dp for single-line
 * - Comfortable horizontal & vertical padding
 * - Text color: #1F2937 (StudioDarkText)
 * - Muted gray placeholder
 * - No floating labels or underlines
 */
@Composable
fun StudioInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    placeholderContent: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    testTag: String = "",
    minHeight: Dp = 50.dp
) {
    val shape = RoundedCornerShape(7.dp)
    val borderColor = StudioRedPrimary // Always #E53935
    val borderWidth = 2.dp

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = TextStyle(
            color = StudioDarkText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = visualTransformation,
        cursorBrush = SolidColor(StudioRedPrimary),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (singleLine) Modifier.heightIn(min = 48.dp, max = 52.dp)
                        else Modifier.heightIn(min = (minLines * 24 + 24).dp.coerceAtLeast(minHeight))
                    )
                    .clip(shape)
                    .background(Color.White)
                    .border(width = borderWidth, color = borderColor, shape = shape)
                    .padding(horizontal = 14.dp, vertical = if (singleLine) 12.dp else 10.dp),
                contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
                ) {
                    if (leadingIcon != null) {
                        Box(
                            modifier = Modifier.padding(end = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            leadingIcon()
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .then(if (singleLine) Modifier.fillMaxWidth() else Modifier),
                        contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
                    ) {
                        if (value.isEmpty()) {
                            if (placeholderContent != null) {
                                placeholderContent()
                            } else if (placeholder.isNotEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = StudioSecondaryGray,
                                    fontSize = 15.sp,
                                    maxLines = if (singleLine) 1 else maxLines,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        innerTextField()
                    }

                    if (trailingIcon != null) {
                        Box(
                            modifier = Modifier.padding(start = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            trailingIcon()
                        }
                    }
                }
            }
        }
    )
}

/**
 * Standard Studio Clickable Box (for Date/Time/Dropdown pickers):
 * Identical 2px #E53935 red border, 7px radius, white background and 48-52dp height.
 */
@Composable
fun StudioClickableBox(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(7.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp, max = 52.dp)
            .clip(shape)
            .background(Color.White)
            .border(width = 2.dp, color = StudioRedPrimary, shape = shape)
            .clickable(onClick = onClick)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        content()
    }
}

/**
 * Interactive WhatsApp Call Sheet Dialog:
 * Shows a full preview of the formatted call sheet with quick action buttons
 * to send to the Customer, send to Assigned Crew, copy text, or share to other apps.
 */
@Composable
fun EventCallSheetModal(
    callSheetText: String,
    customerMobile: String,
    customerName: String,
    assignedCrew: List<com.example.data.model.CrewMember>,
    onSendWhatsApp: (mobile: String, text: String) -> Unit,
    onShareGeneral: (text: String) -> Unit,
    onDismiss: () -> Unit,
    initialShowCustomerPhone: Boolean = true,
    onSendCrewPersonal: ((crew: com.example.data.model.CrewMember, showPhone: Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    var showCustomerPhone by remember { mutableStateOf(initialShowCustomerPhone) }

    // Dynamically mask or include customer phone based on studio owner's toggle
    val activeCallSheetText = remember(callSheetText, showCustomerPhone, customerMobile) {
        if (showCustomerPhone) {
            callSheetText
        } else {
            val clean = customerMobile.trim()
            if (clean.isNotEmpty()) {
                callSheetText
                    .replace("📞 Customer Phone: $clean", "📞 Customer Phone: [સુરક્ષા માટે છુપાવેલ / Protected]")
                    .replace("📞 Customer Phone: $customerMobile", "📞 Customer Phone: [સુરક્ષા માટે છુપાવેલ / Protected]")
                    .replace("Customer Phone: $clean", "Customer Phone: [સુરક્ષા માટે છુપાવેલ / Protected]")
            } else {
                callSheetText.replace(
                    Regex("📞 Customer Phone:.*"),
                    "📞 Customer Phone: [સુરક્ષા માટે છુપાવેલ / Protected]"
                )
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(StudioGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = StudioGreenDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Event Call Sheet",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioDarkText
                        )
                        Text(
                            text = "WhatsApp ઇવેન્ટ શિડ્યુલ",
                            fontSize = 11.sp,
                            color = StudioSecondaryGray
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Customer Phone Block / Show Toggle Control
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (showCustomerPhone) StudioGreenLight else Color(0xFFFFF3E0)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (showCustomerPhone) StudioGreenDark.copy(alpha = 0.5f) else Color(0xFFFFB74D)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_customer_phone_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (showCustomerPhone) Icons.Default.Phone else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (showCustomerPhone) StudioGreenDark else Color(0xFFE65100),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (showCustomerPhone) "મેસેજમાં ગ્રાહકનો નંબર બતાવો (Show)" else "ગ્રાહકનો નંબર છુપાવો (Block/Protect)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (showCustomerPhone) StudioGreenDark else Color(0xFFBF360C)
                                )
                                Text(
                                    text = if (showCustomerPhone) "મેસેજમાં ગ્રાહકનો મોબાઇલ નંબર દેખાશે" else "ક્રૂ કે અન્યને મોકલતી વખતે નંબર છુપાવેલ રહેશે",
                                    fontSize = 10.sp,
                                    color = StudioSecondaryGray
                                )
                            }
                        }
                        Switch(
                            checked = showCustomerPhone,
                            onCheckedChange = { showCustomerPhone = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StudioGreenDark,
                                checkedTrackColor = StudioGreenLight,
                                uncheckedThumbColor = Color(0xFFE65100),
                                uncheckedTrackColor = Color(0xFFFFE0B2)
                            ),
                            modifier = Modifier.testTag("toggle_customer_phone_switch")
                        )
                    }
                }

                // Call Sheet Preview Card (Real-time Live Preview)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF212529))
                        .padding(12.dp)
                ) {
                    Text(
                        text = activeCallSheetText,
                        fontSize = 11.sp,
                        color = Color(0xFFF8F9FA),
                        lineHeight = 16.sp
                    )
                }

                // Action 1: Send to Customer
                Button(
                    onClick = {
                        onSendWhatsApp(customerMobile, activeCallSheetText)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("send_call_sheet_customer_btn"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioGreenSuccess)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ગ્રાહકને મોકલો ($customerName)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Action 2: Send to Assigned Crew
                if (assignedCrew.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioSurfaceVariant)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "👥 ક્રૂ (ટીમ મેમ્બર્સ) ને મોકલો:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioDarkText
                        )
                        assignedCrew.forEach { crew ->
                            OutlinedButton(
                                onClick = {
                                    if (onSendCrewPersonal != null) {
                                        onSendCrewPersonal(crew, showCustomerPhone)
                                    } else {
                                        onSendWhatsApp(crew.mobile, activeCallSheetText)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioDarkText)
                            ) {
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = StudioGreenDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${crew.name} (${crew.role})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Action 3: Copy & General Share Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Event Call Sheet", activeCallSheetText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "કોલ શીટ કોપી થઈ ગઈ! (Copied)", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("copy_call_sheet_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            onShareGeneral(activeCallSheetText)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("share_call_sheet_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {}
    )
}

/**
 * Google AdMob Banner Component
 * Embeds the real AdMob Banner AdView with smooth test/offline fallback.
 */
@Composable
fun AdMobBanner(
    adUnitId: String = com.example.util.AdMobConfig.DEFAULT_LIVE_BANNER_ID,
    modifier: Modifier = Modifier
) {
    var isAdLoaded by remember(adUnitId) { mutableStateOf(false) }
    var isAdFailed by remember(adUnitId) { mutableStateOf(false) }
    val isEmulator = remember { com.example.util.AdMobManager.isEmulator() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color(0xFFF1F3F4))
            .border(width = 0.8.dp, color = Color(0xFFDADCE0))
            .testTag("admob_banner_container"),
        contentAlignment = Alignment.Center
    ) {
        if (!isAdFailed && !isEmulator) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                factory = { context ->
                    try {
                        com.google.android.gms.ads.AdView(context).apply {
                            setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                            this.adUnitId = adUnitId
                            adListener = object : com.google.android.gms.ads.AdListener() {
                                override fun onAdLoaded() {
                                    super.onAdLoaded()
                                    isAdLoaded = true
                                    isAdFailed = false
                                }

                                override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                                    super.onAdFailedToLoad(loadAdError)
                                    Log.w("AdMobBanner", "Banner failed to load: ${loadAdError.message}")
                                    isAdFailed = true
                                }
                            }
                            loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
                        }
                    } catch (t: Throwable) {
                        Log.e("AdMobBanner", "Error instantiating AdView: ${t.message}")
                        isAdFailed = true
                        android.view.View(context)
                    }
                }
            )
        }

        // When ad is loading or in test/fallback mode, show professional Ad badge and banner information
        if (!isAdLoaded || isAdFailed) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Official Ad badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFF9AB00))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Ad",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Google AdMob",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF202124)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (adUnitId.contains("3940256099942544")) "• Test Mode (320x50)" else "• Live Mode (320x50)",
                                fontSize = 10.sp,
                                color = Color(0xFF5F6368)
                            )
                        }
                        Text(
                            text = "Maruti Photo Studio • Smart Photography Solution",
                            fontSize = 10.sp,
                            color = Color(0xFF5F6368),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Google Ad Choices info badge
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8EAED)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "i",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5F6368)
                    )
                }
            }
        }
    }
}

/**
 * BannerAdView alias
 */
@Composable
fun BannerAdView(
    adUnitId: String = com.example.util.AdMobConfig.DEFAULT_LIVE_BANNER_ID,
    modifier: Modifier = Modifier
) {
    AdMobBanner(adUnitId = adUnitId, modifier = modifier)
}




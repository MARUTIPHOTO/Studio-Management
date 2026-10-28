package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by viewModel.language.collectAsState()
    val strings = remember(currentLang) { com.example.ui.strings.AppStrings.get(currentLang) }

    BackHandler {
        viewModel.navigateBack()
    }

    val fullPolicyText = """
PRIVACY POLICY - STUDIO MANAGEMENT
Published by: Maruti Photo Studio
Contact Email: marutiphoto1234@gmail.com
Effective Date: October 2026

1. INTRODUCTION
Studio Management is an offline-first mobile application designed to help photography studios and photographers manage customer bookings, event timelines, invoices, and crew assignments.

2. LOCAL DATA STORAGE & PRIVACY
- All business records (customer names, phone numbers, event venues, packages, payment advances, balances, crew members) are stored strictly locally on your Android device in an encrypted Room SQLite database.
- We do NOT transmit, upload, or sell your customer or business records to any external cloud server.

3. ADVERTISEMENTS & GOOGLE ADMOB
- Studio Management displays banner and interstitial advertisements provided by Google AdMob.
- Google AdMob and its advertising partners may collect device identifiers (Google Advertising ID), diagnostic metrics, and network information to deliver relevant ads and prevent fraud, in accordance with Google's Privacy Policy: https://policies.google.com/privacy

4. DEVICE PERMISSIONS
- INTERNET & ACCESS_NETWORK_STATE: Required solely to load Google AdMob advertisements.
- POST_NOTIFICATIONS: Used to send timely event schedule reminders on Android 13+.
- VIBRATE: Provides alert feedback for scheduled photography assignments.
- READ_CONTACTS: Used strictly to allow you to pick and import customer or crew phone numbers from your device contact book into the app. We do not sync or upload your contacts.
- Photo Picker: Uses zero-permission Android Photo Picker for selecting studio logos.

5. DATA RETENTION & USER RIGHTS
- You have complete control over all data. You can edit or delete any customer, booking, or crew member at any time.
- Uninstalling the application or clearing app storage will permanently remove all locally stored database records.

6. CONTACT INFORMATION
For any privacy questions or developer inquiries:
Studio: Maruti Photo Studio
Email: marutiphoto1234@gmail.com
Country: India
    """.trimIndent()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.privacyPolicyTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioDarkText
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("privacy_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = StudioDarkText
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Privacy Policy", fullPolicyText))
                            Toast.makeText(context, "Privacy Policy copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("copy_policy_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Policy",
                            tint = StudioRedPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = StudioBackground,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Summary Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(StudioGreenLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = StudioGreenSuccess,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Studio Management Privacy Commitment",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioDarkText
                                )
                                Text(
                                    text = "Maruti Photo Studio • India",
                                    fontSize = 12.sp,
                                    color = StudioSecondaryGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "✓ 100% Offline-First Architecture\n✓ Your Client & Event Data Remains Strictly on Your Device\n✓ We Never Sell, Share, or Upload Your Data\n✓ Google Play Policy Compliant",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = StudioGreenDark,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Section 1: Local Data Storage
            item {
                PolicySectionCard(
                    icon = Icons.Default.Storage,
                    iconTint = StudioRedPrimary,
                    iconBg = StudioRedLight,
                    title = "1. Local Data Storage (સ્થાનિક સ્ટોરેજ)",
                    content = "તમારા તમામ ગ્રાહકોના નામ, મોબાઈલ નંબર, ઇવેન્ટની તારીખો, સ્થળ, ફોટોગ્રાફી પેકેજ અને પેમેન્ટ હિસાબ ફક્ત તમારા મોબાઈલમાં જ સ્થાનિક ડેટાબેઝ (Android Room SQLite) માં સચવાય છે. અમે તમારો બિઝનેસ ડેટા અમારા કોઈપણ સર્વર પર મોકલતા કે સ્ટોર કરતા નથી."
                )
            }

            // Section 2: Advertisements & AdMob
            item {
                PolicySectionCard(
                    icon = Icons.Default.Public,
                    iconTint = Color(0xFF1976D2),
                    iconBg = Color(0xFFE3F2FD),
                    title = "2. Advertisements (Google AdMob)",
                    content = "એપ્લિકેશન ફ્રી સેવાઓ આપવા માટે Google AdMob બેનર અને ઇન્ટરસ્ટિશિયલ જાહેરાતોનો ઉપયોગ કરે છે. Google AdMob યુઝર એક્સપિરિયન્સ અને એડ પોલિસી મુજબ એડવર્ટાઇઝિંગ આઈડી (AAID) અને ડિવાઇસ ડાયગ્નોસ્ટિક્સનો ઉપયોગ કરી શકે છે. વધુ માહિતી માટે Google ની સત્તાવાર પ્રાઈવસી પોલિસી (policies.google.com/privacy) માન્ય રહેશે."
                )
            }

            // Section 3: Permissions
            item {
                PolicySectionCard(
                    icon = Icons.Default.Lock,
                    iconTint = Color(0xFFF57C00),
                    iconBg = Color(0xFFFFF3E0),
                    title = "3. Device Permissions (પરમિશન્સ)",
                    content = "• INTERNET & NETWORK STATE: ફક્ત Google AdMob જાહેરાતો લોડ કરવા માટે.\n• READ_CONTACTS: ગ્રાહક કે ક્રૂ મેમ્બરનું નામ અને મોબાઈલ નંબર તમારા ફોનના કોન્ટેક્ટ્સમાંથી સીધા પસંદ કરવા માટે.\n• NOTIFICATIONS: આગામી ફોટોશૂટ અને ઇવેન્ટ્સના સમયસર રિમાઇન્ડર મોકલવા માટે.\n• PHOTO PICKER: સ્ટુડિયો લોગો પસંદ કરવા માટે (કોઈ broad storage access વગર)."
                )
            }

            // Section 4: Data Retention & User Rights
            item {
                PolicySectionCard(
                    icon = Icons.Default.Security,
                    iconTint = Color(0xFF7B1FA2),
                    iconBg = Color(0xFFF3E5F5),
                    title = "4. Your Rights & Data Deletion (ડેટા ડિલીટ)",
                    content = "તમે કોઈપણ સમયે ગ્રાહક કે ઇવેન્ટ રેકોર્ડ જાતે ડિલીટ કરી શકો છો. જો તમે એપ્લિકેશન અનઇન્સ્ટોલ કરશો અથવા ફોનના સેટિંગ્સમાંથી 'Clear Storage' કરશો, તો તમારો તમામ ડેટા મોબાઈલમાંથી કાયમ માટે ડિલીટ થઈ જશે."
                )
            }

            // Section 5: Contact Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(StudioRedLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = StudioRedPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "5. Contact & Support",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioDarkText
                                )
                                Text(
                                    text = "કોઈપણ પ્રશ્ન હોય તો અમારો સંપર્ક કરો",
                                    fontSize = 12.sp,
                                    color = StudioSecondaryGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "• App Name: Studio Management\n• Developer / Studio: Maruti Photo Studio\n• Official Email: marutiphoto1234@gmail.com\n• Country: India",
                            fontSize = 13.sp,
                            color = StudioDarkText,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Email Support Button
                            Button(
                                onClick = {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:marutiphoto1234@gmail.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "Privacy Inquiry - Studio Management")
                                    }
                                    try {
                                        context.startActivity(emailIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Email: marutiphoto1234@gmail.com", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("email_support_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Email Us", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            // Copy Policy Button
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Privacy Policy", fullPolicyText))
                                    Toast.makeText(context, "Full Privacy Policy copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("copy_policy_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioDarkText)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Policy", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

@Composable
private fun PolicySectionCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioDarkText
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = content,
                fontSize = 13.sp,
                color = StudioDarkText,
                lineHeight = 19.sp
            )
        }
    }
}

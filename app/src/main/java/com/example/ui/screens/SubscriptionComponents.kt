package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriptionStatus
import com.example.data.model.UserSubscriptionState
import com.example.ui.strings.StringsDefinition
import com.example.ui.theme.*

/**
 * High-converting, attractive launch banner for HomeScreen.
 * Shows the Early Bird 200 Offer (Refer 2 Friends = 1 Year Free).
 */
@Composable
fun EarlyBirdReferralBannerCard(
    state: UserSubscriptionState,
    strings: StringsDefinition,
    onInviteClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.status == SubscriptionStatus.EARLY_BIRD_REFERRAL_ACTIVE) {
        // Unlocked 1-Year Free VIP Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, StudioGreenDark),
            modifier = modifier
                .fillMaxWidth()
                .clickable { onDetailsClick() }
                .testTag("early_bird_active_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(StudioGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StudioGreenDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.premiumUnlockedBadge,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioGreenDark
                    )
                    Text(
                        text = "WhatsApp શેરિંગ અનલિમિટેડ ચાલુ છે (${state.planExpiryDateString} સુધી)",
                        fontSize = 11.sp,
                        color = StudioSecondaryGray
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = StudioGreenDark
                )
            }
        }
    } else {
        // High-converting Golden Early Bird Launch Card (0/2 or 1/2 friends)
        val goldGradient = Brush.horizontalGradient(
            colors = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(goldGradient)
                .clickable { onDetailsClick() }
                .testTag("early_bird_referral_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Top Tag: Early Bird 200 Studios
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF59E0B))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = strings.earlyBirdBadge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFEF08A))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = String.format(strings.slotsRemainingLabel, state.earlyBirdSlotsRemaining),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF854D0E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Headline
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎁",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.referralCardTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF78350F)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = strings.referralCardSub,
                    fontSize = 12.sp,
                    color = Color(0xFF92400E),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar & Counter
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(strings.friendsJoinedLabel, state.referralCount),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78350F)
                        )
                        Text(
                            text = if (state.referralCount == 1) "હજુ ૧ બાકી" else "૨ મિત્રો જરૂરી",
                            fontSize = 10.sp,
                            color = StudioSecondaryGray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { state.referralCount / 2f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFF59E0B),
                        trackColor = Color(0xFFE5E7EB)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onInviteClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("invite_whatsapp_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.inviteWhatsAppBtn,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onDetailsClick,
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("referral_details_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF78350F)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
                    ) {
                        Text(
                            text = "પ્લાન / કોડ ⚡",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Gorgeous, high-converting Paywall / Subscription Dialog
 * Shows both the Early Bird Referral option and Direct Subscription Plans (₹99 & ₹599).
 */
@Composable
fun SubscriptionPaywallDialog(
    state: UserSubscriptionState,
    strings: StringsDefinition,
    onDismiss: () -> Unit,
    onInviteWhatsApp: () -> Unit,
    onApplyReferralCode: (String) -> Unit,
    onActivatePlan: (String) -> Unit
) {
    val context = LocalContext.current
    var friendCodeInput by remember { mutableStateOf("") }
    var showCodeInputRow by remember { mutableStateOf(false) }

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
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.unlockCallSheetTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioDarkText
                        )
                        Text(
                            text = "WhatsApp કૉલ શીટ અને પાસ શેરિંગ",
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = strings.unlockCallSheetSub,
                    fontSize = 12.sp,
                    color = StudioSecondaryGray,
                    lineHeight = 16.sp
                )

                // OPTION 1: 🏆 EARLY BIRD FREE REFERRAL CARD
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏆 લૉન્ચ ઑફર (પહેલા ૨૦૦ યુઝર્સ)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                            Text(
                                text = String.format(strings.slotsRemainingLabel, state.earlyBirdSlotsRemaining),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF854D0E)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = strings.referralCardTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF78350F)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // User's own code box with copy button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFFCD34D), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "તમારો રેફરલ કોડ:",
                                    fontSize = 10.sp,
                                    color = StudioSecondaryGray
                                )
                                Text(
                                    text = state.myReferralCode,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Referral Code", state.myReferralCode))
                                    Toast.makeText(context, "કોડ કોપી થયો: ${state.myReferralCode}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Share on WhatsApp button
                        Button(
                            onClick = onInviteWhatsApp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.inviteWhatsAppBtn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Toggle row to enter friend's code
                        if (!showCodeInputRow) {
                            TextButton(
                                onClick = { showCodeInputRow = true },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = "+ " + strings.enterFriendCodeBtn,
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = friendCodeInput,
                                    onValueChange = { friendCodeInput = it.uppercase() },
                                    placeholder = { Text("દા.ત. STUDIO12", fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Button(
                                    onClick = {
                                        if (friendCodeInput.isNotBlank()) {
                                            onApplyReferralCode(friendCodeInput)
                                            friendCodeInput = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                                ) {
                                    Text("Apply", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // OR DIVIDER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = StudioLightBorder)
                    Text(
                        text = " અથવા ડાયરેક્ટ સબસ્ક્રાઇબ કરો ",
                        fontSize = 10.sp,
                        color = StudioSecondaryGray,
                        fontWeight = FontWeight.Medium
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = StudioLightBorder)
                }

                // OPTION 2: DIRECT SUBSCRIPTION PLANS (Monthly ₹99 & Yearly ₹599)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Monthly Pass
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFE5E7EB)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Monthly",
                                fontSize = 11.sp,
                                color = StudioSecondaryGray,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.planMonthlyLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioDarkText
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onActivatePlan("monthly") },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = strings.btnGetMonthly,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Yearly Pass (Best Value)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, StudioGreenDark),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StudioGreenDark)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "૫૦% છૂટ 👑",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.planYearlyLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioGreenDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onActivatePlan("yearly") },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StudioGreenSuccess),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = strings.btnGetYearly,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

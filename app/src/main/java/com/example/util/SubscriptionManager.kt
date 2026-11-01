package com.example.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.example.data.model.SubscriptionStatus
import com.example.data.model.UserSubscriptionState
import com.example.ui.strings.AppLanguage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class SubscriptionManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("studio_subscription_prefs", Context.MODE_PRIVATE)

    private val _subscriptionState = MutableStateFlow(loadSubscriptionState())
    val subscriptionState: StateFlow<UserSubscriptionState> = _subscriptionState.asStateFlow()

    init {
        // Ensure initial referral code and trial start time exist
        ensureInitialized()
        refreshState()
        syncWithCloud()
    }

    private fun ensureInitialized() {
        var changed = false
        val editor = prefs.edit()

        if (!prefs.contains(KEY_REFERRAL_CODE)) {
            val randomNum = Random.nextInt(1000, 9999)
            val generatedCode = "STUDIO$randomNum"
            editor.putString(KEY_REFERRAL_CODE, generatedCode)
            changed = true
        }

        if (!prefs.contains(KEY_TRIAL_START_TIME)) {
            editor.putLong(KEY_TRIAL_START_TIME, System.currentTimeMillis())
            changed = true
        }

        if (!prefs.contains(KEY_SLOTS_REMAINING)) {
            editor.putInt(KEY_SLOTS_REMAINING, 158) // 158 out of 200 early-bird slots remaining
            changed = true
        }

        if (changed) {
            editor.apply()
        }
    }

    fun refreshState() {
        _subscriptionState.value = loadSubscriptionState()
    }

    private fun loadSubscriptionState(): UserSubscriptionState {
        val now = System.currentTimeMillis()
        val referralCode = prefs.getString(KEY_REFERRAL_CODE, "STUDIO77") ?: "STUDIO77"
        val referralCount = prefs.getInt(KEY_REFERRAL_COUNT, 0)
        val earlyBirdUnlocked = prefs.getBoolean(KEY_EARLY_BIRD_UNLOCKED, false)
        val earlyBirdExpiry = prefs.getLong(KEY_EARLY_BIRD_EXPIRY, 0L)

        val paidActive = prefs.getBoolean(KEY_PAID_ACTIVE, false)
        val paidExpiry = prefs.getLong(KEY_PAID_EXPIRY, 0L)

        val trialStartTime = prefs.getLong(KEY_TRIAL_START_TIME, now)
        val slotsRemaining = prefs.getInt(KEY_SLOTS_REMAINING, 158)

        val trialDuration = 15L * 24 * 60 * 60 * 1000L
        val trialElapsed = now - trialStartTime
        val trialDaysRemaining = maxOf(0, (15 - (trialElapsed / (24 * 60 * 60 * 1000L))).toInt())

        val status: SubscriptionStatus
        val isUnlocked: Boolean
        val expiryString: String
        val planName: String

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        if (earlyBirdUnlocked && now < earlyBirdExpiry) {
            status = SubscriptionStatus.EARLY_BIRD_REFERRAL_ACTIVE
            isUnlocked = true
            expiryString = dateFormat.format(Date(earlyBirdExpiry))
            planName = "1 Year Free VIP (Early Bird)"
        } else if (paidActive && now < paidExpiry) {
            status = SubscriptionStatus.PAID_ACTIVE
            isUnlocked = true
            expiryString = dateFormat.format(Date(paidExpiry))
            planName = prefs.getString(KEY_PAID_PLAN_NAME, "Premium VIP") ?: "Premium VIP"
        } else if (trialDaysRemaining > 0) {
            status = SubscriptionStatus.TRIAL_ACTIVE
            isUnlocked = true
            expiryString = "$trialDaysRemaining Days Left"
            planName = "15-Day Free Trial"
        } else {
            status = SubscriptionStatus.EXPIRED
            isUnlocked = false
            expiryString = "Expired"
            planName = "Trial Ended - Upgrade"
        }

        return UserSubscriptionState(
            status = status,
            myReferralCode = referralCode,
            referralCount = referralCount,
            maxReferralsNeeded = 2,
            earlyBirdSlotsRemaining = slotsRemaining,
            trialDaysRemaining = trialDaysRemaining,
            isWhatsAppSharingUnlocked = isUnlocked,
            planExpiryDateString = expiryString,
            activePlanName = planName
        )
    }

    /**
     * Invite friends on WhatsApp with prefilled message containing the user's Referral Code.
     */
    fun shareInviteOnWhatsApp(language: AppLanguage) {
        val code = _subscriptionState.value.myReferralCode
        val inviteText = when (language) {
            AppLanguage.GUJARATI -> """
                🙏 નમસ્તે ફોટોગ્રાફર મિત્ર!
                
                મેં મારા ફોટો સ્ટુડિયો માટે *Studio Management* એપ વાપરી, આમાં ઇવેન્ટ બુકિંગ, ક્રૂ પાસ અને કૉલ શીટ WhatsApp પર બહુ સરસ બને છે.
                
                🎁 *ખાસ લૉન્ચ ઑફર:*
                મારા આ રેફરલ કોડથી લૉગિન કરો: *${code}*
                અને તમે પણ ૨૦૦ સ્ટુડિયો વાળી ૧ વર્ષ ફ્રી ઑફરનો લાભ લો!
                
                📲 ડાઉનલોડ લિંક:
                https://play.google.com/store/apps/details?id=com.studiomanagement.app
            """.trimIndent()

            AppLanguage.HINDI -> """
                🙏 नमस्ते फोटोग्राफर साथी!
                
                मैंने अपने फोटो स्टूडियो के लिए *Studio Management* ऐप उपयोग किया। इसमें इवेंट बुकिंग और WhatsApp पर क्रू कॉल शीट भेजना बहुत आसान है।
                
                🎁 *स्पेशल लॉन्च ऑफर:*
                मेरे रेफरल कोड का उपयोग करें: *${code}*
                और पहले २०० स्टूडियो के लिए १ साल फ्री का लाभ उठाएं!
                
                📲 डाउनलोड लिंक:
                https://play.google.com/store/apps/details?id=com.studiomanagement.app
            """.trimIndent()

            AppLanguage.ENGLISH -> """
                Hello Photographer Friend! 📸
                
                Manage your studio events, crew schedules, and WhatsApp call sheets effortlessly with *Studio Management* app.
                
                🎁 *Special Launch Offer:*
                Use my referral code: *${code}*
                and get 1 Year Free access in the first 200 early-bird studios!
                
                📲 Download on Google Play:
                https://play.google.com/store/apps/details?id=com.studiomanagement.app
            """.trimIndent()
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("whatsapp://send?text=" + Uri.encode(inviteText))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to standard share sheet
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, inviteText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Referral Code").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    /**
     * Apply a friend's referral code or count a successful referral.
     */
    fun applyReferral(enteredCode: String, onComplete: (success: Boolean, message: String) -> Unit) {
        val trimmed = enteredCode.trim().uppercase()
        val currentCode = _subscriptionState.value.myReferralCode

        if (trimmed.isBlank()) {
            onComplete(false, "કૃપા કરીને સાચો રેફરલ કોડ નાખો")
            return
        }

        if (trimmed == currentCode) {
            onComplete(false, "તમે તમારો પોતાનો કોડ વાપરી શકતા નથી")
            return
        }

        // Increment referral count
        val newCount = minOf(2, prefs.getInt(KEY_REFERRAL_COUNT, 0) + 1)
        val editor = prefs.edit().putInt(KEY_REFERRAL_COUNT, newCount)

        if (newCount >= 2) {
            // Unlock 1 Full Year Free! (365 days)
            val oneYearMillis = 365L * 24 * 60 * 60 * 1000L
            val expiry = System.currentTimeMillis() + oneYearMillis
            editor.putBoolean(KEY_EARLY_BIRD_UNLOCKED, true)
            editor.putLong(KEY_EARLY_BIRD_EXPIRY, expiry)

            // Decrement remaining slots count
            val remaining = maxOf(0, prefs.getInt(KEY_SLOTS_REMAINING, 158) - 1)
            editor.putInt(KEY_SLOTS_REMAINING, remaining)
            editor.apply()

            refreshState()
            syncWithCloud()
            onComplete(true, "અભિનંદન! ૨ મિત્રો જોડાતા તમારું ૧ વર્ષનું ફ્રી સબસ્ક્રિપ્શન અનલૉક થઈ ગયું છે! 🎉")
        } else {
            editor.apply()
            refreshState()
            syncWithCloud()
            onComplete(true, "૧ મિત્ર જોડાઈ ગયો! ૧ વર્ષ ફ્રી મેળવવા હજુ ૧ મિત્રને જોડો.")
        }
    }

    /**
     * Activate a paid subscription (Monthly ₹99 or Yearly ₹599).
     */
    fun activatePaidPlan(plan: String, onComplete: () -> Unit) {
        val now = System.currentTimeMillis()
        val durationMillis = if (plan == "yearly") {
            365L * 24 * 60 * 60 * 1000L
        } else {
            30L * 24 * 60 * 60 * 1000L
        }
        val planTitle = if (plan == "yearly") "1 Year VIP Pass (₹599)" else "Monthly Pass (₹99)"

        prefs.edit()
            .putBoolean(KEY_PAID_ACTIVE, true)
            .putLong(KEY_PAID_EXPIRY, now + durationMillis)
            .putString(KEY_PAID_PLAN_NAME, planTitle)
            .apply()

        refreshState()
        syncWithCloud()
        onComplete()
    }

    /**
     * Sync referral and subscription state with Firebase Firestore under /users/{uid}
     */
    fun syncWithCloud() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val state = _subscriptionState.value
                val data = hashMapOf<String, Any>(
                    "referralCode" to state.myReferralCode,
                    "referralCount" to state.referralCount,
                    "isUnlocked" to state.isWhatsAppSharingUnlocked,
                    "subscriptionStatus" to state.status.name,
                    "activePlan" to state.activePlanName,
                    "expiryDate" to state.planExpiryDateString,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .set(data, SetOptions.merge())
            } catch (e: Exception) {
                Log.w("SubscriptionManager", "Cloud sync skipped: ${e.message}")
            }
        }
    }

    companion object {
        private const val KEY_REFERRAL_CODE = "key_referral_code"
        private const val KEY_REFERRAL_COUNT = "key_referral_count"
        private const val KEY_EARLY_BIRD_UNLOCKED = "key_early_bird_unlocked"
        private const val KEY_EARLY_BIRD_EXPIRY = "key_early_bird_expiry"
        private const val KEY_PAID_ACTIVE = "key_paid_active"
        private const val KEY_PAID_EXPIRY = "key_paid_expiry"
        private const val KEY_PAID_PLAN_NAME = "key_paid_plan_name"
        private const val KEY_TRIAL_START_TIME = "key_trial_start_time"
        private const val KEY_SLOTS_REMAINING = "key_slots_remaining"

        @Volatile
        private var instance: SubscriptionManager? = null

        fun getInstance(context: Context): SubscriptionManager {
            return instance ?: synchronized(this) {
                instance ?: SubscriptionManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

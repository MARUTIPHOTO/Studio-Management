package com.example.data.model

enum class SubscriptionStatus {
    EARLY_BIRD_REFERRAL_ACTIVE, // Unlocked via referring 2 friends (1 Year Free)
    TRIAL_ACTIVE,               // In 15 days trial
    PAID_ACTIVE,                // Active Monthly or Yearly paid plan
    EXPIRED                     // Needs upgrade/referral
}

data class UserSubscriptionState(
    val status: SubscriptionStatus = SubscriptionStatus.TRIAL_ACTIVE,
    val myReferralCode: String = "",
    val referralCount: Int = 0,
    val maxReferralsNeeded: Int = 2,
    val earlyBirdSlotsRemaining: Int = 158,
    val trialDaysRemaining: Int = 15,
    val isWhatsAppSharingUnlocked: Boolean = true,
    val planExpiryDateString: String = "",
    val activePlanName: String = "Early Bird 200 Offer"
)

package com.example.util

/**
 * AdMob Configuration for Maruti Photo Studio.
 * Contains both Test IDs and User's Production Live IDs.
 *
 * User Live Keys:
 * - Banner: ca-app-pub-3231614216500760/5305629076
 * - Interstitial: ca-app-pub-3231614216500760/6297031135
 * - Publisher ID: ca-app-pub-3231614216500760
 */
object AdMobConfig {
    const val PUBLISHER_ID = "ca-app-pub-3231614216500760"

    // Official Google AdMob Test Ad Unit IDs (Google Sample IDs)
    // Used when Test Mode is enabled to prevent AdMob policy violations during testing/development
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"

    // Production Live Ad Unit IDs provided by user for Play Store deployment
    const val DEFAULT_LIVE_BANNER_ID = "ca-app-pub-3231614216500760/5305629076"
    const val DEFAULT_LIVE_INTERSTITIAL_ID = "ca-app-pub-3231614216500760/6297031135"

    fun getEffectiveBannerId(isTestMode: Boolean, configuredId: String? = null): String {
        return if (isTestMode) {
            TEST_BANNER_ID
        } else {
            configuredId?.takeIf { it.isNotBlank() } ?: DEFAULT_LIVE_BANNER_ID
        }
    }

    fun getEffectiveInterstitialId(isTestMode: Boolean, configuredId: String? = null): String {
        return if (isTestMode) {
            TEST_INTERSTITIAL_ID
        } else {
            configuredId?.takeIf { it.isNotBlank() } ?: DEFAULT_LIVE_INTERSTITIAL_ID
        }
    }
}

package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdMobManager {
    private const val TAG = "AdMobManager"
    private var isInitialized = false
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "MobileAds initialized: $status")
                isInitialized = true
            }
        } catch (t: Throwable) {
            Log.e(TAG, "MobileAds initialize error: ${t.message}")
        }
    }

    fun loadInterstitialAd(
        context: Context,
        adUnitId: String,
        onLoaded: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) {
        if (isInterstitialLoading) return
        isInterstitialLoading = true
        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isInterstitialLoading = false
                        Log.d(TAG, "Interstitial loaded for unit: $adUnitId")
                        onLoaded?.invoke()
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        isInterstitialLoading = false
                        Log.w(TAG, "Interstitial failed to load: ${loadAdError.message}")
                        onFailed?.invoke(loadAdError.message)
                    }
                }
            )
        } catch (t: Throwable) {
            isInterstitialLoading = false
            Log.e(TAG, "loadInterstitialAd exception: ${t.message}")
            onFailed?.invoke(t.message ?: "Unknown error")
        }
    }

    fun showInterstitial(
        activity: Activity,
        adUnitId: String,
        onDismissed: () -> Unit = {}
    ) {
        try {
            val currentAd = interstitialAd
            if (currentAd != null) {
                currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        // Preload next ad
                        loadInterstitialAd(activity.applicationContext, adUnitId)
                        onDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        interstitialAd = null
                        Log.w(TAG, "Ad failed to show: ${adError.message}")
                        onDismissed()
                    }
                }
                currentAd.show(activity)
            } else {
                // If not ready yet, load in background
                loadInterstitialAd(activity.applicationContext, adUnitId)
                onDismissed()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "showInterstitial exception: ${t.message}")
            onDismissed()
        }
    }

    fun isInterstitialReady(): Boolean = interstitialAd != null
}

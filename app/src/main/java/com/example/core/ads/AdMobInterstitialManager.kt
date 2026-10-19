package com.example.core.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdMobInterstitialManager(
  private val context: Context,
  val adUnitId: String = AD_UNIT_ID
) {
  companion object {
    const val TAG = "AdMobInterstitial"
    const val AD_UNIT_ID = "ca-app-pub-5106987776038143/1803650344"
    // AdMob Policy compliance: Minimum 45 seconds between interstitial ads to protect user experience
    private const val MIN_INTERVAL_MS = 45_000L
  }

  private var interstitialAd: InterstitialAd? = null
  private var isLoading: Boolean = false
  private var lastShowTimeMs: Long = 0L

  init {
    preloadAd()
  }

  fun preloadAd() {
    if (interstitialAd != null || isLoading) return
    isLoading = true

    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(
      context,
      adUnitId,
      adRequest,
      object : InterstitialAdLoadCallback() {
        override fun onAdLoaded(ad: InterstitialAd) {
          Log.d(TAG, "Interstitial ad successfully loaded")
          interstitialAd = ad
          isLoading = false
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
          Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message} (code: ${loadAdError.code})")
          interstitialAd = null
          isLoading = false
        }
      }
    )
  }

  /**
   * Shows the interstitial ad if available and policy cooldown has elapsed.
   * If not available or cooldown hasn't passed, immediately invokes [onComplete]
   * so the user flow is never blocked.
   */
  fun showInterstitialIfReady(activity: Activity?, onComplete: () -> Unit) {
    val ad = interstitialAd
    val now = SystemClock.elapsedRealtime()
    val canShow = ad != null && activity != null && (now - lastShowTimeMs >= MIN_INTERVAL_MS)

    if (!canShow || ad == null || activity == null) {
      // Preload next if not ready
      if (interstitialAd == null) {
        preloadAd()
      }
      onComplete()
      return
    }

    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
      override fun onAdDismissedFullScreenContent() {
        Log.d(TAG, "Interstitial ad dismissed by user")
        interstitialAd = null
        lastShowTimeMs = SystemClock.elapsedRealtime()
        preloadAd()
        onComplete()
      }

      override fun onAdFailedToShowFullScreenContent(adError: AdError) {
        Log.w(TAG, "Interstitial ad failed to show: ${adError.message}")
        interstitialAd = null
        preloadAd()
        onComplete()
      }

      override fun onAdShowedFullScreenContent() {
        Log.d(TAG, "Interstitial ad displayed on screen")
        interstitialAd = null
      }
    }

    ad.show(activity)
  }
}

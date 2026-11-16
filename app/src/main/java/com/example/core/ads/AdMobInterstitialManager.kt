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
    // AdMob Policy compliance: Generous 90-second cooldown between interstitial ads
    private const val MIN_INTERVAL_MS = 90_000L
  }

  private var interstitialAd: InterstitialAd? = null
  private var isLoading: Boolean = false
  private var lastShowTimeMs: Long = 0L
  private var completedRoundsInSession: Int = 0

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
   * Shows the interstitial ad at natural round completion boundaries when cooldown has elapsed,
   * skipping the very first completed round of a session for smooth onboarding/first-play UX.
   */
  fun showInterstitialIfReady(activity: Activity?, onComplete: () -> Unit) {
    completedRoundsInSession++
    val ad = interstitialAd
    val now = SystemClock.elapsedRealtime()
    val cooldownPassed = lastShowTimeMs == 0L || (now - lastShowTimeMs >= MIN_INTERVAL_MS)
    val canShow = completedRoundsInSession >= 2 && ad != null && activity != null && cooldownPassed

    if (!canShow || ad == null || activity == null) {
      if (interstitialAd == null) {
        preloadAd()
      }
      onComplete()
      return
    }

    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
      override fun onAdDismissedFullScreenContent() {
        interstitialAd = null
        lastShowTimeMs = SystemClock.elapsedRealtime()
        preloadAd()
        onComplete()
      }

      override fun onAdFailedToShowFullScreenContent(adError: AdError) {
        interstitialAd = null
        preloadAd()
        onComplete()
      }

      override fun onAdShowedFullScreenContent() {
        interstitialAd = null
      }
    }

    ad.show(activity)
  }
}

package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

object AdMobConfig {
  const val APP_ID = "ca-app-pub-5106987776038143~9402104617"
  const val BANNER_AD_UNIT_ID = "ca-app-pub-5106987776038143/1391154977"
  const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-5106987776038143/1803650344"
}

@Composable
fun AdMobBannerBar(
  modifier: Modifier = Modifier,
  adUnitId: String = AdMobConfig.BANNER_AD_UNIT_ID
) {
  val lifecycleOwner = LocalLifecycleOwner.current
  var currentAdView by remember { mutableStateOf<AdView?>(null) }
  var isAdLoaded by remember { mutableStateOf(false) }

  DisposableEffect(lifecycleOwner, currentAdView) {
    val ad = currentAdView
    if (ad == null) {
      onDispose {}
    } else {
      val observer = LifecycleEventObserver { _, event ->
        when (event) {
          Lifecycle.Event.ON_PAUSE -> ad.pause()
          Lifecycle.Event.ON_RESUME -> ad.resume()
          Lifecycle.Event.ON_DESTROY -> ad.destroy()
          else -> Unit
        }
      }
      val lifecycle = lifecycleOwner.lifecycle
      lifecycle.addObserver(observer)
      onDispose {
        lifecycle.removeObserver(observer)
        ad.destroy()
      }
    }
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .testTag("admob_banner_bar"),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 50.dp)
        .padding(vertical = 4.dp),
      contentAlignment = Alignment.Center
    ) {
      AndroidView(
        factory = { ctx ->
          AdView(ctx).apply {
            setAdSize(AdSize.BANNER)
            this.adUnitId = adUnitId
            adListener = object : AdListener() {
              override fun onAdLoaded() {
                super.onAdLoaded()
                isAdLoaded = true
              }

              override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                isAdLoaded = false
              }
            }
            loadAd(AdRequest.Builder().build())
            currentAdView = this
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.Center)
      )
    }
  }
}

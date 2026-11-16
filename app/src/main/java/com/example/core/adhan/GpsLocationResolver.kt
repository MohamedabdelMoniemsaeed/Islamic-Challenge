package com.example.core.adhan

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.location.LocationCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

sealed class GpsLocationResult {
  data class Success(val cityLocation: CityLocation) : GpsLocationResult()
  data object PermissionDenied : GpsLocationResult()
  data object GpsDisabled : GpsLocationResult()
  data object Unavailable : GpsLocationResult()
}

object GpsLocationResolver {

  private const val MAX_CACHE_AGE_MS = 30L * 60L * 1000L
  private const val FIX_TIMEOUT_MS = 6_500L

  suspend fun detectCurrentLocation(context: Context): GpsLocationResult {
    val fineGranted = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!fineGranted && !coarseGranted) {
      return GpsLocationResult.PermissionDenied
    }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
      ?: return GpsLocationResult.Unavailable

    if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
      return GpsLocationResult.GpsDisabled
    }

    val bestRecent = getBestRecentLastKnownLocation(locationManager)
    val location = if (bestRecent != null) {
      bestRecent
    } else {
      requestFreshLocation(context, locationManager)
        ?: getBestAnyLastKnownLocation(locationManager)
    } ?: return GpsLocationResult.Unavailable

    val (localityAr, localityEn) = withContext(Dispatchers.IO) {
      resolveLocalityNames(context, location.latitude, location.longitude)
    }

    val cityLocation = PrayerTimesManager.createGpsCityLocation(
      latitude = location.latitude,
      longitude = location.longitude,
      localityAr = localityAr,
      localityEn = localityEn,
      timeZoneId = TimeZone.getDefault().id
    )
    return GpsLocationResult.Success(cityLocation)
  }

  @SuppressLint("MissingPermission")
  private fun getBestRecentLastKnownLocation(locationManager: LocationManager): Location? {
    val now = System.currentTimeMillis()
    return getBestAnyLastKnownLocation(locationManager)?.takeIf { loc ->
      now - loc.time <= MAX_CACHE_AGE_MS
    }
  }

  @SuppressLint("MissingPermission")
  private fun getBestAnyLastKnownLocation(locationManager: LocationManager): Location? {
    val providers = listOf(
      LocationManager.GPS_PROVIDER,
      LocationManager.NETWORK_PROVIDER,
      LocationManager.PASSIVE_PROVIDER
    )
    val candidates = providers.mapNotNull { provider ->
      try {
        if (locationManager.isProviderEnabled(provider)) {
          locationManager.getLastKnownLocation(provider)
        } else {
          null
        }
      } catch (_: Throwable) {
        null
      }
    }
    return candidates.maxWithOrNull(
      compareBy<Location> { it.time }.thenBy { -it.accuracy }
    )
  }

  @SuppressLint("MissingPermission")
  private suspend fun requestFreshLocation(
    context: Context,
    locationManager: LocationManager
  ): Location? {
    val provider = when {
      safeIsProviderEnabled(locationManager, LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
      safeIsProviderEnabled(locationManager, LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
      else -> return null
    }

    return withTimeoutOrNull(FIX_TIMEOUT_MS) {
      suspendCancellableCoroutine { cont ->
        val cancellationSignal = CancellationSignal()
        try {
          LocationManagerCompat.getCurrentLocation(
            locationManager,
            provider,
            cancellationSignal,
            ContextCompat.getMainExecutor(context)
          ) { location ->
            if (cont.isActive) {
              cont.resume(location)
            }
          }
          cont.invokeOnCancellation {
            cancellationSignal.cancel()
          }
        } catch (_: Throwable) {
          // Fallback to single listener update if getCurrentLocation isn't supported
          try {
            val listener = object : LocationListenerCompat {
              override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)
                if (cont.isActive) cont.resume(location)
              }
            }
            @Suppress("DEPRECATION")
            locationManager.requestSingleUpdate(provider, listener, context.mainLooper)
            cont.invokeOnCancellation {
              try {
                locationManager.removeUpdates(listener)
              } catch (_: Throwable) {}
            }
          } catch (_: Throwable) {
            if (cont.isActive) cont.resume(null)
          }
        }
      }
    }
  }

  private fun safeIsProviderEnabled(locationManager: LocationManager, provider: String): Boolean {
    return try {
      locationManager.isProviderEnabled(provider)
    } catch (_: Throwable) {
      false
    }
  }

  @Suppress("DEPRECATION")
  private fun resolveLocalityNames(
    context: Context,
    latitude: Double,
    longitude: Double
  ): Pair<String?, String?> {
    if (!Geocoder.isPresent()) return null to null

    fun fetchForLocale(locale: Locale): String? {
      return try {
        val geocoder = Geocoder(context, locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          var name: String? = null
          val latch = java.util.concurrent.CountDownLatch(1)
          geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
            val addr = addresses.firstOrNull()
            name = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: addr?.countryName
            latch.countDown()
          }
          latch.await(2, java.util.concurrent.TimeUnit.SECONDS)
          name
        } else {
          val addresses = geocoder.getFromLocation(latitude, longitude, 1)
          val addr = addresses?.firstOrNull()
          addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: addr?.countryName
        }
      } catch (_: Throwable) {
        null
      }
    }

    val arName = fetchForLocale(Locale("ar"))
    val enName = fetchForLocale(Locale.ENGLISH)
    return arName to enName
  }
}

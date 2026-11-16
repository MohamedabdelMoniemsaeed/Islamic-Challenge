package com.example.core.permissions

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

enum class ConnectionType {
  WIFI,
  CELLULAR,
  OTHER,
  OFFLINE
}

data class DeviceAccessStatus(
  val hasLocationPermission: Boolean = false,
  val isPreciseLocationGranted: Boolean = false,
  val isDeviceGpsEnabled: Boolean = false,
  val isInternetConnected: Boolean = false,
  val connectionType: ConnectionType = ConnectionType.OFFLINE,
  val hasNotificationPermission: Boolean = false,
  val canScheduleExactAlarms: Boolean = true,
  val isIgnoringBatteryOptimizations: Boolean = true
) {
  val isGpsReady: Boolean
    get() = hasLocationPermission && isDeviceGpsEnabled

  fun internetLabel(isAr: Boolean): String = when (connectionType) {
    ConnectionType.WIFI -> if (isAr) "متصل (Wi-Fi)" else "Connected (Wi-Fi)"
    ConnectionType.CELLULAR -> if (isAr) "متصل (بيانات الهاتف)" else "Connected (Mobile Data)"
    ConnectionType.OTHER -> if (isAr) "متصل بالإنترنت" else "Connected"
    ConnectionType.OFFLINE -> if (isAr) "غير متصل بالإنترنت" else "Offline"
  }
}

object DeviceAccessManager {

  fun getStatus(context: Context): DeviceAccessStatus {
    val fineGranted = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val hasLocation = fineGranted || coarseGranted

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    val gpsEnabled = try {
      locationManager != null && LocationManagerCompat.isLocationEnabled(locationManager)
    } catch (_: Throwable) {
      false
    }

    val connType = getCurrentConnectionType(context)
    val internetConnected = connType != ConnectionType.OFFLINE

    val hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED &&
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    } else {
      NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    val canExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      try {
        alarmManager?.canScheduleExactAlarms() ?: false
      } catch (_: Throwable) {
        false
      }
    } else {
      true
    }

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val ignoringBattery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      try {
        powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
      } catch (_: Throwable) {
        true
      }
    } else {
      true
    }

    return DeviceAccessStatus(
      hasLocationPermission = hasLocation,
      isPreciseLocationGranted = fineGranted,
      isDeviceGpsEnabled = gpsEnabled,
      isInternetConnected = internetConnected,
      connectionType = connType,
      hasNotificationPermission = hasNotifications,
      canScheduleExactAlarms = canExactAlarms,
      isIgnoringBatteryOptimizations = ignoringBattery
    )
  }

  fun getCurrentConnectionType(context: Context): ConnectionType {
    return try {
      val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return ConnectionType.OFFLINE
      val network = cm.activeNetwork ?: return ConnectionType.OFFLINE
      val caps = cm.getNetworkCapabilities(network) ?: return ConnectionType.OFFLINE
      val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      if (!hasInternet) return ConnectionType.OFFLINE
      when {
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
        else -> ConnectionType.OTHER
      }
    } catch (_: Throwable) {
      ConnectionType.OFFLINE
    }
  }

  fun observeConnectivity(context: Context): Flow<ConnectionType> = callbackFlow {
    trySend(getCurrentConnectionType(context))
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    if (cm == null) {
      close()
      return@callbackFlow
    }

    val callback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        trySend(getCurrentConnectionType(context))
      }

      override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        trySend(getCurrentConnectionType(context))
      }

      override fun onLost(network: Network) {
        trySend(getCurrentConnectionType(context))
      }
    }

    val registered = try {
      val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
      cm.registerNetworkCallback(request, callback)
      true
    } catch (_: Throwable) {
      false
    }

    awaitClose {
      if (registered) {
        try {
          cm.unregisterNetworkCallback(callback)
        } catch (_: Throwable) {}
      }
    }
  }

  fun openLocationSourceSettings(context: Context) {
    val launched = launchIntent(context, Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    if (!launched) {
      openAppDetailsSettings(context)
    }
  }

  fun openInternetSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      if (launchIntent(context, Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))) {
        return
      }
    }
    if (!launchIntent(context, Intent(Settings.ACTION_WIFI_SETTINGS))) {
      launchIntent(context, Intent(Settings.ACTION_WIRELESS_SETTINGS))
    }
  }

  fun openAppNotificationSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
      }
      if (launchIntent(context, intent)) return
    }
    openAppDetailsSettings(context)
  }

  fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.parse("package:${context.packageName}")
      }
      if (launchIntent(context, intent)) return
    }
    openAppDetailsSettings(context)
  }

  fun openBatteryOptimizationSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (launchIntent(context, Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))) {
        return
      }
    }
    openAppDetailsSettings(context)
  }

  fun openAppDetailsSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
      data = Uri.fromParts("package", context.packageName, null)
    }
    launchIntent(context, intent)
  }

  private fun launchIntent(context: Context, intent: Intent): Boolean {
    return try {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(intent)
      true
    } catch (_: Throwable) {
      false
    }
  }
}

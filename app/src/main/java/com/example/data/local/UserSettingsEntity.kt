package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.localization.AppLanguage

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
  @PrimaryKey val id: Int = 1,
  val soundEnabled: Boolean = true,
  val musicEnabled: Boolean = false,
  val vibrationEnabled: Boolean = true,
  val soundVolume: Float = 0.8f,
  val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
  val languageCode: String = "ARABIC" // "ARABIC", "ENGLISH"
) {
  fun toLanguage(): AppLanguage {
    return if (languageCode.equals("ENGLISH", ignoreCase = true)) {
      AppLanguage.ENGLISH
    } else {
      AppLanguage.ARABIC
    }
  }

  fun toDarkModeBoolean(): Boolean? {
    return when (themeMode.uppercase()) {
      "DARK" -> true
      "LIGHT" -> false
      else -> null // System default
    }
  }

  companion object {
    fun fromPreferences(
      sound: Boolean,
      music: Boolean,
      vibration: Boolean,
      volume: Float,
      darkMode: Boolean?,
      language: AppLanguage
    ): UserSettingsEntity {
      val themeStr = when (darkMode) {
        true -> "DARK"
        false -> "LIGHT"
        null -> "SYSTEM"
      }
      return UserSettingsEntity(
        id = 1,
        soundEnabled = sound,
        musicEnabled = music,
        vibrationEnabled = vibration,
        soundVolume = volume,
        themeMode = themeStr,
        languageCode = language.name
      )
    }
  }
}

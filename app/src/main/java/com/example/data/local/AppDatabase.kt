package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL(
      "ALTER TABLE `player_profile` ADD COLUMN `lastDailyChallengeEpochDay` INTEGER NOT NULL DEFAULT 0"
    )
  }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS `user_settings` (
        `id` INTEGER NOT NULL,
        `soundEnabled` INTEGER NOT NULL DEFAULT 1,
        `musicEnabled` INTEGER NOT NULL DEFAULT 0,
        `vibrationEnabled` INTEGER NOT NULL DEFAULT 1,
        `soundVolume` REAL NOT NULL DEFAULT 0.8,
        `themeMode` TEXT NOT NULL DEFAULT 'SYSTEM',
        `languageCode` TEXT NOT NULL DEFAULT 'ARABIC',
        PRIMARY KEY(`id`)
      )
      """.trimIndent()
    )
  }
}

@Database(
  entities = [
    PlayerEntity::class,
    CategoryProgressEntity::class,
    AchievementEntity::class,
    UserSettingsEntity::class
  ],
  version = 3,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun playerDao(): PlayerDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "islamic_challenge.db"
        )
          .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}


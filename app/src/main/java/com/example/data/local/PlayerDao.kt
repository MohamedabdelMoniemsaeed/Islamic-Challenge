package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
  @Query("SELECT * FROM player_profile WHERE id = 1")
  fun getPlayerProfile(): Flow<PlayerEntity?>

  @Query("SELECT * FROM player_profile WHERE id = 1")
  suspend fun getPlayerProfileSync(): PlayerEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdatePlayer(player: PlayerEntity)

  @Query("SELECT * FROM category_progress")
  fun getAllCategoryProgress(): Flow<List<CategoryProgressEntity>>

  @Query("SELECT * FROM category_progress WHERE categoryId = :catId")
  suspend fun getCategoryProgress(catId: String): CategoryProgressEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveCategoryProgress(progress: CategoryProgressEntity)

  @Query("SELECT * FROM unlocked_achievements")
  fun getUnlockedAchievements(): Flow<List<AchievementEntity>>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun unlockAchievement(achievement: AchievementEntity)

  @Transaction
  suspend fun recordGameResultAtomic(
    player: PlayerEntity,
    categoryProgress: CategoryProgressEntity?,
    achievementsToUnlock: List<AchievementEntity>
  ) {
    insertOrUpdatePlayer(player)
    categoryProgress?.let { saveCategoryProgress(it) }
    achievementsToUnlock.forEach { unlockAchievement(it) }
  }

  @Query("DELETE FROM player_profile")
  suspend fun clearPlayer()

  @Query("DELETE FROM category_progress")
  suspend fun clearCategoryProgress()

  @Query("DELETE FROM unlocked_achievements")
  suspend fun clearAchievements()

  @Query("SELECT * FROM user_settings WHERE id = 1")
  fun getUserSettings(): Flow<UserSettingsEntity?>

  @Query("SELECT * FROM user_settings WHERE id = 1")
  suspend fun getUserSettingsSync(): UserSettingsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveUserSettings(settings: UserSettingsEntity)

  @Transaction
  suspend fun resetAllData(defaultPlayer: PlayerEntity) {
    clearPlayer()
    clearCategoryProgress()
    clearAchievements()
    insertOrUpdatePlayer(defaultPlayer)
  }
}

package com.tayseer.app.data.dao

import androidx.room.*
import com.tayseer.app.data.entity.AppSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingValue(key: String): String?

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    fun observeSettingValue(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSetting)

    @Query("SELECT * FROM app_settings")
    suspend fun getAllSettings(): List<AppSetting>
}

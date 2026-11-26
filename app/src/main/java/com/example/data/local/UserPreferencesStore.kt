package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.LessonCatalog
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "hablaya_preferences"
)

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val selectedVoiceId: String = LessonCatalog.voiceProfiles.first().id,
    val hasCompletedVoiceComparison: Boolean = false,
    val biometricLockEnabled: Boolean = false,
    val remindersEnabled: Boolean = true,
    val reminderHour: Int = 19,
    val remoteSyncConfigured: Boolean = false,
    val totalXp: Int = 0,
    val currentStreakDays: Int = 0,
    val lastPlayEpochDay: Long = -1L,
    val blitzBestScore: Int = 0,
    val unlockedPhraseIds: Set<String> = emptySet()
)

class UserPreferencesStore(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SELECTED_VOICE_ID = stringPreferencesKey("selected_voice_id")
        val VOICE_COMPARED = booleanPreferencesKey("voice_compared")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMOTE_SYNC_CONFIGURED = booleanPreferencesKey("remote_sync_configured")
        val TOTAL_XP = intPreferencesKey("total_xp")
        val STREAK_DAYS = intPreferencesKey("streak_days")
        val LAST_PLAY_EPOCH_DAY = longPreferencesKey("last_play_epoch_day")
        val BLITZ_BEST_SCORE = intPreferencesKey("blitz_best_score")
        val UNLOCKED_PHRASE_IDS = stringSetPreferencesKey("unlocked_phrase_ids")
    }

    val preferencesFlow: Flow<UserPreferences> = context.userPrefsDataStore.data.map { prefs ->
        UserPreferences(
            themeMode = ThemeMode.fromKey(prefs[Keys.THEME_MODE]),
            selectedVoiceId = prefs[Keys.SELECTED_VOICE_ID] ?: LessonCatalog.voiceProfiles.first().id,
            hasCompletedVoiceComparison = prefs[Keys.VOICE_COMPARED] ?: false,
            biometricLockEnabled = prefs[Keys.BIOMETRIC_LOCK] ?: false,
            remindersEnabled = prefs[Keys.REMINDERS_ENABLED] ?: true,
            reminderHour = prefs[Keys.REMINDER_HOUR] ?: 19,
            remoteSyncConfigured = prefs[Keys.REMOTE_SYNC_CONFIGURED] ?: false,
            totalXp = prefs[Keys.TOTAL_XP] ?: 0,
            currentStreakDays = prefs[Keys.STREAK_DAYS] ?: 0,
            lastPlayEpochDay = prefs[Keys.LAST_PLAY_EPOCH_DAY] ?: -1L,
            blitzBestScore = prefs[Keys.BLITZ_BEST_SCORE] ?: 0,
            unlockedPhraseIds = prefs[Keys.UNLOCKED_PHRASE_IDS] ?: emptySet()
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.userPrefsDataStore.edit { it[Keys.THEME_MODE] = mode.storageKey }
    }

    suspend fun setSelectedVoice(voiceId: String, fromComparison: Boolean = false) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.SELECTED_VOICE_ID] = voiceId
            if (fromComparison) {
                prefs[Keys.VOICE_COMPARED] = true
            }
        }
    }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.BIOMETRIC_LOCK] = enabled }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.REMINDERS_ENABLED] = enabled }
    }

    suspend fun setReminderHour(hour: Int) {
        context.userPrefsDataStore.edit { it[Keys.REMINDER_HOUR] = hour.coerceIn(7, 22) }
    }

    suspend fun recordGameRewards(
        xpEarned: Int,
        todayEpochDay: Long,
        unlockedCardId: String? = null,
        blitzScore: Int? = null
    ) {
        context.userPrefsDataStore.edit { prefs ->
            val prevXp = prefs[Keys.TOTAL_XP] ?: 0
            prefs[Keys.TOTAL_XP] = prevXp + xpEarned.coerceAtLeast(0)

            val prevDay = prefs[Keys.LAST_PLAY_EPOCH_DAY] ?: -1L
            val prevStreak = prefs[Keys.STREAK_DAYS] ?: 0
            val newStreak = when {
                prevDay == todayEpochDay -> prevStreak.coerceAtLeast(1)
                prevDay == todayEpochDay - 1L -> prevStreak + 1
                else -> 1
            }
            prefs[Keys.STREAK_DAYS] = newStreak
            prefs[Keys.LAST_PLAY_EPOCH_DAY] = todayEpochDay

            if (unlockedCardId != null) {
                val currentCards = prefs[Keys.UNLOCKED_PHRASE_IDS] ?: emptySet()
                prefs[Keys.UNLOCKED_PHRASE_IDS] = currentCards + unlockedCardId
            }

            if (blitzScore != null) {
                val prevBest = prefs[Keys.BLITZ_BEST_SCORE] ?: 0
                if (blitzScore > prevBest) {
                    prefs[Keys.BLITZ_BEST_SCORE] = blitzScore
                }
            }
        }
    }

    suspend fun resetGameStats() {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.TOTAL_XP] = 0
            prefs[Keys.STREAK_DAYS] = 0
            prefs[Keys.LAST_PLAY_EPOCH_DAY] = -1L
            prefs[Keys.BLITZ_BEST_SCORE] = 0
            prefs[Keys.UNLOCKED_PHRASE_IDS] = emptySet()
        }
    }
}

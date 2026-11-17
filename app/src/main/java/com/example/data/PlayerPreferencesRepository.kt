package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.TableTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.hokmDataStore: DataStore<Preferences> by preferencesDataStore(name = "hokm_prefs")

data class PlayerProfile(
    val playerId: String,
    val playerName: String,
    val preferredTargetHands: Int,
    val hapticsEnabled: Boolean,
    val tableThemeId: String,
    val matchesPlayed: Int,
    val matchesWon: Int,
    val kotsScored: Int
)

class PlayerPreferencesRepository(private val context: Context) {

    private object Keys {
        val PLAYER_ID = stringPreferencesKey("player_id")
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val TARGET_HANDS = intPreferencesKey("target_hands")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val TABLE_THEME_ID = stringPreferencesKey("table_theme_id")
        val MATCHES_PLAYED = intPreferencesKey("matches_played")
        val MATCHES_WON = intPreferencesKey("matches_won")
        val KOTS_SCORED = intPreferencesKey("kots_scored")
    }

    val profileFlow: Flow<PlayerProfile> = context.hokmDataStore.data.map { prefs ->
        PlayerProfile(
            playerId = prefs[Keys.PLAYER_ID] ?: UUID.randomUUID().toString(),
            playerName = prefs[Keys.PLAYER_NAME] ?: "بازیکن حکم",
            preferredTargetHands = prefs[Keys.TARGET_HANDS] ?: 7,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            tableThemeId = prefs[Keys.TABLE_THEME_ID] ?: TableTheme.ROYAL_EMERALD.id,
            matchesPlayed = prefs[Keys.MATCHES_PLAYED] ?: 0,
            matchesWon = prefs[Keys.MATCHES_WON] ?: 0,
            kotsScored = prefs[Keys.KOTS_SCORED] ?: 0
        )
    }

    suspend fun ensurePlayerId(): String {
        var resolvedId = ""
        context.hokmDataStore.edit { prefs ->
            val existing = prefs[Keys.PLAYER_ID]
            if (existing != null) {
                resolvedId = existing
            } else {
                resolvedId = UUID.randomUUID().toString()
                prefs[Keys.PLAYER_ID] = resolvedId
            }
        }
        return resolvedId
    }

    suspend fun updatePlayerName(name: String) {
        val sanitized = name.trim().take(18).ifBlank { "بازیکن حکم" }
        context.hokmDataStore.edit { prefs ->
            prefs[Keys.PLAYER_NAME] = sanitized
        }
    }

    suspend fun updatePreferredTargetHands(target: Int) {
        context.hokmDataStore.edit { prefs ->
            prefs[Keys.TARGET_HANDS] = target.coerceIn(1, 13)
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.hokmDataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun updateTableTheme(themeId: String) {
        context.hokmDataStore.edit { prefs ->
            prefs[Keys.TABLE_THEME_ID] = themeId
        }
    }

    suspend fun recordMatchResult(didWin: Boolean, kotsInMatch: Int) {
        context.hokmDataStore.edit { prefs ->
            prefs[Keys.MATCHES_PLAYED] = (prefs[Keys.MATCHES_PLAYED] ?: 0) + 1
            if (didWin) {
                prefs[Keys.MATCHES_WON] = (prefs[Keys.MATCHES_WON] ?: 0) + 1
            }
            prefs[Keys.KOTS_SCORED] = (prefs[Keys.KOTS_SCORED] ?: 0) + kotsInMatch
        }
    }
}

package app.enjoylearning504.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** App-level flags and cached values. */
class AppPreferences(private val store: DataStore<Preferences>) {

    val authPromptShown: Flow<Boolean> = store.data.map { it[AUTH_PROMPT_SHOWN] ?: false }
    val privacyNoticeShown: Flow<Boolean> = store.data.map { it[PRIVACY_NOTICE_SHOWN] ?: false }
    val legacyMigrationDone: Flow<Boolean> = store.data.map { it[LEGACY_MIGRATION_DONE] ?: false }
    val adTurn: Flow<Int?> = store.data.map { it[AD_TURN] }

    suspend fun setAuthPromptShown() = store.edit { it[AUTH_PROMPT_SHOWN] = true }
    suspend fun setPrivacyNoticeShown() = store.edit { it[PRIVACY_NOTICE_SHOWN] = true }
    suspend fun setLegacyMigrationDone() = store.edit { it[LEGACY_MIGRATION_DONE] = true }
    suspend fun setAdTurn(turn: Int) = store.edit { it[AD_TURN] = turn }

    private companion object {
        val AUTH_PROMPT_SHOWN = booleanPreferencesKey("auth_prompt_shown")
        val PRIVACY_NOTICE_SHOWN = booleanPreferencesKey("privacy_notice_shown")
        val LEGACY_MIGRATION_DONE = booleanPreferencesKey("legacy_migration_done")
        val AD_TURN = intPreferencesKey("ad_turn")
    }
}

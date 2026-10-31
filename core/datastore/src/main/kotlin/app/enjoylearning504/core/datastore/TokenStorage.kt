package app.enjoylearning504.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class StoredTokens(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: Long,
    val isGuest: Boolean,
)

/** Auth tokens, encrypted with a Keystore key before they reach disk. */
class TokenStorage internal constructor(
    private val store: DataStore<Preferences>,
    private val cipher: KeystoreCipher,
) {
    constructor(store: DataStore<Preferences>) : this(store, KeystoreCipher())

    val tokens: Flow<StoredTokens?> = store.data.map { prefs ->
        val access = prefs[ACCESS]?.let(cipher::decrypt) ?: return@map null
        val refresh = prefs[REFRESH]?.let(cipher::decrypt) ?: return@map null
        StoredTokens(
            userId = prefs[USER_ID] ?: return@map null,
            accessToken = access,
            refreshToken = refresh,
            expiresAtEpochSeconds = prefs[EXPIRES_AT] ?: 0,
            isGuest = prefs[IS_GUEST] ?: true,
        )
    }

    suspend fun current(): StoredTokens? = tokens.first()

    suspend fun save(tokens: StoredTokens) {
        store.edit {
            it[USER_ID] = tokens.userId
            it[ACCESS] = cipher.encrypt(tokens.accessToken)
            it[REFRESH] = cipher.encrypt(tokens.refreshToken)
            it[EXPIRES_AT] = tokens.expiresAtEpochSeconds
            it[IS_GUEST] = tokens.isGuest
        }
    }

    suspend fun clear() {
        store.edit { it.clear() }
    }

    private companion object {
        val USER_ID = stringPreferencesKey("user_id")
        val ACCESS = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
        val EXPIRES_AT = longPreferencesKey("expires_at")
        val IS_GUEST = booleanPreferencesKey("is_guest")
    }
}

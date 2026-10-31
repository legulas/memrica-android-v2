package app.enjoylearning504.core.data.repository

import app.enjoylearning504.core.datastore.AppPreferences
import app.enjoylearning504.core.domain.ads.AdTurnPolicy
import app.enjoylearning504.core.domain.repository.AppStateRepository
import app.enjoylearning504.core.domain.repository.RemoteConfigRepository
import app.enjoylearning504.core.network.ConfigApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesAppStateRepository(private val prefs: AppPreferences) : AppStateRepository {
    override val authPromptShown: Flow<Boolean> = prefs.authPromptShown
    override val privacyNoticeShown: Flow<Boolean> = prefs.privacyNoticeShown
    override suspend fun markAuthPromptShown() { prefs.setAuthPromptShown() }
    override suspend fun markPrivacyNoticeShown() { prefs.setPrivacyNoticeShown() }
}

class DefaultRemoteConfigRepository(
    private val api: ConfigApi,
    private val prefs: AppPreferences,
) : RemoteConfigRepository {
    override val adTurn: Flow<Int> = prefs.adTurn.map(AdTurnPolicy::sanitize)

    override suspend fun refresh() {
        // Like the original: anything unreadable or out of range means the default turn
        prefs.setAdTurn(AdTurnPolicy.sanitize(api.fetch()?.ttest))
    }
}

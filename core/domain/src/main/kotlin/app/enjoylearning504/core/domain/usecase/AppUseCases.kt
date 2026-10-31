package app.enjoylearning504.core.domain.usecase

import app.enjoylearning504.core.domain.ads.AdTurnPolicy
import app.enjoylearning504.core.domain.repository.AppStateRepository
import app.enjoylearning504.core.domain.repository.AuthRepository
import app.enjoylearning504.core.domain.repository.LegacyDataMigrator
import app.enjoylearning504.core.domain.repository.LegacyMigrationResult
import app.enjoylearning504.core.domain.repository.RemoteConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class StartDestination { AuthPrompt, Home }

/**
 * Everything that must happen before the first screen: move the original app's data across,
 * then decide whether the one-time sign-up/sign-in prompt is due. Network work is started in
 * [backgroundScope] and never delays the UI.
 */
class PrepareAppStartUseCase(
    private val migrator: LegacyDataMigrator,
    private val appState: AppStateRepository,
    private val remoteConfig: RemoteConfigRepository,
    private val auth: AuthRepository,
    private val backgroundScope: CoroutineScope,
) {
    suspend operator fun invoke(): Pair<StartDestination, LegacyMigrationResult> {
        val migration = migrator.migrateIfNeeded()
        backgroundScope.launch { runCatching { remoteConfig.refresh() } }
        backgroundScope.launch { auth.ensureGuestSession() }
        val destination = if (appState.authPromptShown.first()) StartDestination.Home else StartDestination.AuthPrompt
        return destination to migration
    }
}

class CompleteAuthPromptUseCase(private val appState: AppStateRepository) {
    suspend operator fun invoke() = appState.markAuthPromptShown()
}

class ShouldShowInterstitialUseCase(
    private val remoteConfig: RemoteConfigRepository,
    private val policy: AdTurnPolicy,
) {
    suspend operator fun invoke(): Boolean = policy.nextTurnShowsAd(remoteConfig.adTurn.first())
}

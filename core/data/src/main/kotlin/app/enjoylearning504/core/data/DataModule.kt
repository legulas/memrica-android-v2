package app.enjoylearning504.core.data

import app.enjoylearning504.core.data.legacy.LegacyDatabaseMigrator
import app.enjoylearning504.core.data.repository.DefaultAuthRepository
import app.enjoylearning504.core.data.repository.DefaultRemoteConfigRepository
import app.enjoylearning504.core.data.repository.OfflineContentRepository
import app.enjoylearning504.core.data.repository.OfflineProgressRepository
import app.enjoylearning504.core.data.repository.PreferencesAppStateRepository
import app.enjoylearning504.core.data.repository.StoredTokenProvider
import app.enjoylearning504.core.database.databaseModule
import app.enjoylearning504.core.datastore.dataStoreModule
import app.enjoylearning504.core.domain.platform.ConnectivityObserver
import app.enjoylearning504.core.domain.repository.AppStateRepository
import app.enjoylearning504.core.domain.repository.AuthRepository
import app.enjoylearning504.core.domain.repository.ContentRepository
import app.enjoylearning504.core.domain.repository.LegacyDataMigrator
import app.enjoylearning504.core.domain.repository.ProgressRepository
import app.enjoylearning504.core.domain.repository.RemoteConfigRepository
import app.enjoylearning504.core.network.TokenProvider
import app.enjoylearning504.core.network.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    includes(databaseModule, dataStoreModule, networkModule)

    single<ContentRepository> { OfflineContentRepository(get()) }
    single<ProgressRepository> { OfflineProgressRepository(get()) }
    single<AppStateRepository> { PreferencesAppStateRepository(get()) }
    single<RemoteConfigRepository> { DefaultRemoteConfigRepository(get(), get()) }
    single<TokenProvider> { StoredTokenProvider(get()) }
    single<AuthRepository> { DefaultAuthRepository(get(), get()) }
    single<LegacyDataMigrator> { LegacyDatabaseMigrator(androidContext(), get(), get()) }
    single<ConnectivityObserver> { AndroidConnectivityObserver(androidContext()) }
}

package app.enjoylearning504.core.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val Context.appPreferencesStore by preferencesDataStore(name = "app_preferences")
private val Context.tokenStore by preferencesDataStore(name = "auth_tokens")

val dataStoreModule = module {
    single { AppPreferences(androidContext().appPreferencesStore) }
    single { TokenStorage(androidContext().tokenStore) }
}

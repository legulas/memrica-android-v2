package app.enjoylearning504

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.enjoylearning504.core.designsystem.theme.MemricaTheme
import app.enjoylearning504.navigation.MemricaNavHost
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // AppCompat can only apply a language once the activity's delegate exists
        applyDefaultLocaleOnce()
        enableEdgeToEdge()
        splash.setKeepOnScreenCondition { viewModel.startDestination.value == null }
        setContent {
            MemricaTheme {
                val start by viewModel.startDestination.collectAsStateWithLifecycle()
                start?.let { MemricaNavHost(it) }
            }
        }
    }

    /**
     * The app starts in Persian (right to left). This runs only on the very first launch, so a
     * language the user picks later in the system's per-app language settings is respected.
     */
    private fun applyDefaultLocaleOnce() {
        val prefs = getSharedPreferences(LOCALE_PREFS, MODE_PRIVATE)
        if (prefs.getBoolean(KEY_DEFAULT_APPLIED, false)) return
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(DEFAULT_LANGUAGE))
        }
        prefs.edit { putBoolean(KEY_DEFAULT_APPLIED, true) }
    }

    private companion object {
        const val LOCALE_PREFS = "locale"
        const val KEY_DEFAULT_APPLIED = "default_locale_applied"
        const val DEFAULT_LANGUAGE = "fa-IR"
    }
}

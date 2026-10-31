package app.enjoylearning504.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import java.text.NumberFormat
import java.util.Locale

/** The locale the UI is drawn in (the per-app language when one is set). */
@Composable
@ReadOnlyComposable
fun currentLocale(): Locale =
    ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: Locale.getDefault()

/** Formats a whole number with the locale's own digits (Persian digits in Persian). */
@Composable
@ReadOnlyComposable
fun localizedNumber(value: Int): String = NumberFormat.getIntegerInstance(currentLocale()).format(value)

/** "۴۲٪" in Persian, "42%" in English. Percentages are shown truncated, like the original app. */
@Composable
@ReadOnlyComposable
fun localizedPercent(percent: Double): String =
    NumberFormat.getPercentInstance(currentLocale()).apply { maximumFractionDigits = 0 }
        .format(percent.toInt() / 100.0)

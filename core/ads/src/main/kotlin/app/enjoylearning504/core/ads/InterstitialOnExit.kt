package app.enjoylearning504.core.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.koin.compose.koinInject

/**
 * For screens that end with an interstitial (learning and quizzes): preloads the ad when
 * [enabled], and returns an exit function that shows it, if it loaded, before running the exit.
 */
@Composable
fun rememberInterstitialExit(
    placement: AdPlacement,
    enabled: Boolean,
    controller: AdsController = koinInject(),
): (onExit: () -> Unit) -> Unit {
    val activity = LocalContext.current.activity()
    LaunchedEffect(enabled, activity) {
        if (enabled && activity != null) controller.preloadInterstitial(activity, placement)
    }
    return remember(enabled, activity, controller) {
        { onExit ->
            if (enabled && activity != null) controller.showInterstitialIfReady(activity, placement, onExit) else onExit()
        }
    }
}

private tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}

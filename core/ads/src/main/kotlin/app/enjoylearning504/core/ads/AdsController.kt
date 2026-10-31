package app.enjoylearning504.core.ads

import android.app.Activity
import android.content.Context

interface AdsController {
    fun initialize(context: Context)

    /** Starts loading an interstitial so it is ready when the screen closes. */
    fun preloadInterstitial(activity: Activity, placement: AdPlacement)

    /** Shows the preloaded interstitial if there is one; [onDone] runs when it closes or if none was ready. */
    fun showInterstitialIfReady(activity: Activity, placement: AdPlacement, onDone: () -> Unit)
}

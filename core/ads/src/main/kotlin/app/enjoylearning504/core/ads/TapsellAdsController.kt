package app.enjoylearning504.core.ads

import android.app.Activity
import android.content.Context
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.show.AdShowCompletionState

/** Tapsell mediation. The app key comes from the manifest placeholder TapsellMediationAppKey. */
class TapsellAdsController : AdsController {
    private val readyAds = mutableMapOf<AdPlacement, String>()

    override fun initialize(context: Context) {
        Tapsell.initialize(context.applicationContext)
    }

    override fun preloadInterstitial(activity: Activity, placement: AdPlacement) {
        if (readyAds.containsKey(placement)) return
        Tapsell.requestInterstitialAd(placement.zoneId, activity, object : RequestResultListener {
            override fun onSuccess(adId: String) {
                readyAds[placement] = adId
            }

            override fun onFailure(message: String) = Unit
        })
    }

    override fun showInterstitialIfReady(activity: Activity, placement: AdPlacement, onDone: () -> Unit) {
        val adId = readyAds.remove(placement) ?: return onDone()
        Tapsell.showInterstitialAd(adId, activity, object : AdStateListener.Interstitial {
            override fun onAdImpression() = Unit
            override fun onAdClicked() = Unit
            override fun onAdClosed(completionState: AdShowCompletionState) = onDone()
            override fun onAdFailed(message: String) = onDone()
        })
    }
}

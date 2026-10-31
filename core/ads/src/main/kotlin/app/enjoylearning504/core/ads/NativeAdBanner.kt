package app.enjoylearning504.core.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.views.ntv.NativeAdView
import ir.tapsell.mediation.ad.views.ntv.NativeAdViewContainer

/** A native ad row. Takes no space until an ad has loaded, like the original screens. */
@Composable
fun NativeAdBanner(placement: AdPlacement, modifier: Modifier = Modifier) {
    val activity = LocalContext.current.findActivity() ?: return
    var adId by remember(placement) { mutableStateOf<String?>(null) }

    LaunchedEffect(placement) {
        Tapsell.requestNativeAd(placement.zoneId, activity, object : RequestResultListener {
            override fun onSuccess(id: String) {
                adId = id
            }

            override fun onFailure(message: String) = Unit
        })
    }

    val loaded = adId ?: return
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val container = LayoutInflater.from(context)
                .inflate(R.layout.native_ad, null, false) as NativeAdViewContainer
            val view = NativeAdView.Builder(container)
                .withLogo(container.findViewById<ImageView>(R.id.native_ad_logo))
                .withTitle(container.findViewById<TextView>(R.id.native_ad_title))
                .withDescription(container.findViewById<TextView>(R.id.native_ad_description))
                .withCtaButton(container.findViewById<Button>(R.id.native_ad_cta))
                .build()
            Tapsell.showNativeAd(loaded, view, activity)
            container
        },
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

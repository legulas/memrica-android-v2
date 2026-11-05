package app.enjoylearning504.feature.auth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

/**
 * Hands the code from the OTP SMS to [onCode] using Google's SMS Retriever API: no SMS
 * permission and no prompt. Play services only passes on an SMS that ends with this
 * app's hash (the backend's SMS_APP_HASH). One listen lasts until the first such SMS or
 * 5 minutes, so it restarts whenever [key] changes, for example after a resend.
 * Without Play services nothing arrives and the code is typed by hand.
 */
@Composable
internal fun SmsCodeListener(key: Any, onCode: (String) -> Unit) {
    val context = LocalContext.current
    val currentOnCode by rememberUpdatedState(onCode)
    DisposableEffect(key) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val extras = intent.extras ?: return
                val status = BundleCompat.getParcelable(extras, SmsRetriever.EXTRA_STATUS, Status::class.java)
                if (status?.statusCode != CommonStatusCodes.SUCCESS) return
                val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE) ?: return
                SMS_CODE.find(message)?.let { currentOnCode(it.value) }
            }
        }
        // Exported, but only Play services holds SEND_PERMISSION and can send to it
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
            SmsRetriever.SEND_PERMISSION,
            null,
            ContextCompat.RECEIVER_EXPORTED,
        )
        SmsRetriever.getClient(context).startSmsRetriever()
        onDispose { context.unregisterReceiver(receiver) }
    }
}

/** The first run of exactly six digits; the code comes before the app hash in the SMS. */
private val SMS_CODE = Regex("(?<!\\d)\\d{6}(?!\\d)")

package app.enjoylearning504.core.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.enjoylearning504.core.domain.platform.ConnectivityObserver
import org.koin.compose.koinInject

/**
 * The original app blocked lesson, learning and quiz screens while offline (they carry ads).
 * Shown on the same screens; it closes by itself when the connection is back.
 */
@Composable
fun OfflineGate(observer: ConnectivityObserver = koinInject()) {
    val online by observer.isOnline.collectAsStateWithLifecycle(initialValue = true)
    if (online) return
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text(stringResource(R.string.offline_title)) },
        text = { Text(stringResource(R.string.offline_message)) },
        confirmButton = {
            // The dialog closes on its own once online; the button just reassures the user
            TextButton(onClick = {}) { Text(stringResource(R.string.offline_check)) }
        },
    )
}

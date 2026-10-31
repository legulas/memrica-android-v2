package app.enjoylearning504.feature.info.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import app.enjoylearning504.feature.info.HelpDialog
import app.enjoylearning504.feature.info.InfoDialog
import app.enjoylearning504.feature.info.PrivacyNoticeDialog
import app.enjoylearning504.feature.info.ShareDialog
import app.enjoylearning504.feature.info.ShopDialog
import app.enjoylearning504.feature.info.SmartLearningDialog
import kotlinx.serialization.Serializable

@Serializable data object InfoDestination
@Serializable data object SmartLearningDestination
@Serializable data object ShareDestination
@Serializable data object ShopDestination
@Serializable data object HelpDestination
@Serializable data object PrivacyNoticeDestination

/** All of Home's dialogs. [onClose] pops the dialog. */
fun NavGraphBuilder.infoDialogs(onClose: () -> Unit) {
    val properties = DialogProperties(usePlatformDefaultWidth = false)
    dialog<InfoDestination>(dialogProperties = properties) { Sheet { InfoDialog(onClose) } }
    dialog<SmartLearningDestination>(dialogProperties = properties) { Sheet { SmartLearningDialog(onClose) } }
    dialog<ShareDestination>(dialogProperties = properties) { Sheet { ShareDialog(onClose) } }
    dialog<ShopDestination>(dialogProperties = properties) { Sheet { ShopDialog(onClose) } }
    dialog<HelpDestination>(dialogProperties = properties) { Sheet { HelpDialog(onClose) } }
    dialog<PrivacyNoticeDestination>(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false),
    ) { Sheet { PrivacyNoticeDialog(onClose) } }
}

@Composable
private fun Sheet(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 32.dp)) { content() }
}

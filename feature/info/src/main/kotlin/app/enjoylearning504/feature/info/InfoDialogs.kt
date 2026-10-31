package app.enjoylearning504.feature.info

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import app.enjoylearning504.core.designsystem.component.PlantStageIcon
import app.enjoylearning504.core.designsystem.component.PrimaryButton
import app.enjoylearning504.core.designsystem.component.ProgressBar
import app.enjoylearning504.core.designsystem.component.ProgressRing
import app.enjoylearning504.core.designsystem.component.WordLevelIcon
import app.enjoylearning504.core.designsystem.theme.MemricaColors
import app.enjoylearning504.core.model.Curriculum
import app.enjoylearning504.core.model.PlantStage
import app.enjoylearning504.core.model.WordLevel
import app.enjoylearning504.core.ui.R as UiR
import app.enjoylearning504.core.ui.localizedNumber
import app.enjoylearning504.core.ui.localizedPercent
import kotlinx.coroutines.launch

internal const val SHARE_LINK = "cafebazaar.ir/app/app.enjoylearning504"
internal const val INSTAGRAM_URL = "https://instagram.com/enjoylearning504"
internal const val PRIVACY_POLICY_URL = "https://yandex.com/legal/confidential/"

/** The rounded white sheet all of the app's dialogs share, with a close button at the bottom. */
@Composable
internal fun DialogSheet(
    title: String,
    onClose: () -> Unit,
    closeLabel: String = stringResource(UiR.string.action_close),
    content: @Composable () -> Unit,
) {
    Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(12.dp))
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) { content() }
            Spacer(Modifier.height(16.dp))
            PrimaryButton(closeLabel, onClose, Modifier.fillMaxWidth(0.6f))
        }
    }
}

@Composable
private fun Section(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp).semantics { heading() })
}

@Composable
private fun Body(text: String, align: TextAlign = TextAlign.Start) {
    Text(text, style = MaterialTheme.typography.bodyMedium, textAlign = align, modifier = Modifier.fillMaxWidth())
}

@Composable
fun InfoDialog(onClose: () -> Unit) {
    val context = LocalContext.current
    DialogSheet(stringResource(R.string.info_title), onClose) {
        Section(stringResource(R.string.info_source_title, localizedNumber(1)))
        Body(stringResource(R.string.info_source_body, localizedNumber(Curriculum.TOTAL_WORDS)))
        Section(stringResource(R.string.info_structure_title, localizedNumber(2)))
        Body(
            stringResource(
                R.string.info_structure_body,
                localizedNumber(Curriculum.TOTAL_WORDS),
                localizedNumber(Curriculum.CLASS_COUNT),
                localizedNumber(Curriculum.LESSONS_PER_CLASS),
                localizedNumber(Curriculum.WORDS_PER_LESSON),
            ),
        )
        Section(stringResource(R.string.info_progress_title, localizedNumber(3)))
        Body(stringResource(R.string.info_progress_word))
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            (0..WordLevel.MAX).forEach { WordLevelIcon(WordLevel(it), Modifier.size(40.dp)) }
        }
        Body(stringResource(R.string.info_progress_lesson))
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            PlantStage.entries.forEach { PlantStageIcon(it, Modifier.size(40.dp)) }
        }
        Text(localizedPercent(90.0), Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
        ProgressBar(90f, trackColor = MemricaColors.TealDeep)
        Body(stringResource(R.string.info_progress_class))
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center) {
            ProgressRing(67f, localizedPercent(67.0), size = 56.dp, trackColor = MemricaColors.Divider, labelColor = MemricaColors.Text)
        }
        Body(stringResource(R.string.info_progress_all))
        Text(
            stringResource(R.string.info_progress_all_sample, localizedPercent(28.0)),
            Modifier.fillMaxWidth().padding(vertical = 4.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )
        ProgressBar(28f, trackColor = MemricaColors.TealDeep)
        Section(stringResource(R.string.info_word_page_title, localizedNumber(4)))
        Body(stringResource(R.string.info_word_page_body, localizedNumber(0), localizedNumber(100)))
        Section(stringResource(R.string.info_privacy_title, localizedNumber(5)))
        Body(stringResource(R.string.privacy_body))
        Spacer(Modifier.height(8.dp))
        Body(stringResource(R.string.privacy_accept_note))
        TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, PRIVACY_POLICY_URL.toUri())) }) {
            Text(stringResource(R.string.info_privacy_link))
        }
    }
}

/** Shown once, on the first launch, like the original app's first-start notice. */
@Composable
fun PrivacyNoticeDialog(onClose: () -> Unit) {
    val context = LocalContext.current
    DialogSheet(stringResource(R.string.info_privacy_title, localizedNumber(1)).substringAfter(") "), onClose) {
        Body(stringResource(R.string.privacy_body))
        Spacer(Modifier.height(8.dp))
        Body(stringResource(R.string.privacy_accept_note))
        TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, PRIVACY_POLICY_URL.toUri())) }) {
            Text(stringResource(R.string.info_privacy_link))
        }
    }
}

@Composable
fun SmartLearningDialog(onClose: () -> Unit) {
    DialogSheet(stringResource(R.string.smart_title), onClose) {
        Body(stringResource(R.string.smart_toc))
        Section(stringResource(R.string.smart_part1_title))
        Body(stringResource(R.string.smart_part1_intro))
        Section(stringResource(R.string.smart_brain_title))
        Illustration(R.drawable.brain)
        Body(stringResource(R.string.smart_brain_body))
        Spacer(Modifier.height(8.dp))
        Body(stringResource(R.string.smart_synapse_gap))
        Illustration(R.drawable.synops)
        Body(stringResource(R.string.smart_synapse_built))
        Illustration(R.drawable.synopses)
        Body(stringResource(R.string.smart_part1_outro))
        Section(stringResource(R.string.smart_part2_title))
        Body(stringResource(R.string.smart_part2_intro))
        Body(stringResource(R.string.smart_stages, localizedNumber(1), localizedNumber(2)))
        Section(stringResource(R.string.smart_implementation_title))
        Section(stringResource(R.string.smart_short_term_title, localizedNumber(1)))
        Body(stringResource(R.string.smart_short_term_intro))
        Body(stringResource(R.string.smart_short_term_body, localizedNumber(12), localizedNumber(4)))
        Section(stringResource(R.string.smart_long_term_title, localizedNumber(2)))
        Body(stringResource(R.string.smart_long_term_intro))
        Spacer(Modifier.height(8.dp))
        Body(
            stringResource(
                R.string.smart_long_term_body,
                localizedNumber(12), localizedNumber(12), localizedNumber(30),
                localizedNumber(6), localizedNumber(5), localizedNumber(72),
            ),
        )
        Section(stringResource(R.string.smart_part3_title))
        Body(stringResource(R.string.smart_part3_intro))
        Spacer(Modifier.height(8.dp))
        Body(stringResource(R.string.smart_roadmap))
        Spacer(Modifier.height(16.dp))
        Body(stringResource(R.string.smart_closing), TextAlign.Center)
    }
}

@Composable
private fun Illustration(res: Int) {
    Image(
        painterResource(res),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp).padding(vertical = 8.dp),
    )
}

@Composable
fun ShareDialog(onClose: () -> Unit) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }
    val copiedLabel = stringResource(R.string.share_copied)
    DialogSheet(
        stringResource(R.string.share_title),
        onClose = {
            scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(copiedLabel, SHARE_LINK))) }
            copied = true
        },
        closeLabel = if (copied) copiedLabel else stringResource(R.string.share_copy),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Body(stringResource(R.string.share_body), TextAlign.Center)
            Image(painterResource(R.drawable.cafebazaar_logo), null, Modifier.size(40.dp).padding(top = 12.dp))
            Text(SHARE_LINK, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun ShopDialog(onClose: () -> Unit) {
    DialogSheet(stringResource(R.string.shop_title), onClose) {
        Body(stringResource(R.string.shop_body), TextAlign.Center)
    }
}

@Composable
fun HelpDialog(onClose: () -> Unit) {
    val context = LocalContext.current
    DialogSheet(
        stringResource(R.string.help_title),
        onClose = {
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, INSTAGRAM_URL.toUri())) }
            onClose()
        },
        closeLabel = stringResource(R.string.help_open_page),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Body(stringResource(R.string.help_body), TextAlign.Center)
            Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.instagram_logo), null, Modifier.size(24.dp))
                Text("EnjoyLearning504", Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

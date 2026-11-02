package app.enjoylearning504.feature.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.enjoylearning504.core.ads.AdPlacement
import app.enjoylearning504.core.ads.NativeAdBanner
import app.enjoylearning504.core.designsystem.R as DesignR
import app.enjoylearning504.core.designsystem.component.PlantStageIcon
import app.enjoylearning504.core.designsystem.component.PrimaryButton
import app.enjoylearning504.core.designsystem.component.ProgressBar
import app.enjoylearning504.core.designsystem.component.WordLevelIcon
import app.enjoylearning504.core.designsystem.theme.MemricaColors
import app.enjoylearning504.core.domain.usecase.LessonDetail
import app.enjoylearning504.core.model.PlantStage
import app.enjoylearning504.core.model.WordWithProgress
import app.enjoylearning504.core.ui.R as UiR
import app.enjoylearning504.core.ui.localizedNumber
import app.enjoylearning504.core.ui.localizedPercent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LessonRoute(
    onLearn: (lessonId: Int) -> Unit,
    onFastReview: (lessonId: Int) -> Unit,
    onWordClick: (wordId: Int) -> Unit,
    viewModel: LessonViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LessonScreen(
        state = state,
        onLearn = { if (viewModel.canLearn()) onLearn(viewModel.lessonId) },
        onFastReview = { if (viewModel.canReview()) onFastReview(viewModel.lessonId) },
        onWordClick = onWordClick,
        onKnownChange = viewModel::onKnownChange,
        onDifficultChange = viewModel::onDifficultChange,
        onMessageShown = viewModel::onMessageShown,
    )
}

@Composable
internal fun LessonScreen(
    state: LessonUiState,
    onLearn: () -> Unit,
    onFastReview: () -> Unit,
    onWordClick: (Int) -> Unit,
    onKnownChange: (Int, Boolean) -> Unit,
    onDifficultChange: (Int, Boolean) -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    val ready = state as? LessonUiState.Ready
    val messageText = ready?.message?.let { stringResource(it.text()) }
    LaunchedEffect(ready?.message) {
        if (messageText != null) {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(messageText)
            onMessageShown()
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, containerColor = MaterialTheme.colorScheme.background) { padding ->
        if (ready == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item { Header(ready.detail, onLearn, onFastReview) }
            item { NativeAdBanner(AdPlacement.NativeLesson, Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) }
            items(ready.detail.words, key = { it.word.id }) { item ->
                WordRow(item, onWordClick, onKnownChange, onDifficultChange)
                HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 6.dp)
            }
        }
    }
}

private fun LessonMessage.text(): Int = when (this) {
    LessonMessage.LearningComplete -> R.string.lesson_learning_complete
    LessonMessage.LearnFirst -> R.string.lesson_learn_first
    LessonMessage.AddedToDifficult -> R.string.lesson_added_difficult
    LessonMessage.RemovedFromDifficult -> R.string.lesson_removed_difficult
}

@Composable
private fun Header(detail: LessonDetail, onLearn: () -> Unit, onFastReview: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlantStageIcon(PlantStage.forLessonPercent(detail.percent), Modifier.size(48.dp))
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(UiR.string.lesson_n, localizedNumber(detail.lesson.id)), style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(R.string.lesson_range, localizedNumber(detail.lesson.firstWordId), localizedNumber(detail.lesson.lastWordId)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(48.dp))
        }
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier.fillMaxWidth().background(MemricaColors.Teal, MaterialTheme.shapes.extraLarge).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(localizedPercent(detail.percent), color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ProgressBar(detail.percent.toFloat())
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton(stringResource(R.string.lesson_learn), onLearn, Modifier.weight(1f))
                PrimaryButton(stringResource(R.string.lesson_fast_review), onFastReview, Modifier.weight(1f), color = MemricaColors.TealDark)
            }
        }
    }
}

@Composable
private fun WordRow(
    item: WordWithProgress,
    onWordClick: (Int) -> Unit,
    onKnownChange: (Int, Boolean) -> Unit,
    onDifficultChange: (Int, Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onWordClick(item.word.id) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WordLevelIcon(item.level, Modifier.size(52.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.word.english,
                style = MaterialTheme.typography.titleLarge.copy(textDirection = TextDirection.ContentOrLtr),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(item.word.persian, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        val difficultLabel = stringResource(R.string.lesson_mark_difficult)
        IconToggleButton(
            checked = item.isDifficult,
            onCheckedChange = { onDifficultChange(item.word.id, it) },
            modifier = Modifier.semantics { contentDescription = difficultLabel },
        ) {
            Icon(
                painterResource(if (item.isDifficult) DesignR.drawable.ic_is_difficult else DesignR.drawable.ic_not_difficult),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(28.dp),
            )
        }
        val knownLabel = stringResource(R.string.lesson_mark_known)
        Switch(
            checked = item.level.isLearned,
            onCheckedChange = { onKnownChange(item.word.id, it) },
            modifier = Modifier.semantics { contentDescription = knownLabel },
        )
    }
}

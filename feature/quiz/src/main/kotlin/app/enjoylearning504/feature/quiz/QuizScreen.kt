package app.enjoylearning504.feature.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.enjoylearning504.core.ads.AdPlacement
import app.enjoylearning504.core.ads.NativeAdBanner
import app.enjoylearning504.core.ads.rememberInterstitialExit
import app.enjoylearning504.core.designsystem.R as DesignR
import app.enjoylearning504.core.designsystem.component.PrimaryButton
import app.enjoylearning504.core.designsystem.component.ProgressBar
import app.enjoylearning504.core.designsystem.component.SurfaceCard
import app.enjoylearning504.core.designsystem.theme.MemricaColors
import app.enjoylearning504.core.domain.quiz.QuizMode
import app.enjoylearning504.core.domain.usecase.QuizResult
import app.enjoylearning504.core.model.WordWithProgress
import app.enjoylearning504.core.ui.AnswerGrid
import app.enjoylearning504.core.ui.R as UiR
import app.enjoylearning504.core.ui.localizedNumber
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun QuizRoute(onExit: () -> Unit, viewModel: QuizViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val showAd by viewModel.showAdOnExit.collectAsStateWithLifecycle()
    val wrongWords by viewModel.wrongWords.collectAsStateWithLifecycle()
    val exit = rememberInterstitialExit(viewModel.mode.interstitial(), enabled = showAd)
    BackHandler { exit(onExit) }
    QuizScreen(
        mode = viewModel.mode,
        state = state,
        wrongWords = wrongWords,
        onAnswer = viewModel::onAnswer,
        onDifficultChange = viewModel::onDifficultChange,
        onBack = { exit(onExit) },
    )
}

private fun QuizMode.interstitial() = when (this) {
    is QuizMode.Lesson -> AdPlacement.InterstitialLessonQuiz
    is QuizMode.Class -> AdPlacement.InterstitialClassQuiz
    is QuizMode.Difficult -> AdPlacement.InterstitialDifficultQuiz
}

private fun QuizMode.resultAd() = when (this) {
    is QuizMode.Lesson -> AdPlacement.NativeLessonQuizResult
    is QuizMode.Class -> AdPlacement.NativeClassQuizResult
    is QuizMode.Difficult -> AdPlacement.NativeDifficultQuizResult
}

@Composable
internal fun QuizScreen(
    mode: QuizMode,
    state: QuizUiState,
    wrongWords: List<WordWithProgress>,
    onAnswer: (Int) -> Unit,
    onDifficultChange: (Int, Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    val playing = state as? QuizUiState.Playing
    val readyText = playing?.let { stringResource(R.string.quiz_ready_words, localizedNumber(it.quiz.questions.size)) }
    LaunchedEffect(playing?.showReadyCount) {
        if (playing?.showReadyCount == true && readyText != null) snackbar.showSnackbar(readyText)
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, containerColor = MaterialTheme.colorScheme.background) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(16.dp)) {
                when (state) {
                    QuizUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                    QuizUiState.Unavailable -> Text(stringResource(R.string.quiz_unavailable), Modifier.padding(32.dp))
                    is QuizUiState.Playing -> {
                        val quiz = state.quiz
                        Scoreboard(quiz.rightCount, quiz.wrongCount, quiz.progressPercent.toFloat())
                        val question = quiz.current ?: return@Column
                        AnswerGrid(
                            questionKey = quiz.index,
                            word = question.word,
                            options = question.options,
                            askInEnglish = true,
                            level = null,
                            onAnswered = onAnswer,
                        )
                    }
                    is QuizUiState.Finished -> {
                        Scoreboard(state.result.rightCount, state.result.wrongCount, 100f)
                        ResultContent(mode, state.result, wrongWords, onDifficultChange, onBack)
                    }
                }
            }
        }
    }
}

/** Green right-answer count, progress, red wrong-answer count. */
@Composable
private fun Scoreboard(right: Int, wrong: Int, percent: Float) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Counter(right, MemricaColors.Green, stringResource(R.string.quiz_right_count, localizedNumber(right)))
        ProgressBar(percent, Modifier.weight(1f).padding(horizontal = 12.dp), trackColor = MaterialTheme.colorScheme.surface)
        Counter(wrong, MemricaColors.Red, stringResource(R.string.quiz_wrong_count, localizedNumber(wrong)))
    }
}

@Composable
private fun Counter(value: Int, color: Color, description: String) {
    Box(
        Modifier.size(44.dp).background(color, CircleShape).semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(localizedNumber(value), color = Color.White, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ResultContent(
    mode: QuizMode,
    result: QuizResult,
    wrongWords: List<WordWithProgress>,
    onDifficultChange: (Int, Boolean) -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            SurfaceCard(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val total = if (mode is QuizMode.Class) {
                        "${localizedNumber(result.questionCount)}/${localizedNumber(result.classWordCount)}"
                    } else {
                        localizedNumber(result.questionCount)
                    }
                    ResultRow(stringResource(R.string.quiz_words_in_test), total, MaterialTheme.colorScheme.onSurface)
                    ResultRow(stringResource(R.string.quiz_right), localizedNumber(result.rightCount), MemricaColors.Green)
                    ResultRow(stringResource(R.string.quiz_wrong), localizedNumber(result.wrongCount), MemricaColors.Red)
                    result.highScore?.let { best ->
                        Text(
                            if (result.isNewHighScore) stringResource(R.string.quiz_new_record)
                            else stringResource(R.string.quiz_record, localizedNumber(best)),
                            color = if (result.isNewHighScore) MemricaColors.Green else MemricaColors.Muted,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
        item { NativeAdBanner(mode.resultAd(), Modifier.fillMaxWidth().padding(vertical = 12.dp)) }
        item {
            PrimaryButton(stringResource(UiR.string.action_back), onBack, Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.quiz_wrong_list),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            if (mode !is QuizMode.Difficult) {
                Text(
                    stringResource(R.string.quiz_wrong_list_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(12.dp))
        }
        if (result.wrongWordIds.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.quiz_no_wrong),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
        items(wrongWords, key = { it.word.id }) { item ->
            WrongWordRow(item, onDifficultChange)
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleMedium, color = color)
    }
}

@Composable
private fun WrongWordRow(item: WordWithProgress, onDifficultChange: (Int, Boolean) -> Unit) {
    SurfaceCard(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.word.english, style = MaterialTheme.typography.titleMedium.copy(textDirection = TextDirection.Ltr))
                Text(item.word.persian, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val label = stringResource(R.string.quiz_mark_difficult)
            IconToggleButton(
                checked = item.isDifficult,
                onCheckedChange = { onDifficultChange(item.word.id, it) },
                modifier = Modifier.semantics { contentDescription = label },
            ) {
                Icon(
                    painterResource(if (item.isDifficult) DesignR.drawable.ic_is_difficult else DesignR.drawable.ic_not_difficult),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

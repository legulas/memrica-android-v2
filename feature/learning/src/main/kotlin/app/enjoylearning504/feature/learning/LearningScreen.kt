package app.enjoylearning504.feature.learning

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.enjoylearning504.core.ads.AdPlacement
import app.enjoylearning504.core.ads.NativeAdBanner
import app.enjoylearning504.core.ads.rememberInterstitialExit
import app.enjoylearning504.core.designsystem.component.PrimaryButton
import app.enjoylearning504.core.designsystem.component.ProgressBar
import app.enjoylearning504.core.designsystem.component.SurfaceCard
import app.enjoylearning504.core.designsystem.component.WordLevelIcon
import app.enjoylearning504.core.designsystem.component.stepTransition
import app.enjoylearning504.core.designsystem.theme.MemricaColors
import app.enjoylearning504.core.domain.learning.LearningState
import app.enjoylearning504.core.domain.learning.LearningStep
import app.enjoylearning504.core.model.Word
import app.enjoylearning504.core.model.WordLevel
import app.enjoylearning504.core.model.WordWithProgress
import app.enjoylearning504.core.ui.AnswerGrid
import app.enjoylearning504.core.ui.R as UiR
import app.enjoylearning504.core.ui.SentencePager
import app.enjoylearning504.core.ui.WordCard
import app.enjoylearning504.core.ui.localizedNumber
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LearningRoute(onExit: () -> Unit, viewModel: LearningViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val exit = rememberInterstitialExit(AdPlacement.InterstitialLearning, enabled = state.showAdOnExit)
    BackHandler { exit(onExit) }
    LearningScreen(
        state = state,
        onContinue = viewModel::onContinue,
        onAnswer = viewModel::onAnswer,
        onChosen = viewModel::onChosen,
        onPlay = viewModel::playWord,
        onSpeak = viewModel::speak,
        onBackToLesson = { exit(onExit) },
    )
}

@Composable
internal fun LearningScreen(
    state: LearningUiState,
    onContinue: () -> Unit,
    onAnswer: (Int) -> Unit,
    onChosen: (Boolean) -> Unit,
    onPlay: (Word) -> Unit,
    onSpeak: (String) -> Unit,
    onBackToLesson: () -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        val session = state.session
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(16.dp)) {
                ProgressBar(
                    percent = session?.progressPercent?.toFloat() ?: 100f,
                    trackColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                AnimatedContent(
                    targetState = session,
                    contentKey = { it?.stepKey() },
                    transitionSpec = { stepTransition() },
                    label = "learningStep",
                ) { shown -> Step(shown, state.lessonId, onContinue, onAnswer, onChosen, onPlay, onSpeak, onBackToLesson) }
            }
        }
    }
}

/** A step changes whenever the step or the counts behind it change; equal keys stay on screen. */
private fun LearningState.stepKey(): Any = Triple(slots.count { it.done }, totalMistakes, step)

@Composable
private fun Step(
    session: LearningState?,
    lessonId: Int,
    onContinue: () -> Unit,
    onAnswer: (Int) -> Unit,
    onChosen: (Boolean) -> Unit,
    onPlay: (Word) -> Unit,
    onSpeak: (String) -> Unit,
    onBackToLesson: () -> Unit,
) {
    when (val step = session?.step) {
        is LearningStep.Card -> CardStep(step.word, step.level, onPlay, onSpeak, onContinue)
        is LearningStep.SecondChanceCard -> CardStep(step.word, step.level, onPlay, onSpeak, onContinue)
        is LearningStep.Question -> AnswerGrid(
            questionKey = (session?.slots?.count { it.done } ?: 0) to step,
            word = step.word,
            options = step.options,
            askInEnglish = step.exercise.asksPersianAnswer,
            level = step.level,
            onAnswered = onAnswer,
            onChosen = onChosen,
        )
        is LearningStep.SecondChanceQuestion -> AnswerGrid(
            questionKey = (session?.totalMistakes ?: 0) to step,
            word = step.word,
            options = step.options,
            askInEnglish = true,
            level = step.level,
            onAnswered = onAnswer,
            onChosen = onChosen,
        )
        is LearningStep.Finished -> Finished(step.results, lessonId, onBackToLesson)
        null -> Finished(emptyList(), lessonId, onBackToLesson)
    }
}

@Composable
private fun CardStep(
    word: Word,
    level: WordLevel,
    onPlay: (Word) -> Unit,
    onSpeak: (String) -> Unit,
    onContinue: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            WordCard(word, level, onPlay = { onPlay(word) }, Modifier.padding(top = 16.dp))
            Spacer(Modifier.height(12.dp))
            SentencePager(word.sentences, onSpeak, stringResource(R.string.learning_read_sentence))
        }
        PrimaryButton(stringResource(UiR.string.action_continue), onContinue, Modifier.fillMaxWidth().padding(top = 12.dp))
    }
}

@Composable
private fun Finished(results: List<WordWithProgress>, lessonId: Int, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        results.chunked(2).forEachIndexed { rowIndex, row ->
            Row(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                row.forEachIndexed { column, item ->
                    SurfaceCard(Modifier.weight(1f).padding(horizontal = 6.dp).riseIn(order = rowIndex * 2 + column)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            WordLevelIcon(item.level, Modifier.size(56.dp))
                            Text(
                                item.word.english,
                                style = MaterialTheme.typography.titleMedium.copy(textDirection = TextDirection.Ltr),
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }
        }
        NativeAdBanner(AdPlacement.NativeLearningComplete, Modifier.fillMaxWidth().padding(vertical = 16.dp))
        Text(
            stringResource(R.string.learning_finished_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MemricaColors.Text,
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            stringResource(R.string.learning_back_to_lesson, localizedNumber(lessonId)),
            onBack,
            Modifier.fillMaxWidth(),
        )
    }
}

/** Fades and lifts the content in once, [order] steps after the first item. */
@Composable
private fun Modifier.riseIn(order: Int): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 280, delayMillis = 60 * order.coerceAtMost(8)))
    }
    return graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 24.dp.toPx()
    }
}

package app.enjoylearning504.feature.learning

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import app.enjoylearning504.core.designsystem.theme.MemricaColors
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
                    )
                    is LearningStep.SecondChanceQuestion -> AnswerGrid(
                        questionKey = (session?.totalMistakes ?: 0) to step,
                        word = step.word,
                        options = step.options,
                        askInEnglish = true,
                        level = step.level,
                        onAnswered = onAnswer,
                    )
                    is LearningStep.Finished -> Finished(step.results, state.lessonId, onBackToLesson)
                    null -> Finished(emptyList(), state.lessonId, onBackToLesson)
                }
            }
        }
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
        results.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                row.forEach { item ->
                    SurfaceCard(Modifier.weight(1f).padding(horizontal = 6.dp)) {
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

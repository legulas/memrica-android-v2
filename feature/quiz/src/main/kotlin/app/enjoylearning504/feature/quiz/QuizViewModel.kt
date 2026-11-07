package app.enjoylearning504.feature.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import app.enjoylearning504.core.domain.platform.SoundEffect
import app.enjoylearning504.core.domain.platform.SoundEffects
import app.enjoylearning504.core.domain.quiz.QuizMode
import app.enjoylearning504.core.domain.quiz.QuizState
import app.enjoylearning504.core.domain.usecase.BuildQuizUseCase
import app.enjoylearning504.core.domain.usecase.FinishQuizUseCase
import app.enjoylearning504.core.domain.usecase.ObserveWordsUseCase
import app.enjoylearning504.core.domain.usecase.QuizAvailability
import app.enjoylearning504.core.domain.usecase.QuizResult
import app.enjoylearning504.core.domain.usecase.SetWordDifficultUseCase
import app.enjoylearning504.core.domain.usecase.ShouldShowInterstitialUseCase
import app.enjoylearning504.core.model.WordWithProgress
import app.enjoylearning504.feature.quiz.navigation.QuizDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface QuizUiState {
    data object Loading : QuizUiState
    data object Unavailable : QuizUiState
    data class Playing(val quiz: QuizState, val showReadyCount: Boolean) : QuizUiState
    data class Finished(val result: QuizResult) : QuizUiState
}

class QuizViewModel(
    savedStateHandle: SavedStateHandle,
    private val buildQuiz: BuildQuizUseCase,
    private val finishQuiz: FinishQuizUseCase,
    private val shouldShowInterstitial: ShouldShowInterstitialUseCase,
    private val setWordDifficult: SetWordDifficultUseCase,
    private val sounds: SoundEffects,
    observeWords: ObserveWordsUseCase,
) : ViewModel() {
    val mode: QuizMode = savedStateHandle.toRoute<QuizDestination>().toMode()

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _showAdOnExit = MutableStateFlow(false)
    val showAdOnExit: StateFlow<Boolean> = _showAdOnExit.asStateFlow()

    /** The wrong words with their current difficult flag, for the result list. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val wrongWords: StateFlow<List<WordWithProgress>> = _uiState
        .flatMapLatest { state ->
            if (state is QuizUiState.Finished) observeWords(state.result.wrongWordIds) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        sounds.prepare()
        viewModelScope.launch {
            _showAdOnExit.value = shouldShowInterstitial()
            _uiState.value = when (val availability = buildQuiz(mode)) {
                is QuizAvailability.Ready -> QuizUiState.Playing(availability.quiz, showReadyCount = mode is QuizMode.Class)
                else -> QuizUiState.Unavailable
            }
        }
    }

    fun onAnswer(optionWordId: Int) {
        val playing = _uiState.value as? QuizUiState.Playing ?: return
        val next = playing.quiz.answer(optionWordId)
        if (!next.isFinished) {
            _uiState.update { playing.copy(quiz = next, showReadyCount = false) }
            return
        }
        viewModelScope.launch {
            _uiState.value = QuizUiState.Finished(finishQuiz(next))
            sounds.play(SoundEffect.Complete)
        }
    }

    /** Feedback at the moment of the tap, before the answer is recorded. */
    fun onChosen(correct: Boolean) = sounds.play(if (correct) SoundEffect.Correct else SoundEffect.Wrong)

    fun onDifficultChange(wordId: Int, difficult: Boolean) {
        viewModelScope.launch { setWordDifficult(wordId, difficult) }
    }
}

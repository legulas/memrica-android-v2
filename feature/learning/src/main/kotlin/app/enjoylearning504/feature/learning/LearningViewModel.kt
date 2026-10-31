package app.enjoylearning504.feature.learning

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import app.enjoylearning504.core.domain.learning.LearningSessionEngine
import app.enjoylearning504.core.domain.learning.LearningState
import app.enjoylearning504.core.domain.learning.Transition
import app.enjoylearning504.core.domain.platform.PronunciationPlayer
import app.enjoylearning504.core.domain.usecase.SaveLevelUseCase
import app.enjoylearning504.core.domain.usecase.ShouldShowInterstitialUseCase
import app.enjoylearning504.core.domain.usecase.StartLearningSessionUseCase
import app.enjoylearning504.core.model.Word
import app.enjoylearning504.feature.learning.navigation.LearningDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LearningUiState(
    val lessonId: Int,
    val session: LearningState? = null,
    val isLoading: Boolean = true,
    /** Whether leaving this session should show an interstitial (decided once, on entry). */
    val showAdOnExit: Boolean = false,
)

class LearningViewModel(
    savedStateHandle: SavedStateHandle,
    private val startSession: StartLearningSessionUseCase,
    private val saveLevel: SaveLevelUseCase,
    private val shouldShowInterstitial: ShouldShowInterstitialUseCase,
    private val engine: LearningSessionEngine,
    private val player: PronunciationPlayer,
) : ViewModel() {
    private val lessonId = savedStateHandle.toRoute<LearningDestination>().lessonId
    private val _uiState = MutableStateFlow(LearningUiState(lessonId))
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val showAd = shouldShowInterstitial()
            _uiState.value = LearningUiState(lessonId, startSession(lessonId), isLoading = false, showAdOnExit = showAd)
        }
    }

    fun onContinue() = reduce { engine.onContinue(it) }

    fun onAnswer(optionWordId: Int) = reduce { engine.onAnswer(it, optionWordId) }

    fun playWord(word: Word) = player.playWord(word)

    fun speak(sentence: String) = player.speakSentence(sentence)

    override fun onCleared() = player.stop()

    private fun reduce(transition: (LearningState) -> Transition) {
        val session = _uiState.value.session ?: return
        val next = transition(session)
        _uiState.update { it.copy(session = next.state) }
        next.completed?.let { completed -> viewModelScope.launch { saveLevel(completed) } }
    }
}

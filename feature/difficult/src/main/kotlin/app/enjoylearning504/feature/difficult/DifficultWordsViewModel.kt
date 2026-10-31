package app.enjoylearning504.feature.difficult

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import app.enjoylearning504.core.domain.quiz.QuizBuilder
import app.enjoylearning504.core.domain.usecase.ObserveDifficultWordsUseCase
import app.enjoylearning504.core.domain.usecase.SetWordDifficultUseCase
import app.enjoylearning504.core.model.WordWithProgress
import app.enjoylearning504.feature.difficult.navigation.DifficultWordsDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DifficultWordsUiState(
    val classId: Int,
    val words: List<WordWithProgress>? = null,
    val showMinimumMessage: Boolean = false,
)

class DifficultWordsViewModel(
    savedStateHandle: SavedStateHandle,
    observeDifficultWords: ObserveDifficultWordsUseCase,
    private val setWordDifficult: SetWordDifficultUseCase,
) : ViewModel() {
    val classId: Int = savedStateHandle.toRoute<DifficultWordsDestination>().classId
    private val showMinimum = MutableStateFlow(false)

    val uiState: StateFlow<DifficultWordsUiState> =
        combine(observeDifficultWords(classId), showMinimum) { words, showMinimum ->
            DifficultWordsUiState(classId, words, showMinimum)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DifficultWordsUiState(classId))

    fun canStartReview(): Boolean {
        val count = uiState.value.words?.size ?: 0
        if (count >= QuizBuilder.MIN_WORDS) return true
        showMinimum.value = true
        return false
    }

    fun onMessageShown() {
        showMinimum.value = false
    }

    fun remove(wordId: Int) {
        viewModelScope.launch { setWordDifficult(wordId, false) }
    }
}

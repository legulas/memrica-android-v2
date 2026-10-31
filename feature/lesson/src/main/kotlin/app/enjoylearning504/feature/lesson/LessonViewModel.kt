package app.enjoylearning504.feature.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import app.enjoylearning504.core.domain.usecase.LessonDetail
import app.enjoylearning504.core.domain.usecase.ObserveLessonUseCase
import app.enjoylearning504.core.domain.usecase.SetWordDifficultUseCase
import app.enjoylearning504.core.domain.usecase.SetWordKnownUseCase
import app.enjoylearning504.feature.lesson.navigation.LessonDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LessonMessage { LearningComplete, LearnFirst, AddedToDifficult, RemovedFromDifficult }

sealed interface LessonUiState {
    data object Loading : LessonUiState
    data class Ready(val detail: LessonDetail, val message: LessonMessage? = null) : LessonUiState
}

class LessonViewModel(
    savedStateHandle: SavedStateHandle,
    observeLesson: ObserveLessonUseCase,
    private val setWordKnown: SetWordKnownUseCase,
    private val setWordDifficult: SetWordDifficultUseCase,
) : ViewModel() {
    val lessonId: Int = savedStateHandle.toRoute<LessonDestination>().lessonId
    private val message = MutableStateFlow<LessonMessage?>(null)

    val uiState: StateFlow<LessonUiState> = combine(observeLesson(lessonId), message) { detail, message ->
        LessonUiState.Ready(detail, message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LessonUiState.Loading)

    /** Returns true when a learning session can start. */
    fun canLearn(): Boolean = check(!(detail()?.percent?.let { it >= 100.0 } ?: true), LessonMessage.LearningComplete)

    /** Returns true when the fast review can start. */
    fun canReview(): Boolean = check(detail()?.isFullyLearned == true, LessonMessage.LearnFirst)

    fun onKnownChange(wordId: Int, known: Boolean) {
        viewModelScope.launch { setWordKnown(wordId, known) }
    }

    fun onDifficultChange(wordId: Int, difficult: Boolean) {
        viewModelScope.launch { setWordDifficult(wordId, difficult) }
        message.value = if (difficult) LessonMessage.AddedToDifficult else LessonMessage.RemovedFromDifficult
    }

    fun onMessageShown() {
        message.value = null
    }

    private fun detail() = (uiState.value as? LessonUiState.Ready)?.detail

    private fun check(allowed: Boolean, otherwise: LessonMessage): Boolean {
        if (!allowed) message.value = otherwise
        return allowed
    }
}

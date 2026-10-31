package app.enjoylearning504.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.enjoylearning504.core.domain.quiz.QuizBuilder
import app.enjoylearning504.core.domain.repository.AppStateRepository
import app.enjoylearning504.core.domain.usecase.ObserveHomeProgressUseCase
import app.enjoylearning504.core.model.HomeProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Ready(
        val progress: HomeProgress,
        val showPrivacyNotice: Boolean,
        val message: HomeMessage? = null,
    ) : HomeUiState {
        val completedLessons: Int = progress.classes.sumOf { c -> c.lessons.count { it.percent >= 100.0 } }
    }
}

enum class HomeMessage { NeedFiveLearnedWords }

class HomeViewModel(
    observeHomeProgress: ObserveHomeProgressUseCase,
    private val appState: AppStateRepository,
) : ViewModel() {

    private val message = MutableStateFlow<HomeMessage?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        observeHomeProgress(),
        appState.privacyNoticeShown,
        message,
    ) { progress, privacyShown, message ->
        HomeUiState.Ready(progress, showPrivacyNotice = !privacyShown, message = message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    /** Returns true when the class quiz can start; otherwise shows why not. */
    fun canStartClassQuiz(classId: Int): Boolean {
        val ready = uiState.value as? HomeUiState.Ready ?: return false
        val learned = ready.progress.classes.firstOrNull { it.classId == classId }?.learnedWordCount ?: 0
        if (learned >= QuizBuilder.MIN_WORDS) return true
        message.value = HomeMessage.NeedFiveLearnedWords
        return false
    }

    fun onMessageShown() = message.update { null }

    fun onPrivacyNoticeDismissed() {
        viewModelScope.launch { appState.markPrivacyNoticeShown() }
    }
}

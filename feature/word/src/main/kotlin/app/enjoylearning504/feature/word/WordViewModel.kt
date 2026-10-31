package app.enjoylearning504.feature.word

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import app.enjoylearning504.core.domain.platform.PronunciationPlayer
import app.enjoylearning504.core.domain.usecase.ObserveWordUseCase
import app.enjoylearning504.core.model.WordWithProgress
import app.enjoylearning504.feature.word.navigation.WordDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class WordViewModel(
    savedStateHandle: SavedStateHandle,
    observeWord: ObserveWordUseCase,
    private val player: PronunciationPlayer,
) : ViewModel() {
    private val wordId = savedStateHandle.toRoute<WordDestination>().wordId

    val word: StateFlow<WordWithProgress?> =
        observeWord(wordId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun playWord() {
        word.value?.let { player.playWord(it.word) }
    }

    fun speak(sentence: String) = player.speakSentence(sentence)

    override fun onCleared() = player.stop()
}

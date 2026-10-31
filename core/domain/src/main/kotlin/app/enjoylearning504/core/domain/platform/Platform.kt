package app.enjoylearning504.core.domain.platform

import app.enjoylearning504.core.model.Word
import kotlinx.coroutines.flow.Flow

/** Plays a word's recorded pronunciation and reads example sentences aloud. */
interface PronunciationPlayer {
    fun playWord(word: Word)
    fun speakSentence(sentence: String)
    fun stop()
}

interface ConnectivityObserver {
    val isOnline: Flow<Boolean>
}

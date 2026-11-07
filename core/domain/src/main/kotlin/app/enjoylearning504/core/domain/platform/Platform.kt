package app.enjoylearning504.core.domain.platform

import app.enjoylearning504.core.model.Word

/** Plays a word's recorded pronunciation and reads example sentences aloud. */
interface PronunciationPlayer {
    fun playWord(word: Word)
    fun speakSentence(sentence: String)
    fun stop()
}

enum class SoundEffect { Correct, Wrong, Complete }

/** Short feedback sounds. They play on the system sound stream, so silent and vibrate mode mute them. */
interface SoundEffects {
    /** Loads the sounds in the background so the first [play] isn't silent. */
    fun prepare()
    fun play(effect: SoundEffect)
}

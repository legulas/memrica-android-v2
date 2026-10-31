package app.enjoylearning504.core.model

data class WordProgress(
    val wordId: Int,
    val level: WordLevel = WordLevel.New,
    val isDifficult: Boolean = false,
)

/** A word together with the user's progress on it. */
data class WordWithProgress(
    val word: Word,
    val level: WordLevel,
    val isDifficult: Boolean,
)

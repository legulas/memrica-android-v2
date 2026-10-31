package app.enjoylearning504.core.model

/**
 * How far a word has got through the learning steps, 0 (new) to 5 (learned).
 * Each level also picks the plant drawn next to the word.
 */
@JvmInline
value class WordLevel(val value: Int) {
    init {
        require(value in NEW_VALUE..LEARNED_VALUE) { "Word level must be 0..5, was $value" }
    }

    val isLearned: Boolean get() = value == LEARNED_VALUE

    companion object {
        private const val NEW_VALUE = 0
        private const val LEARNED_VALUE = 5
        val New = WordLevel(NEW_VALUE)
        val Learned = WordLevel(LEARNED_VALUE)
        const val MAX = LEARNED_VALUE
    }
}

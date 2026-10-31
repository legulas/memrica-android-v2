package app.enjoylearning504.core.domain.quiz

import app.enjoylearning504.core.model.Word
import kotlin.random.Random

/** The three quick-review quizzes of the original app. */
sealed interface QuizMode {
    /** All 12 words of a lesson; available once every word is learned. */
    data class Lesson(val lessonId: Int) : QuizMode

    /** The learned words of a class (at least five). */
    data class Class(val classId: Int) : QuizMode

    /** The words marked difficult in a class (at least five). */
    data class Difficult(val classId: Int) : QuizMode
}

data class QuizQuestion(val word: Word, val options: List<Word>)

data class QuizState(
    val mode: QuizMode,
    val questions: List<QuizQuestion>,
    val index: Int = 0,
    val rightCount: Int = 0,
    val wrongWords: List<Word> = emptyList(),
) {
    val isFinished: Boolean get() = index >= questions.size
    val current: QuizQuestion? get() = questions.getOrNull(index)
    val wrongCount: Int get() = wrongWords.size
    val progressPercent: Int get() = if (questions.isEmpty()) 0 else index * 100 / questions.size

    fun answer(optionWordId: Int): QuizState {
        val question = current ?: return this
        return if (optionWordId == question.word.id) {
            copy(index = index + 1, rightCount = rightCount + 1)
        } else {
            copy(index = index + 1, wrongWords = wrongWords + question.word)
        }
    }
}

class QuizBuilder(private val random: Random = Random.Default) {

    /**
     * Builds a quiz over [words], asking English to Persian with four options.
     * Wrong options come from [distractorPool], which must hold at least four words.
     */
    fun build(mode: QuizMode, words: List<Word>, distractorPool: List<Word>): QuizState {
        val questions = words.shuffled(random).map { word ->
            val distractors = distractorPool.filter { it.id != word.id }.shuffled(random).take(3)
            QuizQuestion(word, (distractors + word).shuffled(random))
        }
        return QuizState(mode = mode, questions = questions)
    }

    companion object {
        /** Class and difficult-word quizzes need at least this many words. */
        const val MIN_WORDS = 5
    }
}

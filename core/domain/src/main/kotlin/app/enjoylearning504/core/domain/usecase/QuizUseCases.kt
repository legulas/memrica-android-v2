package app.enjoylearning504.core.domain.usecase

import app.enjoylearning504.core.domain.quiz.QuizBuilder
import app.enjoylearning504.core.domain.quiz.QuizMode
import app.enjoylearning504.core.domain.quiz.QuizState
import app.enjoylearning504.core.domain.repository.ContentRepository
import app.enjoylearning504.core.domain.repository.ProgressRepository
import app.enjoylearning504.core.model.Curriculum
import kotlinx.coroutines.flow.first

sealed interface QuizAvailability {
    data class Ready(val quiz: QuizState) : QuizAvailability

    /** Lesson review: every word of the lesson must be learned first. */
    data object LessonNotLearned : QuizAvailability

    /** Class and difficult-word quizzes need at least [QuizBuilder.MIN_WORDS] words. */
    data object NotEnoughWords : QuizAvailability
}

class BuildQuizUseCase(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
    private val builder: QuizBuilder,
) {
    suspend operator fun invoke(mode: QuizMode): QuizAvailability {
        val words = content.words()
        val levels = progress.observeWordProgress().first()
        return when (mode) {
            is QuizMode.Lesson -> {
                val lessonWords = words.filter { it.lessonId == mode.lessonId }.withProgress(levels)
                if (lessonWords.any { !it.level.isLearned }) return QuizAvailability.LessonNotLearned
                val pool = lessonWords.map { it.word }
                QuizAvailability.Ready(builder.build(mode, pool, pool))
            }
            is QuizMode.Class -> {
                val learned = words.filter { it.classId == mode.classId }.withProgress(levels)
                    .filter { it.level.isLearned }.map { it.word }
                if (learned.size < QuizBuilder.MIN_WORDS) return QuizAvailability.NotEnoughWords
                QuizAvailability.Ready(builder.build(mode, learned, learned))
            }
            is QuizMode.Difficult -> {
                val classWords = words.filter { it.classId == mode.classId }
                val difficult = classWords.withProgress(levels).filter { it.isDifficult }.map { it.word }
                if (difficult.size < QuizBuilder.MIN_WORDS) return QuizAvailability.NotEnoughWords
                QuizAvailability.Ready(builder.build(mode, difficult, classWords))
            }
        }
    }
}

data class QuizResult(
    val mode: QuizMode,
    val questionCount: Int,
    val rightCount: Int,
    val wrongCount: Int,
    val wrongWordIds: List<Int>,
    /** Only for class quizzes. */
    val highScore: Int? = null,
    val isNewHighScore: Boolean = false,
) {
    val classWordCount: Int get() = Curriculum.WORDS_PER_CLASS
}

class FinishQuizUseCase(private val progress: ProgressRepository) {
    suspend operator fun invoke(quiz: QuizState): QuizResult {
        val base = QuizResult(
            mode = quiz.mode,
            questionCount = quiz.questions.size,
            rightCount = quiz.rightCount,
            wrongCount = quiz.wrongCount,
            wrongWordIds = quiz.wrongWords.map { it.id },
        )
        val mode = quiz.mode as? QuizMode.Class ?: return base
        val previous = progress.observeHighScores().first()[mode.classId] ?: 0
        val isNew = progress.saveHighScoreIfBetter(mode.classId, quiz.rightCount)
        return base.copy(highScore = maxOf(previous, quiz.rightCount), isNewHighScore = isNew)
    }
}

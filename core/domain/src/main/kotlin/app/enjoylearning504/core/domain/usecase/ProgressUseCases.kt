package app.enjoylearning504.core.domain.usecase

import app.enjoylearning504.core.domain.progress.ProgressCalculator
import app.enjoylearning504.core.domain.repository.ContentRepository
import app.enjoylearning504.core.domain.repository.ProgressRepository
import app.enjoylearning504.core.model.HomeProgress
import app.enjoylearning504.core.model.Lesson
import app.enjoylearning504.core.model.Word
import app.enjoylearning504.core.model.WordLevel
import app.enjoylearning504.core.model.WordProgress
import app.enjoylearning504.core.model.WordWithProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class ObserveHomeProgressUseCase(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
) {
    operator fun invoke(): Flow<HomeProgress> = flow {
        val classes = content.classes()
        val words = content.words()
        emitAll(
            combine(progress.observeWordProgress(), progress.observeHighScores()) { levels, scores ->
                ProgressCalculator.home(classes, words, levels, scores)
            },
        )
    }
}

data class LessonDetail(
    val lesson: Lesson,
    val words: List<WordWithProgress>,
    val percent: Double,
) {
    val isFullyLearned: Boolean get() = words.isNotEmpty() && words.all { it.level.isLearned }
}

class ObserveLessonUseCase(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
) {
    operator fun invoke(lessonId: Int): Flow<LessonDetail> = flow {
        val lesson = content.lesson(lessonId)
        val words = content.words().filter { it.lessonId == lessonId }.sortedBy { it.id }
        emitAll(
            progress.observeWordProgress().map { levels ->
                val items = words.withProgress(levels)
                LessonDetail(lesson, items, ProgressCalculator.lessonPercent(items.map { it.level }))
            },
        )
    }
}

class ObserveDifficultWordsUseCase(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
) {
    operator fun invoke(classId: Int): Flow<List<WordWithProgress>> = flow {
        val words = content.words().filter { it.classId == classId }.sortedBy { it.id }
        emitAll(progress.observeWordProgress().map { levels -> words.withProgress(levels).filter { it.isDifficult } })
    }
}

class ObserveWordUseCase(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
) {
    operator fun invoke(wordId: Int): Flow<WordWithProgress> = flow {
        val word = content.word(wordId)
        emitAll(progress.observeWordProgress().map { listOf(word).withProgress(it).single() })
    }
}

/** The lesson-screen toggle: marks a word learned (level 5) or resets it to new. */
class SetWordKnownUseCase(private val progress: ProgressRepository) {
    suspend operator fun invoke(wordId: Int, known: Boolean) {
        progress.setLevel(wordId, if (known) WordLevel.Learned else WordLevel.New)
    }
}

class SetWordDifficultUseCase(private val progress: ProgressRepository) {
    suspend operator fun invoke(wordId: Int, isDifficult: Boolean) = progress.setDifficult(wordId, isDifficult)
}

internal fun List<Word>.withProgress(levels: Map<Int, WordProgress>): List<WordWithProgress> = map { word ->
    val entry = levels[word.id]
    WordWithProgress(word, entry?.level ?: WordLevel.New, entry?.isDifficult ?: false)
}

class ObserveWordsUseCase(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
) {
    operator fun invoke(wordIds: List<Int>): Flow<List<WordWithProgress>> = flow {
        val all = content.words().associateBy { it.id }
        val words = wordIds.mapNotNull { all[it] }
        emitAll(progress.observeWordProgress().map { words.withProgress(it) })
    }
}

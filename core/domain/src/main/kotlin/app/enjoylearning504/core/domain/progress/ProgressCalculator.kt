package app.enjoylearning504.core.domain.progress

import app.enjoylearning504.core.model.ClassProgress
import app.enjoylearning504.core.model.Curriculum
import app.enjoylearning504.core.model.HomeProgress
import app.enjoylearning504.core.model.LessonProgress
import app.enjoylearning504.core.model.SchoolClass
import app.enjoylearning504.core.model.Word
import app.enjoylearning504.core.model.WordLevel
import app.enjoylearning504.core.model.WordProgress

/**
 * Derives every percentage from the stored word levels, so the figures can never drift.
 * Each level of each word is worth the same share: 1/60 of a lesson and 1/360 of a class.
 */
object ProgressCalculator {

    fun lessonPercent(levels: List<WordLevel>): Double =
        percentOf(levels.sumOf { it.value }, Curriculum.WORDS_PER_LESSON)

    fun home(
        classes: List<SchoolClass>,
        words: List<Word>,
        progress: Map<Int, WordProgress>,
        highScores: Map<Int, Int>,
    ): HomeProgress {
        val levelOf = { word: Word -> progress[word.id]?.level ?: WordLevel.New }
        val wordsByLesson = words.groupBy { it.lessonId }
        val classProgress = classes.map { schoolClass ->
            val lessons = schoolClass.lessons.map { lesson ->
                val levels = wordsByLesson[lesson.id].orEmpty().map(levelOf)
                LessonProgress(lesson, lessonPercent(levels))
            }
            val classWords = schoolClass.lessons.flatMap { wordsByLesson[it.id].orEmpty() }
            ClassProgress(
                classId = schoolClass.id,
                percent = percentOf(classWords.sumOf { levelOf(it).value }, Curriculum.WORDS_PER_CLASS),
                learnedWordCount = classWords.count { levelOf(it).isLearned },
                highScore = highScores[schoolClass.id] ?: 0,
                lessons = lessons,
            )
        }
        val overall = if (classProgress.isEmpty()) 0.0 else classProgress.sumOf { it.percent } / classProgress.size
        return HomeProgress(overallPercent = overall, classes = classProgress)
    }

    private fun percentOf(levelSum: Int, wordCount: Int): Double =
        levelSum * 100.0 / (wordCount * WordLevel.MAX)
}

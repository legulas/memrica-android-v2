package app.enjoylearning504.core.domain

import app.enjoylearning504.core.domain.progress.ProgressCalculator
import app.enjoylearning504.core.model.Lesson
import app.enjoylearning504.core.model.PlantStage
import app.enjoylearning504.core.model.SchoolClass
import app.enjoylearning504.core.model.WordLevel
import app.enjoylearning504.core.model.WordProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCalculatorTest {
    private val classes = (1..7).map { c ->
        SchoolClass(c, (1..6).map { l ->
            val id = (c - 1) * 6 + l
            Lesson(id, c, l, (id - 1) * 12 + 1, id * 12)
        })
    }
    private val words = (1..504).map { word(it, lessonId = (it - 1) / 12 + 1, classId = (it - 1) / 72 + 1) }

    @Test
    fun `five learned words make a lesson 41_67 percent and a class 6_94 percent`() {
        val progress = (1..5).associateWith { WordProgress(it, WordLevel.Learned) }
        val home = ProgressCalculator.home(classes, words, progress, emptyMap())
        assertEquals(41.666, home.classes[0].lessons[0].percent, 0.01)
        assertEquals(6.944, home.classes[0].percent, 0.01)
        assertEquals(5, home.classes[0].learnedWordCount)
        assertEquals(0.99, home.overallPercent, 0.01)
    }

    @Test
    fun `one level of two words is 3 percent of a lesson`() {
        assertEquals(3.33, ProgressCalculator.lessonPercent(listOf(WordLevel(1), WordLevel(1))), 0.01)
    }

    @Test
    fun `plant stages use the original thresholds`() {
        assertEquals(PlantStage.Empty, PlantStage.forLessonPercent(0.9))
        assertEquals(PlantStage.Sprout, PlantStage.forLessonPercent(43.9))
        assertEquals(PlantStage.Growing, PlantStage.forLessonPercent(44.0))
        assertEquals(PlantStage.Growing, PlantStage.forLessonPercent(99.99))
        assertEquals(PlantStage.Flowering, PlantStage.forLessonPercent(100.0))
    }
}

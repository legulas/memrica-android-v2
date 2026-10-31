package app.enjoylearning504.core.domain

import app.enjoylearning504.core.domain.quiz.QuizBuilder
import app.enjoylearning504.core.domain.quiz.QuizMode
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizTest {
    @Test
    fun `quiz counts right and wrong answers and keeps the wrong words`() {
        val words = (1..5).map { word(it) }
        var quiz = QuizBuilder(Random(1)).build(QuizMode.Class(1), words, words)
        assertEquals(5, quiz.questions.size)
        quiz.questions.forEach { q ->
            assertEquals(4, q.options.distinctBy { it.id }.size)
            assertTrue(q.options.contains(q.word))
        }
        val first = quiz.current!!
        quiz = quiz.answer(first.options.first { it.id != first.word.id }.id)
        while (!quiz.isFinished) quiz = quiz.answer(quiz.current!!.word.id)
        assertEquals(4, quiz.rightCount)
        assertEquals(listOf(first.word), quiz.wrongWords)
    }
}

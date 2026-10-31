package app.enjoylearning504.core.domain

import app.enjoylearning504.core.domain.learning.Exercise
import app.enjoylearning504.core.domain.learning.LearningSessionEngine
import app.enjoylearning504.core.domain.learning.LearningState
import app.enjoylearning504.core.domain.learning.LearningStep
import app.enjoylearning504.core.model.WordLevel
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningSessionEngineTest {
    private val engine = LearningSessionEngine(Random(42))

    /** Answers whatever the current step asks, correctly. Returns the visited (wordId, exercise) pairs. */
    private fun playThrough(start: LearningState): Pair<LearningState, List<Pair<Int, Int>>> {
        var state = start
        val visited = mutableListOf<Pair<Int, Int>>()
        while (state.step !is LearningStep.Finished) {
            state = when (val step = state.step) {
                is LearningStep.Card -> { visited += step.word.id to 1; engine.onContinue(state).state }
                is LearningStep.Question -> { visited += step.word.id to step.exercise.level; engine.onAnswer(state, step.word.id).state }
                else -> error("unexpected $step")
            }
        }
        return state to visited
    }

    @Test
    fun `fresh lesson runs the original 20-slot order for the first four words`() {
        val state = engine.start(lessonWords())!!
        val (_, visited) = playThrough(state)
        val expectedSlots = listOf(
            1 to 1, 2 to 1, 1 to 2, 2 to 2, 1 to 3, 2 to 3, 3 to 1, 4 to 1, 3 to 2, 4 to 2,
            3 to 3, 4 to 3, 1 to 4, 2 to 4, 3 to 4, 4 to 4, 1 to 5, 2 to 5, 3 to 5, 4 to 5,
        )
        assertEquals(expectedSlots, visited)
    }

    @Test
    fun `levels already reached are skipped`() {
        val levels = listOf(3, 0, 5, 1) + List(8) { 0 }
        val state = engine.start(lessonWords(levels))!!
        val (_, visited) = playThrough(state)
        assertEquals(listOf(2 to 1, 2 to 2, 2 to 3, 4 to 2, 4 to 3, 1 to 4, 2 to 4, 4 to 4, 1 to 5, 2 to 5, 4 to 5), visited)
    }

    @Test
    fun `picks the first block that is not fully learned`() {
        val levels = List(4) { 5 } + List(8) { 0 }
        val state = engine.start(lessonWords(levels))!!
        assertEquals(listOf(5, 6, 7, 8), state.sessionWords.map { it.word.id })
    }

    @Test
    fun `fully learned lesson has no session`() {
        assertNull(engine.start(lessonWords(List(12) { 5 })))
    }

    @Test
    fun `completing a level reports it for saving and finishes with updated levels`() {
        var state = engine.start(lessonWords())!!
        val transition = engine.onContinue(state)
        assertEquals(1, transition.completed!!.wordId)
        assertEquals(WordLevel(1), transition.completed!!.level)
        state = playThrough(transition.state).first
        val finished = state.step as LearningStep.Finished
        assertTrue(finished.results.all { it.level == WordLevel.Learned })
        assertEquals(100, state.progressPercent)
    }

    @Test
    fun `wrong answer shows the card, then retries until correct, then re-asks the question`() {
        var state = engine.start(lessonWords(listOf(1, 1) + List(10) { 0 }))!!
        val question = state.step as LearningStep.Question
        assertEquals(Exercise.EnglishToPersian, question.exercise)
        val wrong = question.options.first { it.id != question.word.id }

        state = engine.onAnswer(state, wrong.id).state
        assertTrue(state.step is LearningStep.SecondChanceCard)
        state = engine.onContinue(state).state
        val retry = state.step as LearningStep.SecondChanceQuestion
        assertEquals(4, retry.options.size)

        // A wrong retry loops back to the card
        state = engine.onAnswer(state, retry.options.first { it.id != retry.word.id }.id).state
        assertTrue(state.step is LearningStep.SecondChanceCard)
        state = engine.onContinue(state).state
        val retry2 = state.step as LearningStep.SecondChanceQuestion

        val transition = engine.onAnswer(state, retry2.word.id)
        assertNull("a retry does not count as completing the level", transition.completed)
        val again = transition.state.step as LearningStep.Question
        assertEquals(question.word.id, again.word.id)
        assertEquals(Exercise.EnglishToPersian, again.exercise)
    }

    @Test
    fun `second mistake on the same word drops it from the session`() {
        var state = engine.start(lessonWords(listOf(1, 1, 0, 0) + List(8) { 0 }))!!
        repeat(2) {
            val q = state.step as LearningStep.Question
            assertEquals(1, q.word.id)
            state = engine.onAnswer(state, q.options.first { it.id != q.word.id }.id).state
            state = engine.onContinue(state).state
            state = engine.onAnswer(state, (state.step as LearningStep.SecondChanceQuestion).word.id).state
        }
        val (_, visited) = playThrough(state)
        assertTrue(visited.none { it.first == 1 })
    }

    @Test
    fun `options hold the answer and three other words of the lesson`() {
        val state = engine.start(lessonWords(listOf(1) + List(11) { 0 }))!!
        val q = state.step as LearningStep.Card
        val next = engine.onContinue(state).state.step as LearningStep.Question
        assertEquals(4, next.options.distinctBy { it.id }.size)
        assertTrue(next.options.any { it.id == next.word.id })
        assertTrue(next.options.all { it.lessonId == 1 })
        assertEquals(2, q.word.id)
    }
}

package app.enjoylearning504.core.domain.learning

import app.enjoylearning504.core.model.Word
import app.enjoylearning504.core.model.WordLevel
import app.enjoylearning504.core.model.WordWithProgress
import kotlin.random.Random

/** What the learner is asked to do in each of the five levels. */
enum class Exercise(val level: Int) {
    ShowCard(1),
    EnglishToPersian(2),
    PersianToEnglish(3),
    EnglishToPersianAgain(4),
    PersianToEnglishAgain(5);

    val asksPersianAnswer: Boolean get() = this == EnglishToPersian || this == EnglishToPersianAgain

    companion object {
        fun forLevel(level: Int): Exercise = entries.first { it.level == level }
    }
}

data class Slot(
    val wordIndex: Int,
    val exercise: Exercise,
    val done: Boolean = false,
    val blocked: Boolean = false,
)

/** What the screen should show right now. */
sealed interface LearningStep {
    data class Card(val word: Word, val level: WordLevel) : LearningStep

    data class Question(
        val word: Word,
        val level: WordLevel,
        val exercise: Exercise,
        val options: List<Word>,
    ) : LearningStep

    /** After a wrong answer: the word is shown again... */
    data class SecondChanceCard(val word: Word, val level: WordLevel) : LearningStep

    /** ...then asked once more (English to Persian) until answered correctly. */
    data class SecondChanceQuestion(val word: Word, val level: WordLevel, val options: List<Word>) : LearningStep

    data class Finished(val results: List<WordWithProgress>) : LearningStep
}

data class LearningState(
    val lessonWords: List<WordWithProgress>,
    val sessionWords: List<WordWithProgress>,
    val slots: List<Slot>,
    val mistakesPerWord: List<Int>,
    val totalMistakes: Int,
    val activeLength: Int,
    val step: LearningStep,
) {
    /** 0..100, the bar at the top of the learning screen. */
    val progressPercent: Int get() = slots.count { it.done } * (100 / SLOT_COUNT)

    companion object {
        const val SLOT_COUNT = 20
    }
}

/** A level the learner just completed; the caller persists it. */
data class LevelCompleted(val wordId: Int, val level: WordLevel)

data class Transition(val state: LearningState, val completed: LevelCompleted? = null)

/**
 * The learning algorithm of the original app, ported as a pure state machine.
 *
 * A session takes the first block of four words (in lesson order) that is not fully learned and
 * interleaves their five levels over 20 slots. Levels a word already reached are pre-marked done.
 * A wrong answer starts a second chance: the card again, then a retry question until correct, then
 * the session continues. A second mistake on the same word drops that word from the session, and
 * after more than four mistakes in total each further mistake also drops a slot from the end.
 */
class LearningSessionEngine(private val random: Random = Random.Default) {

    /** Returns null when every word of the lesson is already learned. */
    fun start(lessonWords: List<WordWithProgress>): LearningState? {
        val ordered = lessonWords.sortedBy { it.word.id }
        val block = ordered.chunked(BLOCK_SIZE)
            .firstOrNull { chunk -> chunk.size == BLOCK_SIZE && !chunk.all { it.level.isLearned } }
            ?: return null

        val slots = MutableList(LearningState.SLOT_COUNT) { Slot(wordIndex = -1, exercise = Exercise.ShowCard) }
        block.forEachIndexed { wordIndex, item ->
            SLOT_INDEXES[wordIndex].forEachIndexed { levelIndex, slotIndex ->
                val exercise = Exercise.entries[levelIndex]
                slots[slotIndex] = Slot(
                    wordIndex = wordIndex,
                    exercise = exercise,
                    done = item.level.value >= exercise.level,
                )
            }
        }
        val initial = LearningState(
            lessonWords = ordered,
            sessionWords = block,
            slots = slots,
            mistakesPerWord = List(BLOCK_SIZE) { 0 },
            totalMistakes = 0,
            activeLength = LearningState.SLOT_COUNT,
            step = LearningStep.Finished(emptyList()),
        )
        return initial.advance()
    }

    /** The learner tapped Continue on a word card. */
    fun onContinue(state: LearningState): Transition = when (val step = state.step) {
        is LearningStep.Card -> completeCurrentSlot(state)
        is LearningStep.SecondChanceCard -> {
            val word = step.word
            Transition(
                state.copy(
                    step = LearningStep.SecondChanceQuestion(
                        word = word,
                        level = levelOf(state, word),
                        options = optionsFor(state, word),
                    ),
                ),
            )
        }
        else -> Transition(state)
    }

    /** The learner picked [optionWordId] on a question. */
    fun onAnswer(state: LearningState, optionWordId: Int): Transition = when (val step = state.step) {
        is LearningStep.Question ->
            if (optionWordId == step.word.id) completeCurrentSlot(state) else Transition(wrongAnswer(state))
        is LearningStep.SecondChanceQuestion ->
            if (optionWordId == step.word.id) {
                Transition(state.advance())
            } else {
                Transition(state.copy(step = LearningStep.SecondChanceCard(step.word, levelOf(state, step.word))))
            }
        else -> Transition(state)
    }

    private fun completeCurrentSlot(state: LearningState): Transition {
        val index = nextSlotIndex(state.slots)
        if (index >= LearningState.SLOT_COUNT) return Transition(state)
        val slot = state.slots[index]
        val newLevel = WordLevel(slot.exercise.level)
        val sessionWords = state.sessionWords.toMutableList().also {
            it[slot.wordIndex] = it[slot.wordIndex].copy(level = newLevel)
        }
        val slots = state.slots.toMutableList().also { it[index] = slot.copy(done = true) }
        val wordId = state.sessionWords[slot.wordIndex].word.id
        val lessonWords = state.lessonWords.map { if (it.word.id == wordId) it.copy(level = newLevel) else it }
        val updated = state.copy(sessionWords = sessionWords, slots = slots, lessonWords = lessonWords)
        return Transition(updated.advance(), LevelCompleted(wordId, newLevel))
    }

    private fun wrongAnswer(state: LearningState): LearningState {
        val index = nextSlotIndex(state.slots)
        val wordIndex = state.slots[index].wordIndex
        val totalMistakes = state.totalMistakes + 1
        val slots = state.slots.toMutableList()
        var activeLength = state.activeLength

        if (totalMistakes > MISTAKES_BEFORE_SHORTENING) {
            // Drop the nearest not-yet-dropped slot from the end, as the original did
            for (offset in 1..MAX_TRAILING_LOOKBACK) {
                val candidate = activeLength - offset
                if (candidate < 0) break
                if (!slots[candidate].blocked) {
                    slots[candidate] = slots[candidate].copy(blocked = true)
                    activeLength -= offset
                    break
                }
            }
        }

        val mistakes = state.mistakesPerWord.toMutableList()
        val wordMistakes = mistakes[wordIndex] + 1
        if (wordMistakes < MISTAKES_BEFORE_DROPPING_WORD) {
            mistakes[wordIndex] = wordMistakes
        } else {
            SLOT_INDEXES[wordIndex].forEach { slots[it] = slots[it].copy(blocked = true) }
        }

        val word = state.sessionWords[wordIndex].word
        return state.copy(
            slots = slots,
            mistakesPerWord = mistakes,
            totalMistakes = totalMistakes,
            activeLength = activeLength,
            step = LearningStep.SecondChanceCard(word, state.sessionWords[wordIndex].level),
        )
    }

    private fun LearningState.advance(): LearningState {
        val index = nextSlotIndex(slots)
        if (index >= LearningState.SLOT_COUNT) {
            return copy(step = LearningStep.Finished(sessionWords))
        }
        val slot = slots[index]
        val item = sessionWords[slot.wordIndex]
        val step = if (slot.exercise == Exercise.ShowCard) {
            LearningStep.Card(item.word, item.level)
        } else {
            LearningStep.Question(item.word, item.level, slot.exercise, optionsFor(this, item.word))
        }
        return copy(step = step)
    }

    /** The correct word plus three other words of the same lesson, in random order. */
    private fun optionsFor(state: LearningState, word: Word): List<Word> {
        val distractors = state.lessonWords.map { it.word }.filter { it.id != word.id }.shuffled(random).take(3)
        return (distractors + word).shuffled(random)
    }

    private fun levelOf(state: LearningState, word: Word): WordLevel =
        state.sessionWords.first { it.word.id == word.id }.level

    companion object {
        const val BLOCK_SIZE = 4
        private const val MISTAKES_BEFORE_SHORTENING = 4
        private const val MISTAKES_BEFORE_DROPPING_WORD = 2
        private const val MAX_TRAILING_LOOKBACK = 6

        /** Slot positions of each of the four words' levels 1..5. */
        val SLOT_INDEXES: List<List<Int>> = listOf(
            listOf(0, 2, 4, 12, 16),
            listOf(1, 3, 5, 13, 17),
            listOf(6, 8, 10, 14, 18),
            listOf(7, 9, 11, 15, 19),
        )

        internal fun nextSlotIndex(slots: List<Slot>): Int {
            val index = slots.indexOfFirst { !it.blocked && !it.done }
            return if (index == -1) slots.size else index
        }
    }
}

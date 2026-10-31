package app.enjoylearning504.core.domain

import app.enjoylearning504.core.model.Word
import app.enjoylearning504.core.model.WordLevel
import app.enjoylearning504.core.model.WordWithProgress

fun word(id: Int, lessonId: Int = 1, classId: Int = 1) = Word(
    id = id,
    classId = classId,
    lessonId = lessonId,
    english = "en$id",
    persian = "fa$id",
    synonyms = null,
    phonetic = null,
    partOfSpeech = null,
    sentences = emptyList(),
    audioAsset = null,
)

fun lessonWords(levels: List<Int> = List(12) { 0 }, lessonId: Int = 1): List<WordWithProgress> =
    levels.mapIndexed { i, level ->
        WordWithProgress(word((lessonId - 1) * 12 + i + 1, lessonId), WordLevel(level), isDifficult = false)
    }

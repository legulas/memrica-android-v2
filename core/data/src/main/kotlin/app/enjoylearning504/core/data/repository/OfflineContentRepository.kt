package app.enjoylearning504.core.data.repository

import app.enjoylearning504.core.database.content.ContentDao
import app.enjoylearning504.core.database.content.LessonEntity
import app.enjoylearning504.core.database.content.WordEntity
import app.enjoylearning504.core.domain.repository.ContentRepository
import app.enjoylearning504.core.model.Lesson
import app.enjoylearning504.core.model.SchoolClass
import app.enjoylearning504.core.model.Sentence
import app.enjoylearning504.core.model.Word
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** The content never changes while the app runs, so it is read once and kept in memory. */
class OfflineContentRepository(private val dao: ContentDao) : ContentRepository {
    private val mutex = Mutex()
    private var cache: Content? = null

    override suspend fun classes(): List<SchoolClass> = content().classes
    override suspend fun lesson(lessonId: Int): Lesson = content().lessons.getValue(lessonId)
    override suspend fun words(): List<Word> = content().words
    override suspend fun word(wordId: Int): Word = content().wordsById.getValue(wordId)

    private suspend fun content(): Content = mutex.withLock {
        cache ?: run {
            val lessons = dao.lessons().map { it.toModel() }
            val classes = dao.classes().map { c -> SchoolClass(c.id, lessons.filter { it.classId == c.id }) }
            val words = dao.words().map { it.toModel() }
            Content(classes, lessons.associateBy { it.id }, words, words.associateBy { it.id })
        }.also { cache = it }
    }

    private class Content(
        val classes: List<SchoolClass>,
        val lessons: Map<Int, Lesson>,
        val words: List<Word>,
        val wordsById: Map<Int, Word>,
    )
}

internal fun LessonEntity.toModel() = Lesson(id, classId, numberInClass, firstWordId, lastWordId)

internal fun WordEntity.toModel() = Word(
    id = id,
    classId = classId,
    lessonId = lessonId,
    english = english,
    persian = persian,
    synonyms = synonyms,
    phonetic = phonetic,
    partOfSpeech = partOfSpeech,
    sentences = listOfNotNull(
        sentence(sentenceEn1, sentenceFa1),
        sentence(sentenceEn2, sentenceFa2),
        sentence(sentenceEn3, sentenceFa3),
    ),
    audioAsset = audioAsset,
)

private fun sentence(en: String?, fa: String?): Sentence? =
    if (en.isNullOrBlank()) null else Sentence(en, fa.orEmpty())

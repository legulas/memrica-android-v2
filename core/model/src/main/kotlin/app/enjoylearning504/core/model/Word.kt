package app.enjoylearning504.core.model

/** One of the 504 words. Content only: it never changes at runtime. */
data class Word(
    val id: Int,
    val classId: Int,
    val lessonId: Int,
    val english: String,
    val persian: String,
    val synonyms: String?,
    val phonetic: String?,
    val partOfSpeech: String?,
    val sentences: List<Sentence>,
    val audioAsset: String?,
)

data class Sentence(val english: String, val persian: String)

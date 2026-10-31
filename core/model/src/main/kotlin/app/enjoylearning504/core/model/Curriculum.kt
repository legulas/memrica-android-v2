package app.enjoylearning504.core.model

/** The fixed shape of the course: 7 classes, 6 lessons each, 12 words per lesson. */
object Curriculum {
    const val CLASS_COUNT = 7
    const val LESSONS_PER_CLASS = 6
    const val WORDS_PER_LESSON = 12
    const val WORDS_PER_CLASS = LESSONS_PER_CLASS * WORDS_PER_LESSON
    const val TOTAL_WORDS = CLASS_COUNT * WORDS_PER_CLASS
}

data class Lesson(
    val id: Int,
    val classId: Int,
    /** 1..6 inside its class. */
    val numberInClass: Int,
    val firstWordId: Int,
    val lastWordId: Int,
)

data class SchoolClass(val id: Int, val lessons: List<Lesson>)

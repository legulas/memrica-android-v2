package app.enjoylearning504.core.model

/** Progress figures shown across the app. Percentages are 0..100. */
data class LessonProgress(
    val lesson: Lesson,
    val percent: Double,
) {
    val stage: PlantStage get() = PlantStage.forLessonPercent(percent)
}

data class ClassProgress(
    val classId: Int,
    val percent: Double,
    val learnedWordCount: Int,
    val highScore: Int,
    val lessons: List<LessonProgress>,
)

data class HomeProgress(
    val overallPercent: Double,
    val classes: List<ClassProgress>,
)

/** The pot drawn on a lesson card. The thresholds match the original app. */
enum class PlantStage {
    Empty, Sprout, Growing, Flowering;

    companion object {
        fun forLessonPercent(percent: Double): PlantStage {
            val whole = percent.toInt()
            return when {
                whole <= 0 -> Empty
                whole < 44 -> Sprout
                whole < 100 -> Growing
                else -> Flowering
            }
        }
    }
}

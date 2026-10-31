package app.enjoylearning504.feature.quiz.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.enjoylearning504.core.domain.quiz.QuizMode
import app.enjoylearning504.feature.quiz.QuizRoute
import kotlinx.serialization.Serializable

/** Navigation arguments must be primitives, so the mode is flattened. */
@Serializable
data class QuizDestination(val kind: Kind, val id: Int) {
    @Serializable
    enum class Kind { Lesson, Class, Difficult }

    fun toMode(): QuizMode = when (kind) {
        Kind.Lesson -> QuizMode.Lesson(id)
        Kind.Class -> QuizMode.Class(id)
        Kind.Difficult -> QuizMode.Difficult(id)
    }
}

fun NavController.navigateToQuiz(mode: QuizMode) = navigate(
    when (mode) {
        is QuizMode.Lesson -> QuizDestination(QuizDestination.Kind.Lesson, mode.lessonId)
        is QuizMode.Class -> QuizDestination(QuizDestination.Kind.Class, mode.classId)
        is QuizMode.Difficult -> QuizDestination(QuizDestination.Kind.Difficult, mode.classId)
    },
)

fun NavGraphBuilder.quizScreen(onExit: () -> Unit) {
    composable<QuizDestination> { QuizRoute(onExit) }
}

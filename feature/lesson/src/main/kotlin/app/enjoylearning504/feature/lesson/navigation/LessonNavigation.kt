package app.enjoylearning504.feature.lesson.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.enjoylearning504.feature.lesson.LessonRoute
import kotlinx.serialization.Serializable

@Serializable
data class LessonDestination(val lessonId: Int)

fun NavController.navigateToLesson(lessonId: Int) = navigate(LessonDestination(lessonId))

fun NavGraphBuilder.lessonScreen(
    onLearn: (Int) -> Unit,
    onFastReview: (Int) -> Unit,
    onWordClick: (Int) -> Unit,
) {
    composable<LessonDestination> { LessonRoute(onLearn, onFastReview, onWordClick) }
}

package app.enjoylearning504.feature.learning.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.enjoylearning504.feature.learning.LearningRoute
import kotlinx.serialization.Serializable

@Serializable
data class LearningDestination(val lessonId: Int)

fun NavController.navigateToLearning(lessonId: Int) = navigate(LearningDestination(lessonId))

fun NavGraphBuilder.learningScreen(onExit: () -> Unit) {
    composable<LearningDestination> { LearningRoute(onExit) }
}

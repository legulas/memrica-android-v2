package app.enjoylearning504.feature.difficult.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.enjoylearning504.feature.difficult.DifficultWordsRoute
import kotlinx.serialization.Serializable

@Serializable
data class DifficultWordsDestination(val classId: Int)

fun NavController.navigateToDifficultWords(classId: Int) = navigate(DifficultWordsDestination(classId))

fun NavGraphBuilder.difficultWordsScreen(onFastReview: (Int) -> Unit, onWordClick: (Int) -> Unit) {
    composable<DifficultWordsDestination> { DifficultWordsRoute(onFastReview, onWordClick) }
}

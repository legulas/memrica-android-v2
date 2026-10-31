package app.enjoylearning504.feature.word.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.enjoylearning504.feature.word.WordRoute
import kotlinx.serialization.Serializable

@Serializable
data class WordDestination(val wordId: Int)

fun NavController.navigateToWord(wordId: Int) = navigate(WordDestination(wordId))

fun NavGraphBuilder.wordScreen() {
    composable<WordDestination> { WordRoute() }
}

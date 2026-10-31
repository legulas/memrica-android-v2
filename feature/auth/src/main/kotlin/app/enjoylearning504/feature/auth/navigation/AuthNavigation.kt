package app.enjoylearning504.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.enjoylearning504.feature.auth.AuthRoute
import kotlinx.serialization.Serializable

@Serializable
data object AuthPromptDestination

fun NavGraphBuilder.authPromptScreen(onDone: () -> Unit) {
    composable<AuthPromptDestination> { AuthRoute(onDone) }
}

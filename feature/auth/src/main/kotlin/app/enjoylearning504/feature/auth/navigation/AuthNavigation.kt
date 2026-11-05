package app.enjoylearning504.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import app.enjoylearning504.feature.auth.AuthRoute
import kotlinx.serialization.Serializable

/** [upgradedFromOldApp]: pitch saving existing progress instead of showing a plain sign-in. */
@Serializable
data class AuthPromptDestination(val upgradedFromOldApp: Boolean)

fun NavGraphBuilder.authPromptScreen(onDone: () -> Unit) {
    composable<AuthPromptDestination> { entry ->
        AuthRoute(upgradedFromOldApp = entry.toRoute<AuthPromptDestination>().upgradedFromOldApp, onDone = onDone)
    }
}

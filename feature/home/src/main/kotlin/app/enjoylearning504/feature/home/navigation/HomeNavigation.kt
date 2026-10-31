package app.enjoylearning504.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.enjoylearning504.feature.home.HomeActions
import app.enjoylearning504.feature.home.HomeRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination

fun NavGraphBuilder.homeScreen(actions: HomeActions) {
    composable<HomeDestination> { HomeRoute(actions) }
}

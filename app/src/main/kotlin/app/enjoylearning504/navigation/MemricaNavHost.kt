package app.enjoylearning504.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import app.enjoylearning504.core.domain.quiz.QuizMode
import app.enjoylearning504.core.domain.usecase.StartDestination
import app.enjoylearning504.feature.auth.navigation.AuthPromptDestination
import app.enjoylearning504.feature.auth.navigation.authPromptScreen
import app.enjoylearning504.feature.difficult.navigation.difficultWordsScreen
import app.enjoylearning504.feature.difficult.navigation.navigateToDifficultWords
import app.enjoylearning504.feature.home.HomeActions
import app.enjoylearning504.feature.home.navigation.HomeDestination
import app.enjoylearning504.feature.home.navigation.homeScreen
import app.enjoylearning504.feature.info.navigation.HelpDestination
import app.enjoylearning504.feature.info.navigation.InfoDestination
import app.enjoylearning504.feature.info.navigation.PrivacyNoticeDestination
import app.enjoylearning504.feature.info.navigation.ShareDestination
import app.enjoylearning504.feature.info.navigation.ShopDestination
import app.enjoylearning504.feature.info.navigation.SmartLearningDestination
import app.enjoylearning504.feature.info.navigation.infoDialogs
import app.enjoylearning504.feature.learning.navigation.learningScreen
import app.enjoylearning504.feature.learning.navigation.navigateToLearning
import app.enjoylearning504.feature.lesson.navigation.lessonScreen
import app.enjoylearning504.feature.lesson.navigation.navigateToLesson
import app.enjoylearning504.feature.quiz.navigation.navigateToQuiz
import app.enjoylearning504.feature.quiz.navigation.quizScreen
import app.enjoylearning504.feature.word.navigation.navigateToWord
import app.enjoylearning504.feature.word.navigation.wordScreen

@Composable
fun MemricaNavHost(start: StartDestination, navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = when (start) {
            is StartDestination.AuthPrompt -> AuthPromptDestination(start.upgradedFromOldApp)
            StartDestination.Home -> HomeDestination
        },
        // New screens slide in from the reading-direction end; going back reverses it
        enterTransition = { slideIntoContainer(SlideDirection.Start, tween(NAV_MS)) { it / 5 } + fadeIn(tween(NAV_MS)) },
        exitTransition = { slideOutOfContainer(SlideDirection.Start, tween(NAV_MS)) { it / 5 } + fadeOut(tween(NAV_MS)) },
        popEnterTransition = { slideIntoContainer(SlideDirection.End, tween(NAV_MS)) { it / 5 } + fadeIn(tween(NAV_MS)) },
        popExitTransition = { slideOutOfContainer(SlideDirection.End, tween(NAV_MS)) { it / 5 } + fadeOut(tween(NAV_MS)) },
    ) {
        authPromptScreen(onDone = {
            navController.navigate(HomeDestination) {
                popUpTo<AuthPromptDestination> { inclusive = true }
            }
        })
        homeScreen(
            HomeActions(
                onLessonClick = navController::navigateToLesson,
                onClassQuizClick = { navController.navigateToQuiz(QuizMode.Class(it)) },
                onDifficultWordsClick = navController::navigateToDifficultWords,
                onInfoClick = { navController.navigate(InfoDestination) },
                onSmartLearningClick = { navController.navigate(SmartLearningDestination) },
                onShareClick = { navController.navigate(ShareDestination) },
                onShopClick = { navController.navigate(ShopDestination) },
                onHelpClick = { navController.navigate(HelpDestination) },
                onPrivacyNotice = { navController.navigate(PrivacyNoticeDestination) { launchSingleTop = true } },
            ),
        )
        lessonScreen(
            onLearn = navController::navigateToLearning,
            onFastReview = { navController.navigateToQuiz(QuizMode.Lesson(it)) },
            onWordClick = navController::navigateToWord,
        )
        wordScreen()
        learningScreen(onExit = { navController.popBackStack() })
        quizScreen(onExit = { navController.popBackStack() })
        difficultWordsScreen(
            onFastReview = { navController.navigateToQuiz(QuizMode.Difficult(it)) },
            onWordClick = navController::navigateToWord,
        )
        infoDialogs(onClose = { navController.popBackStack() })
    }
}

private const val NAV_MS = 300

package app.enjoylearning504.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.enjoylearning504.core.designsystem.R as DesignR
import app.enjoylearning504.core.designsystem.component.PlantStageIcon
import app.enjoylearning504.core.designsystem.component.PrimaryButton
import app.enjoylearning504.core.designsystem.component.ProgressBar
import app.enjoylearning504.core.designsystem.component.ProgressRing
import app.enjoylearning504.core.designsystem.component.SurfaceCard
import app.enjoylearning504.core.designsystem.theme.LogoFont
import app.enjoylearning504.core.designsystem.theme.MemricaColors
import app.enjoylearning504.core.model.ClassProgress
import app.enjoylearning504.core.model.LessonProgress
import app.enjoylearning504.core.ui.R as UiR
import app.enjoylearning504.core.ui.localizedNumber
import app.enjoylearning504.core.ui.localizedPercent
import org.koin.compose.viewmodel.koinViewModel

data class HomeActions(
    val onLessonClick: (lessonId: Int) -> Unit,
    val onClassQuizClick: (classId: Int) -> Unit,
    val onDifficultWordsClick: (classId: Int) -> Unit,
    val onInfoClick: () -> Unit,
    val onSmartLearningClick: () -> Unit,
    val onShareClick: () -> Unit,
    val onShopClick: () -> Unit,
    val onHelpClick: () -> Unit,
    val onPrivacyNotice: () -> Unit,
)

@Composable
fun HomeRoute(actions: HomeActions, viewModel: HomeViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        actions = actions.copy(
            onClassQuizClick = { if (viewModel.canStartClassQuiz(it)) actions.onClassQuizClick(it) },
            onPrivacyNotice = {
                viewModel.onPrivacyNoticeDismissed()
                actions.onPrivacyNotice()
            },
        ),
        onMessageShown = viewModel::onMessageShown,
    )
}

@Composable
internal fun HomeScreen(state: HomeUiState, actions: HomeActions, onMessageShown: () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    val ready = state as? HomeUiState.Ready
    val needFive = stringResource(R.string.home_need_five_learned)

    LaunchedEffect(ready?.message) {
        if (ready?.message == HomeMessage.NeedFiveLearnedWords) {
            snackbar.showSnackbar(needFive)
            onMessageShown()
        }
    }
    LaunchedEffect(ready?.showPrivacyNotice) {
        if (ready?.showPrivacyNotice == true) actions.onPrivacyNotice()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when (state) {
            HomeUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            is HomeUiState.Ready -> HomeContent(state, actions, PaddingValues(bottom = 24.dp), Modifier.padding(padding))
        }
    }
}

@Composable
private fun HomeContent(state: HomeUiState.Ready, actions: HomeActions, contentPadding: PaddingValues, modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { TopBar(state.completedLessons, actions) }
        item { OverallCard(state, Modifier.widthIn(max = 600.dp).padding(horizontal = 16.dp)) }
        item {
            PrimaryButton(
                text = stringResource(R.string.home_help_improve),
                onClick = actions.onHelpClick,
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item {
            Text(
                stringResource(R.string.home_class_list),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp).semantics { heading() },
            )
        }
        items(state.progress.classes, key = { it.classId }) { classProgress ->
            ClassSection(classProgress, actions, Modifier.widthIn(max = 600.dp).padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun TopBar(completedLessons: Int, actions: HomeActions) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TopIcon(DesignR.drawable.ic_info, R.string.home_info, actions.onInfoClick)
        TopIcon(DesignR.drawable.ic_brain, R.string.home_smart_learning, actions.onSmartLearningClick)
        Spacer(Modifier.weight(1f))
        Logo(completedLessons)
        Spacer(Modifier.weight(1f))
        TopIcon(DesignR.drawable.ic_share, R.string.home_share, actions.onShareClick, inCircle = true)
        TopIcon(DesignR.drawable.ic_shop, R.string.home_shop, actions.onShopClick, inCircle = true)
    }
}

@Composable
private fun TopIcon(icon: Int, label: Int, onClick: () -> Unit, inCircle: Boolean = false) {
    IconButton(onClick = onClick, modifier = Modifier.size(56.dp)) {
        // The share and shop glyphs are white and sit on a dark teal circle, as in the original
        val circle = if (inCircle) Modifier.background(MemricaColors.TealDeep, CircleShape).padding(7.dp) else Modifier
        Image(painterResource(icon), contentDescription = stringResource(label), modifier = Modifier.size(36.dp).then(circle))
    }
}

/** The shield logo; its number is how many lessons are fully learned. */
@Composable
private fun Logo(completedLessons: Int) {
    val description = stringResource(R.string.home_completed_lessons, localizedNumber(completedLessons))
    Box(Modifier.size(64.dp).semantics(mergeDescendants = true) {}, contentAlignment = Alignment.Center) {
        Image(painterResource(DesignR.drawable.ic_1), contentDescription = description, modifier = Modifier.size(64.dp))
        Image(painterResource(DesignR.drawable.ic_2), contentDescription = null, modifier = Modifier.size(57.dp))
        Image(painterResource(DesignR.drawable.ic_3), contentDescription = null, modifier = Modifier.size(51.dp))
        Image(
            painterResource(DesignR.drawable.ic_star_yellow),
            contentDescription = null,
            modifier = Modifier.size(14.dp).align(Alignment.TopCenter).padding(top = 0.dp),
        )
        Text(
            localizedNumber(completedLessons),
            color = Color.White,
            fontFamily = LogoFont,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun OverallCard(state: HomeUiState.Ready, modifier: Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .background(MemricaColors.Teal, MaterialTheme.shapes.extraLarge)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.home_overall, localizedPercent(state.progress.overallPercent)),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(16.dp))
        ProgressBar(state.progress.overallPercent.toFloat())
        Spacer(Modifier.height(32.dp))
        Text(stringResource(R.string.home_class_progress), color = Color.White, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            state.progress.classes.forEach { c ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ProgressRing(c.percent.toFloat(), localizedPercent(c.percent))
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier.size(28.dp).background(MemricaColors.TealDeep, MaterialTheme.shapes.extraLarge),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(localizedNumber(c.classId), color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassSection(classProgress: ClassProgress, actions: HomeActions, modifier: Modifier) {
    Column(modifier.padding(bottom = 32.dp)) {
        SurfaceCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(UiR.string.class_n, localizedNumber(classProgress.classId)),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f).semantics { heading() },
                    )
                    ProgressRing(
                        percent = classProgress.percent.toFloat(),
                        label = localizedPercent(classProgress.percent),
                        size = 48.dp,
                        trackColor = MemricaColors.Divider,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryButton(
                        text = stringResource(R.string.home_class_quiz),
                        onClick = { actions.onClassQuizClick(classProgress.classId) },
                        color = MemricaColors.Teal,
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.labelMedium,
                    )
                    PrimaryButton(
                        text = stringResource(R.string.home_class_difficult),
                        onClick = { actions.onDifficultWordsClick(classProgress.classId) },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
        classProgress.lessons.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { lesson ->
                    LessonCard(lesson, { actions.onLessonClick(lesson.lesson.id) }, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LessonCard(lesson: LessonProgress, onClick: () -> Unit, modifier: Modifier) {
    SurfaceCard(modifier, onClick = onClick) {
        Column(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            PlantStageIcon(lesson.stage, Modifier.size(56.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(UiR.string.lesson_n, localizedNumber(lesson.lesson.id)),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(
                    UiR.string.lesson_words_range,
                    localizedNumber(lesson.lesson.firstWordId),
                    localizedNumber(lesson.lesson.lastWordId),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

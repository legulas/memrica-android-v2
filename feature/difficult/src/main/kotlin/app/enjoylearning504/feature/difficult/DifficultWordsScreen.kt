package app.enjoylearning504.feature.difficult

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.enjoylearning504.core.ads.AdPlacement
import app.enjoylearning504.core.ads.NativeAdBanner
import app.enjoylearning504.core.designsystem.R as DesignR
import app.enjoylearning504.core.designsystem.component.PrimaryButton
import app.enjoylearning504.core.designsystem.component.SurfaceCard
import app.enjoylearning504.core.designsystem.component.WordLevelIcon
import app.enjoylearning504.core.designsystem.theme.MemricaColors
import app.enjoylearning504.core.domain.quiz.QuizBuilder
import app.enjoylearning504.core.model.WordWithProgress
import app.enjoylearning504.core.ui.OfflineGate
import app.enjoylearning504.core.ui.localizedNumber
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DifficultWordsRoute(
    onFastReview: (classId: Int) -> Unit,
    onWordClick: (wordId: Int) -> Unit,
    viewModel: DifficultWordsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OfflineGate()
    DifficultWordsScreen(
        state = state,
        onFastReview = { if (viewModel.canStartReview()) onFastReview(viewModel.classId) },
        onWordClick = onWordClick,
        onRemove = viewModel::remove,
        onMessageShown = viewModel::onMessageShown,
    )
}

@Composable
internal fun DifficultWordsScreen(
    state: DifficultWordsUiState,
    onFastReview: () -> Unit,
    onWordClick: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    val minimum = stringResource(R.string.difficult_min_words, localizedNumber(QuizBuilder.MIN_WORDS))
    LaunchedEffect(state.showMinimumMessage) {
        if (state.showMinimumMessage) {
            snackbar.showSnackbar(minimum)
            onMessageShown()
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, containerColor = MaterialTheme.colorScheme.background) { padding ->
        val words = state.words
        if (words == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(horizontal = 16.dp)) {
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 16.dp)
                            .background(MemricaColors.Yellow, MaterialTheme.shapes.large)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.difficult_title, localizedNumber(state.classId)),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(localizedNumber(words.size), color = Color.White, style = MaterialTheme.typography.titleLarge)
                    }
                }
                item { NativeAdBanner(AdPlacement.NativeDifficultWords, Modifier.fillMaxWidth().padding(vertical = 8.dp)) }
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        PrimaryButton(
                            stringResource(R.string.difficult_fast_review),
                            onFastReview,
                            Modifier.fillMaxWidth(0.5f),
                            color = MemricaColors.Teal,
                        )
                    }
                }
                if (words.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.difficult_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                        )
                    }
                }
                items(words, key = { it.word.id }) { item -> DifficultRow(item, onWordClick, onRemove) }
            }
        }
    }
}

@Composable
private fun DifficultRow(item: WordWithProgress, onWordClick: (Int) -> Unit, onRemove: (Int) -> Unit) {
    SurfaceCard(Modifier.fillMaxWidth().padding(bottom = 6.dp), onClick = { onWordClick(item.word.id) }) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            WordLevelIcon(item.level, Modifier.size(48.dp))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(item.word.english, style = MaterialTheme.typography.titleMedium.copy(textDirection = TextDirection.Ltr))
                Text(item.word.persian, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { onRemove(item.word.id) }) {
                Icon(
                    painterResource(DesignR.drawable.ic_is_difficult),
                    contentDescription = stringResource(R.string.difficult_remove),
                    tint = Color.Unspecified,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

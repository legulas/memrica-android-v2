package app.enjoylearning504.feature.word

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.enjoylearning504.core.ads.AdPlacement
import app.enjoylearning504.core.ads.NativeAdBanner
import app.enjoylearning504.core.model.WordWithProgress
import app.enjoylearning504.core.ui.SentencePager
import app.enjoylearning504.core.ui.WordCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WordRoute(viewModel: WordViewModel = koinViewModel()) {
    val word by viewModel.word.collectAsStateWithLifecycle()
    WordScreen(word, viewModel::playWord, viewModel::speak)
}

@Composable
internal fun WordScreen(item: WordWithProgress?, onPlay: () -> Unit, onSpeak: (String) -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        if (item == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 600.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            ) {
                WordCard(item.word, item.level, onPlay, Modifier.padding(top = 24.dp))
                NativeAdBanner(AdPlacement.NativeWord, Modifier.fillMaxWidth().padding(vertical = 12.dp))
                SentencePager(item.word.sentences, onSpeak, stringResource(R.string.word_read_sentence))
            }
        }
    }
}

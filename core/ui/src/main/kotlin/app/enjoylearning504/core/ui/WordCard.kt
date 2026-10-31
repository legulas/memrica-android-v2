package app.enjoylearning504.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.enjoylearning504.core.designsystem.R as DesignR
import app.enjoylearning504.core.designsystem.component.SurfaceCard
import app.enjoylearning504.core.designsystem.component.WordLevelIcon
import app.enjoylearning504.core.model.Word
import app.enjoylearning504.core.model.WordLevel

/** The word card shared by the word screen and learning mode: audio, plant, word, meaning, synonyms. */
@Composable
fun WordCard(word: Word, level: WordLevel, onPlay: () -> Unit, modifier: Modifier = Modifier) {
    SurfaceCard(modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().padding(16.dp)) {
            IconButton(onClick = onPlay, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(
                    painterResource(DesignR.drawable.ic_sound_gray_24dp),
                    contentDescription = stringResource(R.string.play_pronunciation),
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp),
                )
            }
            WordLevelIcon(level, Modifier.size(44.dp).align(Alignment.TopStart))
            Column(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    word.english,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        textDirection = TextDirection.Ltr,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                HorizontalDivider(Modifier.fillMaxWidth(0.6f).padding(vertical = 12.dp))
                Text(word.persian, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                word.synonyms?.let {
                    HorizontalDivider(Modifier.fillMaxWidth(0.6f).padding(vertical = 12.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

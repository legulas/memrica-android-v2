package app.enjoylearning504.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import app.enjoylearning504.core.designsystem.R as DesignR
import app.enjoylearning504.core.designsystem.component.SurfaceCard
import app.enjoylearning504.core.designsystem.theme.MemricaColors
import app.enjoylearning504.core.model.Sentence

/** The example sentences with numbered tabs. Tapping a sentence reads it aloud. */
@Composable
fun SentencePager(
    sentences: List<Sentence>,
    onSpeak: (String) -> Unit,
    readAloudLabel: String,
    modifier: Modifier = Modifier,
) {
    if (sentences.isEmpty()) return
    var selected by rememberSaveable(sentences) { mutableIntStateOf(0) }
    val sentence = sentences[selected.coerceIn(sentences.indices)]
    Column(modifier) {
        SurfaceCard(Modifier.fillMaxWidth(), onClick = { onSpeak(sentence.english) }) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    sentence.english,
                    style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(Modifier.weight(1f))
                    Icon(
                        painterResource(DesignR.drawable.ic_sound_gray_24dp),
                        contentDescription = readAloudLabel,
                        tint = Color.Unspecified,
                        modifier = Modifier.padding(horizontal = 8.dp).size(18.dp),
                    )
                    HorizontalDivider(Modifier.weight(1f))
                }
                Text(
                    sentence.persian,
                    style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Rtl),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            sentences.indices.forEach { index ->
                SurfaceCard(Modifier.weight(1f), onClick = { selected = index }) {
                    Text(
                        localizedNumber(index + 1),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (index == selected) MemricaColors.Yellow else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    }
}

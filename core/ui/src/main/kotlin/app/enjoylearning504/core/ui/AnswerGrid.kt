package app.enjoylearning504.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import app.enjoylearning504.core.designsystem.component.OptionCard
import app.enjoylearning504.core.designsystem.component.OptionState
import app.enjoylearning504.core.designsystem.component.WordLevelIcon
import app.enjoylearning504.core.designsystem.theme.MemricaColors
import app.enjoylearning504.core.model.Word
import app.enjoylearning504.core.model.WordLevel
import kotlinx.coroutines.delay

/**
 * A word to translate and four answers. After a tap the choice is shown green or red (with the
 * right answer in green), then [onAnswered] runs. [questionKey] resets the feedback for a new question.
 * [onChosen] runs right at the tap, for sound; the tap also gives a confirm or reject vibration.
 */
@Composable
fun AnswerGrid(
    questionKey: Any,
    word: Word,
    options: List<Word>,
    askInEnglish: Boolean,
    level: WordLevel?,
    onAnswered: (optionWordId: Int) -> Unit,
    modifier: Modifier = Modifier,
    onChosen: (correct: Boolean) -> Unit = {},
) {
    val haptics = LocalHapticFeedback.current
    var chosen by remember(questionKey) { mutableStateOf<Int?>(null) }
    LaunchedEffect(questionKey, chosen) {
        val picked = chosen ?: return@LaunchedEffect
        delay(if (picked == word.id) CORRECT_DELAY_MS else WRONG_DELAY_MS)
        onAnswered(picked)
    }
    Column(modifier) {
        Row(Modifier.fillMaxWidth().padding(vertical = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (askInEnglish) word.english else word.persian,
                style = MaterialTheme.typography.headlineMedium.copy(
                    textDirection = if (askInEnglish) TextDirection.Ltr else TextDirection.Rtl,
                ),
                color = MemricaColors.Teal,
                modifier = Modifier.weight(1f),
            )
            if (level != null) WordLevelIcon(level, Modifier.size(52.dp))
        }
        options.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { option ->
                    val state = when {
                        chosen == null -> OptionState.Idle
                        option.id == word.id -> OptionState.Correct
                        option.id == chosen -> OptionState.Wrong
                        else -> OptionState.Idle
                    }
                    OptionCard(
                        text = if (askInEnglish) option.persian else option.english,
                        state = state,
                        enabled = chosen == null,
                        onClick = {
                            val correct = option.id == word.id
                            chosen = option.id
                            haptics.performHapticFeedback(if (correct) HapticFeedbackType.Confirm else HapticFeedbackType.Reject)
                            onChosen(correct)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

private const val CORRECT_DELAY_MS = 450L
private const val WRONG_DELAY_MS = 900L

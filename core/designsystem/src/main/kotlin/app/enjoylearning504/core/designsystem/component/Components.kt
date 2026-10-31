package app.enjoylearning504.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.enjoylearning504.core.designsystem.theme.LocalAnswerColors
import app.enjoylearning504.core.designsystem.theme.MemricaColors

/** The big yellow call-to-action button used across the app. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MemricaColors.Yellow,
    enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 52.dp),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
    ) {
        Text(text, style = textStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** A thin rounded progress bar; [percent] is 0..100. */
@Composable
fun ProgressBar(
    percent: Float,
    modifier: Modifier = Modifier,
    color: Color = MemricaColors.Yellow,
    trackColor: Color = MemricaColors.TealDeep,
) {
    LinearProgressIndicator(
        progress = { (percent / 100f).coerceIn(0f, 1f) },
        modifier = modifier.fillMaxWidth().height(8.dp).clip(MaterialTheme.shapes.small),
        color = color,
        trackColor = trackColor,
        drawStopIndicator = {},
        gapSize = 0.dp,
    )
}

/** A ring with the percentage in the middle; [label] is already localized. */
@Composable
fun ProgressRing(
    percent: Float,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    color: Color = MemricaColors.Yellow,
    trackColor: Color = Color.White.copy(alpha = 0.6f),
    labelColor: Color = Color.White,
) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { (percent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.size(size),
            color = color,
            trackColor = trackColor,
            strokeWidth = 4.dp,
            gapSize = 0.dp,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = labelColor)
    }
}

enum class OptionState { Idle, Correct, Wrong }

/** One of the four answer cards in learning and quiz questions. */
@Composable
fun OptionCard(
    text: String,
    state: OptionState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val answers = LocalAnswerColors.current
    val container by animateColorAsState(
        when (state) {
            OptionState.Idle -> MaterialTheme.colorScheme.surface
            OptionState.Correct -> answers.correct
            OptionState.Wrong -> answers.wrong
        },
        label = "optionColor",
    )
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 140.dp).semantics { role = Role.Button },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = container,
            disabledContainerColor = container,
            contentColor = if (state == OptionState.Idle) MaterialTheme.colorScheme.onSurface else Color.White,
            disabledContentColor = if (state == OptionState.Idle) MaterialTheme.colorScheme.onSurface else Color.White,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(Modifier.fillMaxWidth().heightIn(min = 140.dp).padding(12.dp), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        }
    }
}

/** White rounded card used for list rows and panels. */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors, elevation = elevation) { content() }
    } else {
        Card(modifier = modifier, shape = shape, colors = colors, elevation = elevation) { content() }
    }
}

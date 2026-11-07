package app.enjoylearning504.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.enjoylearning504.core.designsystem.theme.MemricaColors

/**
 * A one-time code entered into a row of [length] boxes. The caller keeps [value] to digits.
 * Boxes always run left to right, also in RTL layouts, because codes are read that way.
 * Marked as an SMS OTP field so the autofill service and keyboard can offer the code.
 * Digits pop into their boxes, and the row shakes when [isError] turns on.
 */
@Composable
fun OtpField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    isError: Boolean = false,
    enabled: Boolean = true,
    autoFocus: Boolean = false,
    onDone: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    if (autoFocus) LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val motion = rememberFeedbackMotion()
    LaunchedEffect(isError) { if (isError) motion.shake() }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BasicTextField(
            // The cursor stays at the end: digits are added and removed from the right only
            value = TextFieldValue(value, TextRange(value.length)),
            onValueChange = { onValueChange(it.text) },
            enabled = enabled,
            singleLine = true,
            cursorBrush = SolidColor(Color.Transparent),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = modifier
                .feedbackMotion(motion)
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { focused = it.isFocused }
                .semantics { contentType = ContentType.SmsOtpCode },
            decorationBox = {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    repeat(length) { index ->
                        val active = focused && index == value.length.coerceAtMost(length - 1)
                        OtpCell(
                            digit = value.getOrNull(index),
                            borderColor = when {
                                isError -> MaterialTheme.colorScheme.error
                                active -> MemricaColors.Teal
                                else -> MemricaColors.Divider
                            },
                            borderWidth = if (active || isError) 2.dp else 1.dp,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun OtpCell(digit: Char?, borderColor: Color, borderWidth: Dp, modifier: Modifier) {
    val shape = MaterialTheme.shapes.small
    val border by animateColorAsState(borderColor, tween(150), label = "otpBorder")
    Box(
        modifier
            .width(52.dp)
            .height(60.dp)
            .background(MemricaColors.Surface, shape)
            .border(borderWidth, border, shape),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = digit,
            transitionSpec = { (scaleIn(tween(160), initialScale = 0.5f) + fadeIn(tween(160))) togetherWith fadeOut(tween(80)) },
            label = "otpDigit",
        ) { shown ->
            Text(
                shown?.toString().orEmpty(),
                style = MaterialTheme.typography.headlineSmall,
                color = MemricaColors.TextStrong,
            )
        }
    }
}

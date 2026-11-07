package app.enjoylearning504.core.designsystem.component

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * One-shot feedback motion: a sideways shake for "no" and a quick grow-and-settle for "yes".
 * The values are only read while drawing, so a running animation never recomposes its target.
 */
@Stable
class FeedbackMotion internal constructor() {
    internal val offsetDp = Animatable(0f)
    internal val scale = Animatable(1f)

    suspend fun shake() {
        offsetDp.snapTo(0f)
        offsetDp.animateTo(
            0f,
            keyframes {
                durationMillis = 380
                -10f at 50
                9f at 120
                -6f at 190
                4f at 260
                -2f at 320
            },
        )
    }

    suspend fun pulse(peak: Float = 1.06f) {
        scale.snapTo(1f)
        scale.animateTo(1f, keyframes { durationMillis = 280; peak at 110 using FastOutSlowInEasing })
    }
}

@Composable
fun rememberFeedbackMotion(): FeedbackMotion = remember { FeedbackMotion() }

/** Applies [motion]'s shake and pulse; [extraScale] is multiplied in (for example a press effect). */
fun Modifier.feedbackMotion(motion: FeedbackMotion, extraScale: () -> Float = { 1f }): Modifier = graphicsLayer {
    translationX = motion.offsetDp.value.dp.toPx()
    val s = motion.scale.value * extraScale()
    scaleX = s
    scaleY = s
}

/**
 * The next step slides in from the reading-direction end while the old one leaves toward the
 * start (right to left in Persian); [forward] false runs it the other way, for going back.
 * Size changes snap instead of animating.
 */
fun <S> AnimatedContentTransitionScope<S>.stepTransition(forward: Boolean = true, durationMillis: Int = 260): ContentTransform {
    val direction = if (forward) SlideDirection.Start else SlideDirection.End
    return (slideIntoContainer(direction, tween(durationMillis)) { it / 4 } + fadeIn(tween(durationMillis)))
        .togetherWith(slideOutOfContainer(direction, tween(durationMillis)) { it / 4 } + fadeOut(tween(durationMillis / 2)))
        .using(null)
}

package app.enjoylearning504.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import app.enjoylearning504.core.designsystem.R
import app.enjoylearning504.core.model.PlantStage
import app.enjoylearning504.core.model.WordLevel

@DrawableRes
fun wordLevelDrawable(level: WordLevel): Int = when (level.value) {
    0 -> R.drawable.word_level_0
    1 -> R.drawable.word_level_1
    2 -> R.drawable.word_level_2
    3 -> R.drawable.word_level_3
    4 -> R.drawable.word_level_4
    else -> R.drawable.word_level_5
}

@DrawableRes
fun plantStageDrawable(stage: PlantStage): Int = when (stage) {
    PlantStage.Empty -> R.drawable.lesson_level_0
    PlantStage.Sprout -> R.drawable.lesson_level_1
    PlantStage.Growing -> R.drawable.lesson_level_2
    PlantStage.Flowering -> R.drawable.lesson_level_3
}

/** The six-stage plant shown for a word's level. Decorative: the level is also given as text where it matters. */
@Composable
fun WordLevelIcon(level: WordLevel, modifier: Modifier = Modifier) {
    Image(painterResource(wordLevelDrawable(level)), contentDescription = null, modifier = modifier)
}

@Composable
fun PlantStageIcon(stage: PlantStage, modifier: Modifier = Modifier) {
    Image(painterResource(plantStageDrawable(stage)), contentDescription = null, modifier = modifier)
}

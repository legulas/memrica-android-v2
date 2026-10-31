package app.enjoylearning504.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.enjoylearning504.core.designsystem.R

/** The original app's palette. */
object MemricaColors {
    val Teal = Color(0xFF00ACC1)
    val TealDark = Color(0xFF0191A3)
    val TealDeep = Color(0xFF026077)
    val TealLight = Color(0xFFB2EBF2)
    val Yellow = Color(0xFFFAC305)
    val YellowDark = Color(0xFFDCAA00)
    val Green = Color(0xFF7EC447)
    val Red = Color(0xFFD75A46)
    val Background = Color(0xFFF5F5F5)
    val Surface = Color(0xFFFFFFFF)
    val Divider = Color(0xFFE0E0E0)
    val Muted = Color(0xFFA1A1A1)
    val Text = Color(0xFF515151)
    val TextStrong = Color(0xFF212121)
}

/** Colors the material scheme has no slot for. */
@Immutable
data class AnswerColors(val correct: Color, val wrong: Color)

val LocalAnswerColors = staticCompositionLocalOf { AnswerColors(MemricaColors.Green, MemricaColors.Red) }

val IranYekan = FontFamily(Font(R.font.iranyekan, FontWeight.Normal))
val LogoFont = FontFamily(Font(R.font.segoe_black, FontWeight.Black))

private val ColorScheme = lightColorScheme(
    primary = MemricaColors.Teal,
    onPrimary = Color.White,
    primaryContainer = MemricaColors.TealDark,
    onPrimaryContainer = Color.White,
    secondary = MemricaColors.Yellow,
    onSecondary = Color.White,
    secondaryContainer = MemricaColors.TealLight,
    onSecondaryContainer = MemricaColors.TealDeep,
    tertiary = MemricaColors.Green,
    error = MemricaColors.Red,
    background = MemricaColors.Background,
    onBackground = MemricaColors.Text,
    surface = MemricaColors.Surface,
    onSurface = MemricaColors.Text,
    surfaceVariant = MemricaColors.Background,
    onSurfaceVariant = MemricaColors.Muted,
    outlineVariant = MemricaColors.Divider,
)

private fun TextStyle.yekan() = copy(fontFamily = IranYekan)

private val BaseTypography = Typography()

private val AppTypography = Typography(
    displaySmall = BaseTypography.displaySmall.yekan(),
    headlineLarge = BaseTypography.headlineLarge.yekan().copy(fontWeight = FontWeight.Bold),
    headlineMedium = BaseTypography.headlineMedium.yekan(),
    headlineSmall = BaseTypography.headlineSmall.yekan(),
    titleLarge = BaseTypography.titleLarge.yekan(),
    titleMedium = BaseTypography.titleMedium.yekan(),
    titleSmall = BaseTypography.titleSmall.yekan(),
    bodyLarge = BaseTypography.bodyLarge.yekan().copy(lineHeight = 28.sp),
    bodyMedium = BaseTypography.bodyMedium.yekan().copy(lineHeight = 24.sp),
    bodySmall = BaseTypography.bodySmall.yekan(),
    labelLarge = BaseTypography.labelLarge.yekan().copy(fontSize = 16.sp),
    labelMedium = BaseTypography.labelMedium.yekan(),
    labelSmall = BaseTypography.labelSmall.yekan(),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun MemricaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColorScheme, typography = AppTypography, shapes = AppShapes, content = content)
}

package radiant.nimbus.ui.elements

import android.content.res.Resources
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimensions(
    val none: Dp = 0.dp,
    val cardRounding: Dp = 8.dp,

    //Divider values
    //NOTE:Divider 1dp is also used in other placed as a default border
    val divider: Dp = 1.dp,
    val dividerSmall: Dp = 2.dp,
    val dividerMedium: Dp = 4.dp,

    //elevation values
    val elevationSmall: Dp = 2.dp,
    val elevationMedium: Dp = 4.dp,
    val elevationLarge: Dp = 8.dp,

    //Padding values
    val paddingExtraExtraExtraSmall: Dp = 2.dp,
    val paddingExtraExtraSmall: Dp = 4.dp,
    val paddingExtraSmall: Dp = 8.dp,
    val paddingSmall: Dp = 12.dp,
    val paddingMedium: Dp = 16.dp,
    val paddingLarge: Dp = 20.dp,
    val paddingExtraLarge: Dp = 24.dp,
    val paddingExtraExtraLarge: Dp = 32.dp,

    //Icon sizes
    val iconExtraSmall: Dp = 12.dp,
    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 20.dp,
    val iconLarge: Dp = 24.dp,
    val iconExtraLarge: Dp = 32.dp,
    val iconExtraExtraLarge: Dp = 50.dp,

    //Image size
    val imageExtraSmall: Dp = 20.dp,
    val imageSmall: Dp = 28.dp,
    val imageMedium: Dp = 40.dp,
    val imageLarge: Dp = 48.dp,
    val imageMediumLarge: Dp = 60.dp,
    val imageExtraLarge: Dp = 88.dp,
    val imageExtraExtraLarge: Dp = 135.dp,
)


val LocalDimensions = staticCompositionLocalOf { Dimensions() }

val MaterialTheme.dimens
    @Composable
    @ReadOnlyComposable
    get() = LocalDimensions.current

fun dpToPx(size: Dp) : Float {
    return size.value * Resources.getSystem().displayMetrics.density
}
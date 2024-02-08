package morpho.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min


val morphoLightPrimary = Color(0xff004182)
val morphoLightOnPrimary = Color(0xFFFFFFFF)
val morphoLightPrimaryContainer = Color(0xffaec8ff)
val morphoLightOnPrimaryContainer = Color(0xff0e002a)
val morphoLightSecondary = Color(0xff45406f)
val morphoLightOnSecondary = Color(0xFFFFFFFF)
val morphoLightSecondaryContainer = Color(0xffd8bcfa)
val morphoLightOnSecondaryContainer = Color(0xFF271904)
val morphoLightTertiary = Color(0xff64405f)
val morphoLightOnTertiary = Color(0xFFFFFFFF)
val morphoLightTertiaryContainer = Color(0xffbcbdea)
val morphoLightOnTertiaryContainer = Color(0xff040c20)
val morphoLightError = Color(0xFFBA1B1B)
val morphoLightErrorContainer = Color(0xFFFFDAD4)
val morphoLightOnError = Color(0xFFFFFFFF)
val morphoLightOnErrorContainer = Color(0xFF410001)
val morphoLightBackground = Color(0xFFFCFCFC)
val morphoLightOnBackground = Color(0xff19161f)
val morphoLightSurface = Color(0xFFFCFCFC)
val morphoLightOnSurface = Color(0xFF1F1B16)
val morphoLightSurfaceVariant = Color(0xFFF0E0CF)
val morphoLightOnSurfaceVariant = Color(0xff39394f)
val morphoLightOutline = Color(0xff676a81)
val morphoLightInverseOnSurface = Color(0xffe7e6f9)
val morphoLightInverseSurface = Color(0xff2a2b34)
val morphoLightPrimaryInverse = Color(0xff45b8ff)

val morphoDarkPrimary = Color(0xff458fff)
val morphoDarkOnPrimary = Color(0xff888faf)
val morphoDarkPrimaryContainer = Color(0xff2c2f60)
val morphoDarkOnPrimaryContainer = Color(0xffd1d1ff)
val morphoDarkSecondary = Color(0xffb0acbe)
val morphoDarkOnSecondary = Color(0xff16183e)
val morphoDarkSecondaryContainer = Color(0xff2b3156)
val morphoDarkOnSecondaryContainer = Color(0xffbebcfa)
val morphoDarkTertiary = Color(0xffc6a2ce)
val morphoDarkOnTertiary = Color(0xff2c1635)
val morphoDarkTertiaryContainer = Color(0xFF3A2B4C)
val morphoDarkOnTertiaryContainer = Color(0xffc2bcea)
val morphoDarkError = Color(0xFFFFB4A9)
val morphoDarkErrorContainer = Color(0xFF930006)
val morphoDarkOnError = Color(0xFF680003)
val morphoDarkOnErrorContainer = Color(0xFFFFDAD4)
val morphoDarkBackground = Color(0xff000000)
val morphoDarkOnBackground = Color(0xFFEAE1D9)
val morphoDarkSurface = Color(0xff121017)
val morphoDarkOnSurface = Color(0xffd9eadb)
val morphoDarkSurfaceVariant = Color(0xff0d121a)
val morphoDarkOnSurfaceVariant = Color(0xffbbb4d3)
val morphoDarkOutline = Color(0xff80849c)
val morphoDarkInverseOnSurface = Color(0xff2a1a32)
val morphoDarkInverseSurface = Color(0xffead9ea)
val morphoDarkPrimaryInverse = Color(0xff002262)

fun Color.contrastAgainst(background: Color): Float {
    val fg = if (alpha < 1f) compositeOver(background) else this

    val fgLuminance = fg.luminance() + 0.05f
    val bgLuminance = background.luminance() + 0.05f

    return max(fgLuminance, bgLuminance) / min(fgLuminance, bgLuminance)
}
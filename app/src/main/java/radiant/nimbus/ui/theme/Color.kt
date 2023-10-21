package radiant.nimbus.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min


val nimbusLightPrimary = Color(0xff004182)
val nimbusLightOnPrimary = Color(0xFFFFFFFF)
val nimbusLightPrimaryContainer = Color(0xffaec8ff)
val nimbusLightOnPrimaryContainer = Color(0xff0e002a)
val nimbusLightSecondary = Color(0xff45406f)
val nimbusLightOnSecondary = Color(0xFFFFFFFF)
val nimbusLightSecondaryContainer = Color(0xffd8bcfa)
val nimbusLightOnSecondaryContainer = Color(0xFF271904)
val nimbusLightTertiary = Color(0xff64405f)
val nimbusLightOnTertiary = Color(0xFFFFFFFF)
val nimbusLightTertiaryContainer = Color(0xffbcbdea)
val nimbusLightOnTertiaryContainer = Color(0xff040c20)
val nimbusLightError = Color(0xFFBA1B1B)
val nimbusLightErrorContainer = Color(0xFFFFDAD4)
val nimbusLightOnError = Color(0xFFFFFFFF)
val nimbusLightOnErrorContainer = Color(0xFF410001)
val nimbusLightBackground = Color(0xFFFCFCFC)
val nimbusLightOnBackground = Color(0xff19161f)
val nimbusLightSurface = Color(0xFFFCFCFC)
val nimbusLightOnSurface = Color(0xFF1F1B16)
val nimbusLightSurfaceVariant = Color(0xFFF0E0CF)
val nimbusLightOnSurfaceVariant = Color(0xff39394f)
val nimbusLightOutline = Color(0xff676a81)
val nimbusLightInverseOnSurface = Color(0xffe7e6f9)
val nimbusLightInverseSurface = Color(0xff2a2b34)
val nimbusLightPrimaryInverse = Color(0xff45b8ff)

val nimbusDarkPrimary = Color(0xff458fff)
val nimbusDarkOnPrimary = Color(0xff888faf)
val nimbusDarkPrimaryContainer = Color(0xff2c2f60)
val nimbusDarkOnPrimaryContainer = Color(0xffd1d1ff)
val nimbusDarkSecondary = Color(0xffb0acbe)
val nimbusDarkOnSecondary = Color(0xff16183e)
val nimbusDarkSecondaryContainer = Color(0xff2b3156)
val nimbusDarkOnSecondaryContainer = Color(0xffbebcfa)
val nimbusDarkTertiary = Color(0xffc6a2ce)
val nimbusDarkOnTertiary = Color(0xff2c1635)
val nimbusDarkTertiaryContainer = Color(0xFF3A2B4C)
val nimbusDarkOnTertiaryContainer = Color(0xffc2bcea)
val nimbusDarkError = Color(0xFFFFB4A9)
val nimbusDarkErrorContainer = Color(0xFF930006)
val nimbusDarkOnError = Color(0xFF680003)
val nimbusDarkOnErrorContainer = Color(0xFFFFDAD4)
val nimbusDarkBackground = Color(0xff000000)
val nimbusDarkOnBackground = Color(0xFFEAE1D9)
val nimbusDarkSurface = Color(0xff121017)
val nimbusDarkOnSurface = Color(0xffd9eadb)
val nimbusDarkSurfaceVariant = Color(0xff0d121a)
val nimbusDarkOnSurfaceVariant = Color(0xffbbb4d3)
val nimbusDarkOutline = Color(0xff80849c)
val nimbusDarkInverseOnSurface = Color(0xff2a1a32)
val nimbusDarkInverseSurface = Color(0xffead9ea)
val nimbusDarkPrimaryInverse = Color(0xff002262)

fun Color.contrastAgainst(background: Color): Float {
    val fg = if (alpha < 1f) compositeOver(background) else this

    val fgLuminance = fg.luminance() + 0.05f
    val bgLuminance = background.luminance() + 0.05f

    return max(fgLuminance, bgLuminance) / min(fgLuminance, bgLuminance)
}
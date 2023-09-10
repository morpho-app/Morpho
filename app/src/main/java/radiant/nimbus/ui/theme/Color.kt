package radiant.nimbus.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val nimbusLightPrimary = Color(0xFF825500)
val nimbusLightOnPrimary = Color(0xFFFFFFFF)
val nimbusLightPrimaryContainer = Color(0xFFFFDDAE)
val nimbusLightOnPrimaryContainer = Color(0xFF2A1800)
val nimbusLightSecondary = Color(0xFF6F5B40)
val nimbusLightOnSecondary = Color(0xFFFFFFFF)
val nimbusLightSecondaryContainer = Color(0xFFFADEBC)
val nimbusLightOnSecondaryContainer = Color(0xFF271904)
val nimbusLightTertiary = Color(0xFF516440)
val nimbusLightOnTertiary = Color(0xFFFFFFFF)
val nimbusLightTertiaryContainer = Color(0xFFD3EABC)
val nimbusLightOnTertiaryContainer = Color(0xFF102004)
val nimbusLightError = Color(0xFFBA1B1B)
val nimbusLightErrorContainer = Color(0xFFFFDAD4)
val nimbusLightOnError = Color(0xFFFFFFFF)
val nimbusLightOnErrorContainer = Color(0xFF410001)
val nimbusLightBackground = Color(0xFFFCFCFC)
val nimbusLightOnBackground = Color(0xFF1F1B16)
val nimbusLightSurface = Color(0xFFFCFCFC)
val nimbusLightOnSurface = Color(0xFF1F1B16)
val nimbusLightSurfaceVariant = Color(0xFFF0E0CF)
val nimbusLightOnSurfaceVariant = Color(0xFF4F4539)
val nimbusLightOutline = Color(0xFF817567)
val nimbusLightInverseOnSurface = Color(0xFFF9EFE6)
val nimbusLightInverseSurface = Color(0xFF34302A)
val nimbusLightPrimaryInverse = Color(0xFFFFB945)

val nimbusDarkPrimary = Color(0xFFFFB945)
val nimbusDarkOnPrimary = Color(0xFF452B00)
val nimbusDarkPrimaryContainer = Color(0xFF624000)
val nimbusDarkOnPrimaryContainer = Color(0xFFFFDDAE)
val nimbusDarkSecondary = Color(0xFFDDC3A2)
val nimbusDarkOnSecondary = Color(0xFF3E2E16)
val nimbusDarkSecondaryContainer = Color(0xFF56442B)
val nimbusDarkOnSecondaryContainer = Color(0xFFFADEBC)
val nimbusDarkTertiary = Color(0xFFB8CEA2)
val nimbusDarkOnTertiary = Color(0xFF243516)
val nimbusDarkTertiaryContainer = Color(0xFF3A4C2B)
val nimbusDarkOnTertiaryContainer = Color(0xFFD3EABC)
val nimbusDarkError = Color(0xFFFFB4A9)
val nimbusDarkErrorContainer = Color(0xFF930006)
val nimbusDarkOnError = Color(0xFF680003)
val nimbusDarkOnErrorContainer = Color(0xFFFFDAD4)
val nimbusDarkBackground = Color(0xFF1F1B16)
val nimbusDarkOnBackground = Color(0xFFEAE1D9)
val nimbusDarkSurface = Color(0xFF1F1B16)
val nimbusDarkOnSurface = Color(0xFFEAE1D9)
val nimbusDarkSurfaceVariant = Color(0xFF4F4539)
val nimbusDarkOnSurfaceVariant = Color(0xFFD3C4B4)
val nimbusDarkOutline = Color(0xFF9C8F80)
val nimbusDarkInverseOnSurface = Color(0xFF32281A)
val nimbusDarkInverseSurface = Color(0xFFEAE1D9)
val nimbusDarkPrimaryInverse = Color(0xFF624000)

fun Color.contrastAgainst(background: Color): Float {
    val fg = if (alpha < 1f) compositeOver(background) else this

    val fgLuminance = fg.luminance() + 0.05f
    val bgLuminance = background.luminance() + 0.05f

    return max(fgLuminance, bgLuminance) / min(fgLuminance, bgLuminance)
}
package com.morpho.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min


val morphoLightPrimary =Color(0xff5079be)
val morphoLightOnPrimary = Color(0xffdde2e7)
val morphoLightPrimaryContainer = Color(0xff95b4ea)
val morphoLightOnPrimaryContainer = Color(0xff3a3746)
val morphoLightSecondary = Color(0xffb05cce)
val morphoLightOnSecondary = Color(0xffc5cdd9)
val morphoLightSecondaryContainer = Color(0xffcea1de)
val morphoLightOnSecondaryContainer = Color(0xff313a44)
val morphoLightTertiary = Color(0xff608e32)
val morphoLightOnTertiary = Color(0xffe5eee4)
val morphoLightTertiaryContainer = Color(0xff76af6f)
val morphoLightOnTertiaryContainer = Color(0xff2d3329)
val morphoLightError = Color(0xffd05858)
val morphoLightErrorContainer = Color(0xffe17373)
val morphoLightOnError = Color(0xfff6e4e4)
val morphoLightOnErrorContainer = Color(0xFF410001)
val morphoLightBackground = Color(0xfffafafa)
val morphoLightOnBackground = Color(0xff2b2c36)
val morphoLightSurface = Color(0xFFFCFCFC)
val morphoLightOnSurface = Color(0xFF1F1B16)
val morphoLightSurfaceVariant = Color(0xffe8ebf0)
val morphoLightOnSurfaceVariant = Color(0xff39394f)
val morphoLightOutline = Color(0xff676a81)
val morphoLightInverseOnSurface = Color(0xffe7e6f9)
val morphoLightInverseSurface = Color(0xff2a2b34)
val morphoLightPrimaryInverse = Color(0xff2c1635)
val morphoLightSurfaceDim = Color(0xffBAC3CB)

val morphoDarkPrimary = Color(0xff7f93e8)
val morphoDarkOnPrimary = Color(0xff242934)
val morphoDarkPrimaryContainer = Color(0xff6c8aeb)
val morphoDarkOnPrimaryContainer = Color(0xff22222d)
val morphoDarkSecondary =  Color(0xffd38aea)
val morphoDarkOnSecondary = Color(0xff3f3557)
val morphoDarkSecondaryContainer =Color(0xff8f5da1)
val morphoDarkOnSecondaryContainer = Color(0xffc5cdd9)
val morphoDarkTertiary = Color(0xffa0c980)
val morphoDarkOnTertiary = Color(0xff394634)
val morphoDarkTertiaryContainer = Color(0xffa0c980)
val morphoDarkOnTertiaryContainer = Color(0xffc6eabc)
val morphoDarkError = Color(0xffec7279)
val morphoDarkErrorContainer = Color(0xff55393d)
val morphoDarkOnError = Color(0xff55393d)
val morphoDarkOnErrorContainer = Color(0xffec7279)
val morphoDarkBackground = Color(0xff2c2e34)
val morphoDarkOnBackground = Color(0xffb6b5c2)
val morphoDarkSurface = Color(0xff33353f)
val morphoDarkOnSurface = Color(0xfff6f6f6)
val morphoDarkSurfaceVariant = Color(0xff414550)
val morphoDarkOnSurfaceVariant = Color(0xffc5cdd9)
val morphoDarkOutline = Color(0xff80849c)
val morphoDarkInverseOnSurface = Color(0xff535c6a)
val morphoDarkSurfaceDim = Color(0xff24262a)
val morphoDarkInverseSurface = Color(0xffd7dee3)
val morphoDarkPrimaryInverse = Color(0xff492452)

fun Color.contrastAgainst(background: Color): Float {
    val fg = if (alpha < 1f) compositeOver(background) else this

    val fgLuminance = fg.luminance() + 0.05f
    val bgLuminance = background.luminance() + 0.05f

    return max(fgLuminance, bgLuminance) / min(fgLuminance, bgLuminance)
}
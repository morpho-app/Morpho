package com.morpho.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import morpho.composeapp.generated.resources.IBMPlexSans_Bold
import morpho.composeapp.generated.resources.IBMPlexSans_ExtraLight
import morpho.composeapp.generated.resources.IBMPlexSans_Light
import morpho.composeapp.generated.resources.IBMPlexSans_Medium
import morpho.composeapp.generated.resources.IBMPlexSans_Regular
import morpho.composeapp.generated.resources.IBMPlexSans_SemiBold
import morpho.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font

@OptIn(ExperimentalResourceApi::class)
@Composable
fun IBMPlexSans() = FontFamily(
    Font(Res.font.IBMPlexSans_ExtraLight, weight = FontWeight.ExtraLight),
    Font(Res.font.IBMPlexSans_Light, weight = FontWeight.Light),
    Font(Res.font.IBMPlexSans_Regular, weight = FontWeight.Normal),
    Font(Res.font.IBMPlexSans_Medium, weight = FontWeight.Medium),
    Font(Res.font.IBMPlexSans_SemiBold, weight = FontWeight.SemiBold),
    Font(Res.font.IBMPlexSans_Bold, weight = FontWeight.Bold),
)

// Set of Material typography styles to start with
@Composable
fun MorphoTypography() = Typography().run {
    val fontFamily = IBMPlexSans()
    copy(
        headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
        displayLarge = displayLarge.copy(fontFamily = fontFamily),
        displayMedium = displayMedium.copy(fontFamily = fontFamily),
        displaySmall = displaySmall.copy(fontFamily = fontFamily),
        titleLarge = titleLarge.copy(fontFamily = fontFamily),
        titleMedium = titleMedium.copy(fontFamily = fontFamily),
        titleSmall = titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = bodySmall.copy(fontFamily = fontFamily),
        labelLarge = labelLarge.copy(fontFamily = fontFamily),
        labelMedium = labelMedium.copy(fontFamily = fontFamily),
        labelSmall = labelSmall.copy(fontFamily = fontFamily)
    )
}
package com.morpho.app.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


val LightColorScheme = lightColorScheme(

    primary = morphoLightPrimary,
    onPrimary = morphoLightOnPrimary,
    primaryContainer = morphoLightPrimaryContainer,
    onPrimaryContainer = morphoLightOnPrimaryContainer,
    inversePrimary = morphoLightPrimaryInverse,
    secondary = morphoLightSecondary,
    onSecondary = morphoLightOnSecondary,
    secondaryContainer = morphoLightSecondaryContainer,
    onSecondaryContainer = morphoLightOnSecondaryContainer,
    tertiary = morphoLightTertiary,
    onTertiary = morphoLightOnTertiary,
    tertiaryContainer = morphoLightTertiaryContainer,
    onTertiaryContainer = morphoLightOnTertiaryContainer,
    background = morphoLightBackground,
    onBackground = morphoLightOnBackground,
    surface = morphoLightSurface,
    onSurface = morphoLightOnSurface,
    surfaceVariant = morphoLightSurfaceVariant,
    onSurfaceVariant = morphoLightOnSurfaceVariant,
    inverseSurface = morphoLightInverseSurface,
    inverseOnSurface = morphoLightInverseOnSurface,
    error = morphoLightError,
    onError = morphoLightOnError,
    errorContainer = morphoLightErrorContainer,
    onErrorContainer = morphoLightOnErrorContainer,
    outline = morphoLightOutline,



)

val DarkColorScheme = darkColorScheme(

    primary = morphoDarkPrimary,
    onPrimary = morphoDarkOnPrimary,
    primaryContainer = morphoDarkPrimaryContainer,
    onPrimaryContainer = morphoDarkOnPrimaryContainer,
    inversePrimary = morphoDarkPrimaryInverse,
    secondary = morphoDarkSecondary,
    onSecondary = morphoDarkOnSecondary,
    secondaryContainer = morphoDarkSecondaryContainer,
    onSecondaryContainer = morphoDarkOnSecondaryContainer,
    tertiary = morphoDarkTertiary,
    onTertiary = morphoDarkOnTertiary,
    tertiaryContainer = morphoDarkTertiaryContainer,
    onTertiaryContainer = morphoDarkOnTertiaryContainer,
    background = morphoDarkBackground,
    onBackground = morphoDarkOnBackground,
    surface = morphoDarkSurface,
    onSurface = morphoDarkOnSurface,
    surfaceVariant = morphoDarkSurfaceVariant,
    onSurfaceVariant = morphoDarkOnSurfaceVariant,
    inverseSurface = morphoDarkInverseSurface,
    inverseOnSurface = morphoLightInverseOnSurface,
    error = morphoDarkError,
    onError = morphoDarkOnError,
    errorContainer = morphoDarkErrorContainer,
    onErrorContainer = morphoDarkOnErrorContainer,
    outline = morphoDarkOutline,

)

@Composable
expect fun MorphoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
)
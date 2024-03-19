package com.morpho.app.ui.theme

import android.app.Activity
import android.graphics.Color
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LightColorScheme = lightColorScheme(
    //primary = Purple80,
    //secondary = PurpleGrey80,
    //tertiary = Pink80

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
    //primary = Purple40,
    //secondary = PurpleGrey40,
    //tertiary = Pink40

    primary = morphoDarkPrimary,
    onPrimary = morphoDarkOnPrimary,
    primaryContainer = morphoDarkPrimaryContainer,
    onPrimaryContainer = morphoLightOnPrimaryContainer,
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
fun MorphoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {

        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
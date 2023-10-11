package radiant.nimbus.ui.theme

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

    primary = nimbusLightPrimary,
    onPrimary = nimbusLightOnPrimary,
    primaryContainer = nimbusLightPrimaryContainer,
    onPrimaryContainer = nimbusLightOnPrimaryContainer,
    inversePrimary = nimbusLightPrimaryInverse,
    secondary = nimbusLightSecondary,
    onSecondary = nimbusLightOnSecondary,
    secondaryContainer = nimbusLightSecondaryContainer,
    onSecondaryContainer = nimbusLightOnSecondaryContainer,
    tertiary = nimbusLightTertiary,
    onTertiary = nimbusLightOnTertiary,
    tertiaryContainer = nimbusLightTertiaryContainer,
    onTertiaryContainer = nimbusLightOnTertiaryContainer,
    background = nimbusLightBackground,
    onBackground = nimbusLightOnBackground,
    surface = nimbusLightSurface,
    onSurface = nimbusLightOnSurface,
    surfaceVariant = nimbusLightSurfaceVariant,
    onSurfaceVariant = nimbusLightOnSurfaceVariant,
    inverseSurface = nimbusLightInverseSurface,
    inverseOnSurface = nimbusLightInverseOnSurface,
    error = nimbusLightError,
    onError = nimbusLightOnError,
    errorContainer = nimbusLightErrorContainer,
    onErrorContainer = nimbusLightOnErrorContainer,
    outline = nimbusLightOutline,



)

val DarkColorScheme = darkColorScheme(
    //primary = Purple40,
    //secondary = PurpleGrey40,
    //tertiary = Pink40

    primary = nimbusDarkPrimary,
    onPrimary = nimbusDarkOnPrimary,
    primaryContainer = nimbusDarkPrimaryContainer,
    onPrimaryContainer = nimbusLightOnPrimaryContainer,
    inversePrimary = nimbusDarkPrimaryInverse,
    secondary = nimbusDarkSecondary,
    onSecondary = nimbusDarkOnSecondary,
    secondaryContainer = nimbusDarkSecondaryContainer,
    onSecondaryContainer = nimbusDarkOnSecondaryContainer,
    tertiary = nimbusDarkTertiary,
    onTertiary = nimbusDarkOnTertiary,
    tertiaryContainer = nimbusDarkTertiaryContainer,
    onTertiaryContainer = nimbusDarkOnTertiaryContainer,
    background = nimbusDarkBackground,
    onBackground = nimbusDarkOnBackground,
    surface = nimbusDarkSurface,
    onSurface = nimbusDarkOnSurface,
    surfaceVariant = nimbusDarkSurfaceVariant,
    onSurfaceVariant = nimbusDarkOnSurfaceVariant,
    inverseSurface = nimbusDarkInverseSurface,
    inverseOnSurface = nimbusLightInverseOnSurface,
    error = nimbusDarkError,
    onError = nimbusDarkOnError,
    errorContainer = nimbusDarkErrorContainer,
    onErrorContainer = nimbusDarkOnErrorContainer,
    outline = nimbusDarkOutline,

)

@Composable
fun NimbusTheme(
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
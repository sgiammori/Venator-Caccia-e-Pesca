package it.mygroup.org.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── LIGHT – Verde bosco + Marrone terra + Teal acqua ─────────────────────────
private val LightColorScheme = lightColorScheme(
    primary               = ForestGreen40,
    onPrimary             = Color.White,
    primaryContainer      = ForestGreen90,
    onPrimaryContainer    = ForestGreen10,

    secondary             = EarthBrown40,
    onSecondary           = Color.White,
    secondaryContainer    = EarthBrown90,
    onSecondaryContainer  = EarthBrown10,

    tertiary              = WaterTeal40,
    onTertiary            = Color.White,
    tertiaryContainer     = WaterTeal90,
    onTertiaryContainer   = WaterTeal10,

    error                 = ErrorRed,
    onError               = Color.White,
    errorContainer        = ErrorContainer,
    onErrorContainer      = OnErrorContainer,

    background            = NeutralGreen99,
    onBackground          = NeutralDark10,
    surface               = NeutralGreen99,
    onSurface             = NeutralDark10,
    surfaceVariant        = SurfaceVariantLight,
    onSurfaceVariant      = NeutralDark30,
    outline               = OutlineLight,
    outlineVariant        = OutlineVariantLight,
    inverseSurface        = NeutralDark20,
    inverseOnSurface      = NeutralGreen95,
    inversePrimary        = ForestGreen80,
)

// ── DARK – Sfondo foresta notte + accenti chiari ─────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary               = ForestGreen80,
    onPrimary             = ForestGreen20,
    primaryContainer      = ForestGreen30,
    onPrimaryContainer    = ForestGreen90,

    secondary             = EarthBrown80,
    onSecondary           = EarthBrown20,
    secondaryContainer    = EarthBrown30,
    onSecondaryContainer  = EarthBrown90,

    tertiary              = WaterTeal80,
    onTertiary            = WaterTeal20,
    tertiaryContainer     = WaterTeal30,
    onTertiaryContainer   = WaterTeal90,

    error                 = ErrorRedDark,
    onError               = Color(0xFF690005),
    errorContainer        = Color(0xFF93000A),
    onErrorContainer      = ErrorContainer,

    background            = NeutralDark10,
    onBackground          = NeutralGreen95,
    surface               = NeutralDark10,
    onSurface             = NeutralGreen95,
    surfaceVariant        = SurfaceVariantDark,
    onSurfaceVariant      = OutlineVariantLight,
    outline               = OutlineDark,
    outlineVariant        = SurfaceVariantDark,
    inverseSurface        = NeutralGreen95,
    inverseOnSurface      = NeutralDark20,
    inversePrimary        = ForestGreen40,
)

@Composable
fun CacciatoriEPescatoriAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disabilita dynamic color per mantenere sempre il tema caccia/pesca
    dynamicColor: Boolean = false,
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

    // Colora la status bar con il colore primary del tema
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
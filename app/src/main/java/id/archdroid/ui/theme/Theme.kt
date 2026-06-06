package id.archdroid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Dark = darkColorScheme(
    primary = Color(0xFF7DD3FC),
    secondary = Color(0xFF86EFAC),
    background = Color(0xFF050505),
    surface = Color(0xFF111111),
    onBackground = Color(0xFFEDEDED),
    onSurface = Color(0xFFEDEDED)
)

private val Light = lightColorScheme(
    primary = Color(0xFF075985),
    secondary = Color(0xFF166534),
    background = Color(0xFFF7F7F7),
    surface = Color.White
)

@Composable
fun ArchDroidTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) Dark else Light, content = content)
}

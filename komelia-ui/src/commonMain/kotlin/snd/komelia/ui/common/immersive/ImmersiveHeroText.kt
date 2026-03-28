package snd.komelia.ui.common.immersive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp

@Composable
fun ImmersiveHeroText(
    seriesTitle: String,
    authorYear: String,
    chapterTitle: String? = null,
    expandFraction: Float,
    accentColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val resolvedAccentColor = accentColor ?: MaterialTheme.colorScheme.primary

    val isCollapsed = expandFraction < 0.5f
    val isLightText = onSurface.luminance() > 0.5f
    val shadowColor = if (isLightText) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f)
    val shadow = if (isCollapsed) Shadow(
        color = shadowColor,
        blurRadius = 8f
    ) else Shadow.None

    val titleColor = lerp(onSurface, onSurface, expandFraction)
    val authorColor = lerp(resolvedAccentColor, onSurfaceVariant, expandFraction)
    val chapterColor = lerp(onSurface, onSurfaceVariant, expandFraction)

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Author & Year
        Text(
            text = authorYear.uppercase(),
            color = authorColor,
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 2.sp,
                shadow = shadow,
                fontSize = lerp(11.sp, 12.sp, expandFraction)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Series Title
        Text(
            text = seriesTitle.uppercase(),
            color = titleColor,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                shadow = shadow,
                fontSize = lerp(32.sp, 20.sp, expandFraction),
                lineHeight = lerp(36.sp, 24.sp, expandFraction)
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(vertical = lerp(4.dp, 0.dp, expandFraction))
        )

        // Chapter Name (Optional)
        if (chapterTitle != null) {
            Text(
                text = chapterTitle.uppercase(),
                color = chapterColor,
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 1.5.sp,
                    shadow = shadow,
                    fontSize = lerp(14.sp, 12.sp, expandFraction)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

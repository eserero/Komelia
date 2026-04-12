package snd.komelia.ui.reader.epub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
actual fun EpubLoadingScreen(
    steps: List<EpubLoadingStep>,
    bookTitle: String?,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()

            if (!bookTitle.isNullOrBlank() || steps.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
            }

            if (!bookTitle.isNullOrBlank()) {
                Text(
                    text = bookTitle,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(16.dp))
            }

            if (steps.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    steps.forEach { step -> LoadingStepRow(step) }
                }
            }
        }
    }
}

@Composable
private fun LoadingStepRow(step: EpubLoadingStep) {
    val primary = MaterialTheme.colorScheme.primary
    val dimmed = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            when (step.status) {
                EpubLoadingStepStatus.Complete -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(20.dp),
                )
                EpubLoadingStepStatus.InProgress -> CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
                EpubLoadingStepStatus.Pending -> Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = dimmed,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = step.label,
            style = MaterialTheme.typography.bodyMedium,
            color = when (step.status) {
                EpubLoadingStepStatus.Complete -> MaterialTheme.colorScheme.onSurface
                EpubLoadingStepStatus.InProgress -> primary
                EpubLoadingStepStatus.Pending -> dimmed
            },
        )
    }
}

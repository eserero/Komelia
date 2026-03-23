package snd.komelia.ui.reader.epub

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.util.Url
import snd.komelia.ui.LocalAccentColor

private fun containsHref(link: Link, targetHref: Url): Boolean {
    if (link.url()?.removeFragment() == targetHref) return true
    return link.children.any { containsHref(it, targetHref) }
}

private fun expandAncestors(links: List<Link>, targetHref: Url, expandedState: MutableMap<String, Boolean>) {
    for (link in links) {
        if (link.children.any { containsHref(it, targetHref) }) {
            expandedState[link.href.toString()] = true
            expandAncestors(link.children, targetHref, expandedState)
        }
    }
}

@Composable
fun Epub3TocDialog(
    toc: List<Link>,
    currentHref: Url?,
    onNavigate: (Link) -> Unit,
    onDismiss: () -> Unit,
) {
    val expandedState = remember { mutableStateMapOf<String, Boolean>() }
    val configuration = LocalConfiguration.current
    val maxHeightDp = (configuration.screenHeightDp * 0.8f).dp
    val lazyListState = rememberLazyListState()

    val currentTopLevelIndex = remember(currentHref, toc) {
        if (currentHref == null) -1
        else {
            val target = currentHref.removeFragment()
            toc.indexOfFirst { containsHref(it, target) }
        }
    }

    LaunchedEffect(currentHref) {
        if (currentHref != null) {
            val target = currentHref.removeFragment()
            expandAncestors(toc, target, expandedState)
            if (currentTopLevelIndex >= 0) {
                lazyListState.scrollToItem(currentTopLevelIndex)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column {
                // Title row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 4.dp),
                ) {
                    Text(
                        text = "Table of Contents",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (toc.isEmpty()) {
                    Text(
                        text = "No chapters available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                    )
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.heightIn(max = maxHeightDp),
                    ) {
                        itemsIndexed(toc) { index, link ->
                            if (index > 0) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            TocRow(
                                link = link,
                                depth = 0,
                                currentHref = currentHref,
                                expandedState = expandedState,
                                onNavigate = onNavigate,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TocRow(
    link: Link,
    depth: Int,
    currentHref: Url?,
    expandedState: MutableMap<String, Boolean>,
    onNavigate: (Link) -> Unit,
) {
    val key = link.href.toString()
    val hasChildren = link.children.isNotEmpty()
    val isExpanded = expandedState[key] ?: false
    val title = link.title
        ?: link.href.toString().substringAfterLast('/').substringBeforeLast('.')

    val isCurrentChapter = currentHref != null &&
        link.url()?.removeFragment() == currentHref.removeFragment()

    val accentColor = LocalAccentColor.current ?: MaterialTheme.colorScheme.secondary
    val buttonColors = if (isCurrentChapter) {
        ButtonDefaults.textButtonColors(
            containerColor = accentColor.copy(alpha = 0.15f),
            contentColor = accentColor,
        )
    } else {
        ButtonDefaults.textButtonColors()
    }

    if (hasChildren) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (depth * 16).dp),
        ) {
            TextButton(
                onClick = { onNavigate(link) },
                colors = buttonColors,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            IconButton(onClick = { expandedState[key] = !isExpanded }) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                )
            }
        }
        if (isExpanded) {
            link.children.forEach { child ->
                TocRow(
                    link = child,
                    depth = depth + 1,
                    currentHref = currentHref,
                    expandedState = expandedState,
                    onNavigate = onNavigate,
                )
            }
        }
    } else {
        TextButton(
            onClick = { onNavigate(link) },
            colors = buttonColors,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .padding(start = (depth * 16).dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

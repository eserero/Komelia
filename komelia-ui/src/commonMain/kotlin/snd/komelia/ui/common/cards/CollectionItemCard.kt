package snd.komelia.ui.common.cards

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import snd.komelia.ui.LocalCardLayoutBelow
import snd.komelia.ui.LocalKomgaState
import snd.komelia.ui.common.images.CollectionThumbnail
import snd.komelia.ui.common.menus.CollectionActionsMenu
import snd.komga.client.collection.KomgaCollection

@Composable
fun CollectionImageCard(
    collection: KomgaCollection,
    onCollectionClick: () -> Unit,
    onCollectionDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardLayoutBelow = LocalCardLayoutBelow.current
    ItemCard(
        modifier = modifier,
        onClick = onCollectionClick,
        image = {
            CollectionCardHoverOverlay(collection, onCollectionDelete) {
                CollectionImageOverlay(
                    collection = collection,
                    showTitle = !cardLayoutBelow,
                ) {
                    CollectionThumbnail(
                        collectionId = collection.id,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        },
        content = {
            if (cardLayoutBelow) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = collection.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "${collection.seriesIds.size} series",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    )
}

@Composable
private fun CollectionCardHoverOverlay(
    collection: KomgaCollection,
    onCollectionDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState()
    var isActionsMenuExpanded by remember { mutableStateOf(false) }
    val showOverlay = derivedStateOf { isHovered.value || isActionsMenuExpanded }

    val border = if (showOverlay.value) overlayBorderModifier() else Modifier

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hoverable(interactionSource)
            .then(border),
        contentAlignment = Alignment.Center
    ) {
        content()

        val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
        if (showOverlay.value && isAdmin) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom,
            ) {

                Spacer(Modifier.weight(1f))

                Box {
                    IconButton(
                        onClick = { isActionsMenuExpanded = true },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) { Icon(Icons.Default.MoreVert, contentDescription = null) }

                    CollectionActionsMenu(
                        collection = collection,
                        onCollectionDelete = onCollectionDelete,
                        expanded = isActionsMenuExpanded,
                        onDismissRequest = { isActionsMenuExpanded = false }
                    )

                }
            }
        }
    }
}

@Composable
private fun CollectionImageOverlay(
    collection: KomgaCollection,
    showTitle: Boolean = true,
    content: @Composable () -> Unit
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart
    ) {
        content()
        if (showTitle) {
            CardGradientOverlay()
            Column(Modifier.padding(10.dp)) {
                CardOutlinedText(collection.name)
                CardOutlinedText("${collection.seriesIds.size} series")
            }
        }
    }

}
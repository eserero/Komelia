package snd.komelia.ui.common.menus

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import snd.komelia.ui.dialogs.ConfirmationDialog
import snd.komelia.ui.dialogs.collectionedit.CollectionEditDialog
import snd.komga.client.collection.KomgaCollection

@Composable
fun CollectionActionsMenu(
    collection: KomgaCollection,
    onCollectionDelete: () -> Unit,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Collection",
            body = "Collection ${collection.name} will be removed from this server. Your media files will not be affected. This cannot be undone. Continue?",
            confirmText = "Yes, delete collection \"${collection.name}\"",
            onDialogConfirm = {
                onCollectionDelete()
                onDismissRequest()

            },
            onDialogDismiss = {
                showDeleteDialog = false
                onDismissRequest()
            },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    }

    var showEditDialog by remember { mutableStateOf(false) }
    if (showEditDialog) {
        CollectionEditDialog(collection = collection, onDismissRequest = {
            showEditDialog = false
            onDismissRequest()
        })
    }

    val showDropdown = derivedStateOf { expanded && !showDeleteDialog && !showEditDialog }
    DropdownMenu(
        expanded = showDropdown.value,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            text = { Text("Edit", style = MaterialTheme.typography.labelLarge) },
            leadingIcon = { Icon(Icons.Rounded.Edit, null) },
            onClick = { showEditDialog = true },
        )

        DropdownMenuItem(
            text = { Text("Delete", style = MaterialTheme.typography.labelLarge) },
            leadingIcon = { Icon(Icons.Rounded.DeleteForever, null) },
            onClick = { showDeleteDialog = true },
            colors = MenuDefaults.itemColors(
                textColor = MaterialTheme.colorScheme.error,
                leadingIconColor = MaterialTheme.colorScheme.error
            )
        )

    }
}
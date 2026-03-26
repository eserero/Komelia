package snd.komelia.offline.sync

import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.context
import kotlin.time.Instant

class AndroidOfflineFileSystem : OfflineFileSystem {
    override fun listDirectories(root: PlatformFile): List<PlatformFile> {
        return when (val androidFile = root.androidFile) {
            is AndroidFile.FileWrapper -> {
                androidFile.file.listFiles { f -> f.isDirectory }?.map { PlatformFile(it) } ?: emptyList()
            }
            is AndroidFile.UriWrapper -> queryChildren(androidFile.uri, directoriesOnly = true)
        }
    }

    override fun listFiles(root: PlatformFile): List<PlatformFile> {
        return when (val androidFile = root.androidFile) {
            is AndroidFile.FileWrapper -> {
                androidFile.file.listFiles { f -> f.isFile }?.map { PlatformFile(it) } ?: emptyList()
            }
            is AndroidFile.UriWrapper -> queryChildren(androidFile.uri, directoriesOnly = false)
        }
    }

    private fun queryChildren(uri: android.net.Uri, directoriesOnly: Boolean): List<PlatformFile> {
        val context = FileKit.context
        val docId = if (DocumentsContract.isDocumentUri(context, uri)) {
            DocumentsContract.getDocumentId(uri)
        } else {
            DocumentsContract.getTreeDocumentId(uri)
        }
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, docId)
        val results = mutableListOf<PlatformFile>()
        context.contentResolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
            ),
            null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val childDocId = cursor.getString(0)
                val mimeType = cursor.getString(1)
                val isDir = mimeType == DocumentsContract.Document.MIME_TYPE_DIR
                if (directoriesOnly == isDir) {
                    results.add(PlatformFile(DocumentsContract.buildDocumentUriUsingTree(uri, childDocId)))
                }
            }
        }
        return results
    }

    override fun getFileSize(file: PlatformFile): Long {
        return when (val androidFile = file.androidFile) {
            is AndroidFile.FileWrapper -> androidFile.file.length()
            is AndroidFile.UriWrapper -> {
                DocumentFile.fromSingleUri(FileKit.context, androidFile.uri)?.length() ?: 0L
            }
        }
    }

    override fun getLastModified(file: PlatformFile): Instant {
        return when (val androidFile = file.androidFile) {
            is AndroidFile.FileWrapper -> Instant.fromEpochMilliseconds(androidFile.file.lastModified())
            is AndroidFile.UriWrapper -> {
                val lastModified = DocumentFile.fromSingleUri(FileKit.context, androidFile.uri)?.lastModified() ?: 0L
                Instant.fromEpochMilliseconds(lastModified)
            }
        }
    }

    override fun getName(file: PlatformFile): String {
        return when (val androidFile = file.androidFile) {
            is AndroidFile.FileWrapper -> androidFile.file.name
            is AndroidFile.UriWrapper -> {
                DocumentFile.fromSingleUri(FileKit.context, androidFile.uri)?.name ?: ""
            }
        }
    }
}

actual fun createOfflineFileSystem(): OfflineFileSystem = AndroidOfflineFileSystem()

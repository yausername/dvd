@file:JvmName("PathUtils")

import android.net.Uri
import android.provider.DocumentsContract

fun getPathFromUri(uri: Uri): String? {
    val docId = DocumentsContract.getTreeDocumentId(uri)
    val split: Array<String?> = docId.split(":").toTypedArray()
    val res: String?
    if (split.size >= 2 && split[1] != null) {
        res = split[1]!!
    } else {
        res = null
    }
    return res
}
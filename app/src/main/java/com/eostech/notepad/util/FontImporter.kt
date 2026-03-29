package com.eostech.notepad.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object FontImporter {

    /**
     * Imports fonts from the selected URI. Supports .ttf, .otf, and .zip files.
     * Extracts fonts into context.filesDir/fonts and returns the number of successfully imported fonts.
     */
    fun importFonts(context: Context, uris: List<Uri>): Int {
        val fontsDir = File(context.filesDir, "fonts")
        if (!fontsDir.exists()) {
            fontsDir.mkdirs()
        }

        var importedCount = 0

        uris.forEach { uri ->
            try {
                val
                fileName = getFileName(context, uri) ?: return@forEach
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@forEach

                if (fileName.endsWith(".zip", true)) {
                    val zis = ZipInputStream(inputStream)
                    var entry: ZipEntry? = zis.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory && (entry.name.endsWith(".ttf", true) || entry.name.endsWith(".otf", true))) {
                            val fName = File(entry.name).name
                            val targetFile = File(fontsDir, fName)
                            FileOutputStream(targetFile).use { out ->
                                zis.copyTo(out)
                            }
                            importedCount++
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                    zis.close()
                } else if (fileName.endsWith(".ttf", true) || fileName.endsWith(".otf", true)) {
                    val targetFile = File(fontsDir, fileName)
                    FileOutputStream(targetFile).use { out ->
                        inputStream.copyTo(out)
                    }
                    importedCount++
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return importedCount
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = it.getString(index)
                }
            }
        }
        if (result == null) {
            result = uri.path?.let { File(it).name }
        }
        return result
    }
}

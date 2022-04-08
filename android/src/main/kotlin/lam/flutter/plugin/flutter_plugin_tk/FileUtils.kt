package lam.flutter.plugin.flutter_plugin_tk

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

object FileUtils {
    fun createFileFromUri(contentResolver: ContentResolver, uri: Uri, directory: File): File {
        val file = createTempFile(directory)
        file.outputStream().use {
            contentResolver.openInputStream(uri)?.copyTo(it)
        }
        return file
    }

    private fun createTempFile(directory: File): File {
        return File.createTempFile("img", ".jpg", directory)
    }

    fun storeImageToAppCache(image: Bitmap, directory: File, onSaveImageSuccess: (String) -> Unit) {
        val pictureFile: File = createTempFile(directory)
        try {
            val fos = FileOutputStream(pictureFile)
            image.compress(Bitmap.CompressFormat.PNG, 90, fos)
            fos.close()
            onSaveImageSuccess(pictureFile.path)
        } catch (e: FileNotFoundException) {
            Log.d("AppLog", "File not found: $e")
        } catch (e: IOException) {
            Log.d("AppLog", "Error accessing file: $e")
        }
    }
}
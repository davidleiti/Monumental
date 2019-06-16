package ubb.thesis.david.monumental.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

sealed class FileUtils {
    companion object {

        fun createTempFile(context: Context): File? {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDirectory: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            return try {
                File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDirectory)
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }
        }

        fun deleteFile(context: Context, filePath: String): Boolean {
            val photo = File(filePath)
            if (photo.exists()) {
                return if (photo.delete()) {
                    MediaScannerConnection.scanFile(context, arrayOf(filePath), null) { _, _ ->
                        //  Performed media scan at given path successfully
                    }
                    true
                } else {
                    false
                }
            }
            return false
        }

    }
}
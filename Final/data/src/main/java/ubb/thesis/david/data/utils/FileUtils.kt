package ubb.thesis.david.data.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

sealed class FileUtils {
    companion object {

        fun createTempFile(context: Context): File? {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDirectory: File = context.getExternalFilesDir(DIRECTORY_PICTURES)!!
            return try {
                File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDirectory)
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }
        }

        fun createSharedFile(fileName: String): File? {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDirectory: File = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES)
            return try {
                File.createTempFile("JPEG_${fileName}_${timeStamp}_", ".jpg", storageDirectory)
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }
        }

        fun performMediaScan(context: Context, filePath: String) =
            MediaScannerConnection.scanFile(context, arrayOf(filePath), null, null)

        fun deleteFile(context: Context, filePath: String): Boolean {
            val photo = File(filePath)
            if (photo.exists()) {
                return if (photo.delete()) {
                    performMediaScan(context, filePath)
                    true
                } else {
                    false
                }
            }
            return false
        }

    }
}
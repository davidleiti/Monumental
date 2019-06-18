package ubb.thesis.david.data.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
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
            val storageDirectory: File = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES)
            return try {
                File.createTempFile("JPEG_${fileName}_", ".jpg", storageDirectory)
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }
        }

        fun copyToShared(context: Context, sourcePath: String): Boolean {
            val tempFile = File(sourcePath)
            val sharedFile = createSharedFile(tempFile.name)

            var source: FileChannel? = null
            var destination: FileChannel? = null

            return try {
                source = FileInputStream(tempFile).channel
                destination = FileOutputStream(sharedFile).channel
                destination.transferFrom(source, 0, source.size())
                performMediaScan(context, sharedFile!!.absolutePath)
                true
            } catch (ex: IOException) {
                ex.printStackTrace()
                false
            } finally {
                source?.close()
                destination?.close()
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
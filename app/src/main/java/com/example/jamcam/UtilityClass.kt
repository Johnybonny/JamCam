package com.example.jamcam

import android.content.Context
import android.os.Build
import android.os.Environment
import android.text.format.DateFormat
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

class UtilityClass {
    companion object {
        interface FileOperationCallback {
            fun onFileOperationComplete()
            fun onFileOperationError(error: Exception)
        }


        @JvmStatic
        fun now(): String {
            return DateFormat.format("yyyy-MM-dd_kk-mm-ss", Date().time).toString()
        }

        @JvmStatic
        fun readFile(context: Context, childName: String, fileName: String): String? {
            try {
                val root = File(context.getExternalFilesDir(null), childName)
                val newFile = File(root, fileName)
                val reader = BufferedReader(FileReader(newFile))
                val line: String? = reader.readLine()
                reader.close()
                if (line != null) {
                    return line
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        @JvmStatic
        fun saveToFile(context: Context, childName: String, fileName: String, toSave: String) {
            try {
                val root = File(context.getExternalFilesDir(null), childName)
                if (!root.exists()) {
                    root.mkdirs()
                }
                val newFile = File(root, fileName)
                if (newFile.exists()) {
                    newFile.delete()
                }

                val writer = FileWriter(newFile)
                writer.append(toSave)
                writer.flush()
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun differenceInSeconds(date1: String, date2: String): Long {
            val format = SimpleDateFormat("yyyy-MM-dd_kk-mm-ss", Locale.getDefault())
            val startDate = format.parse(date1)
            val endDate = format.parse(date2)

            val diffInMillis = endDate!!.time - startDate!!.time
            val diffInSeconds = diffInMillis / 1000
            return abs(diffInSeconds)
        }

        @JvmStatic
        fun directoryPath(folderName: String): File {
            val dir: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    folderName
                )
            } else {
                File(Environment.getExternalStorageDirectory(), folderName)
            }

            // Make sure the path directory exists.
            if (!dir.exists()) {
                // Make it if it doesn't exist.
                val success: Boolean = dir.mkdirs()
//                if (!success) {
//                    return null
//                }
            }
            return dir
        }

        @JvmStatic
        fun createFolderIfNotExists(path: String) {
            val folder = File(path)
            if (!folder.exists()) {
                val created = folder.mkdir()
                if (created) {
                    println("Folder created successfully.")
                } else {
                    println("Failed to create the folder.")
                }
            } else {
                println("Folder already exists.")
            }
        }

        @JvmStatic
        fun substractTime(initialTime: String, secondsToSubstract: Int): String {
            val parts = initialTime.split(":")
            val minutes = parts[0].toInt()
            val seconds = parts[1].toInt()

            val totalSeconds = minutes * 60 + seconds - secondsToSubstract

            return secondsToTimestamp(totalSeconds)
        }

        @JvmStatic
        fun secondsToTimestamp(seconds: Int): String {
            val newMinutes = seconds / 60
            val newSeconds = seconds % 60

            return String.format("%02d:%02d", newMinutes, newSeconds)
        }

        @JvmStatic
        fun roundPercentage(number: Double, prec: Int): Double {
            var divider = 1
            for(i in 1..prec){
                divider *= 10
            }
            return round(number * divider)
        }

    }
}

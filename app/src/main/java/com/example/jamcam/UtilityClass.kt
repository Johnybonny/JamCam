package com.example.jamcam

import android.content.Context
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

class UtilityClass {
    companion object {
        @JvmStatic
        fun readTimestamp(context: Context, fileName: String): String? {
            try {
                val root = File(context.getExternalFilesDir(null), "Timestamps")
                val newFile = File(root, fileName)
                val reader = BufferedReader(FileReader(newFile))
                val line: String? = reader.readLine()
                println("READ: $line")
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
        fun saveTimestamp(context: Context, fileName: String) {
            try {
                val root = File(context.getExternalFilesDir(null), "Timestamps")
                if (!root.exists()) {
                    root.mkdirs()
                }
                val newFile = File(root, fileName)
                if (newFile.exists()) {
                    newFile.delete()
                }

                val writer = FileWriter(newFile)
                val ts = DateFormat.format("yyyy-MM-dd_kk-mm-ss", Date().time).toString()
                writer.append(ts)
                println("WRITE: $ts")
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

    }
}

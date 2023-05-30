package com.example.jamcam

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.Date



class MainActivity : AppCompatActivity() {

    private val MICROPHONE_PERMISSION_CODE = 201
    private val CAMERA_PERMISSION_CODE = 202
    private val WRITE_EXTERNAL_STORAGE_CODE = 203
    private val SYSTEM_ALERT_WINDOW_CODE = 204
    private val FOREGROUND_SERVICE_CODE = 205


    private var isPermissionsGrantedMicrophone = false
    private var isPermissionsGrantedCamera = false
    private var isPermissionsGrantedStorage = false
    private var isPermissionsGrantedAlert = false
    private var isPermissionsGrantedForeground = false

    private var ableToRecord = false
    private var isRecording = false

    private var seconds = 0
    private var running = false


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton: Button = findViewById(R.id.startButton)
        val stopButton: Button = findViewById(R.id.stopButton)
        val clickButton: Button = findViewById(R.id.clickButton)

        val timeView: TextView = findViewById(R.id.time_view)

        getMicrophonePermission()
        getCameraPermission()
        getExternalStoragePermission()
        getAlertWindowPermission()
        getForegroundServicePermission()

        startButton.setOnClickListener { startRecording(timeView) }
        stopButton.setOnClickListener { stopRecording() }
        clickButton.setOnClickListener { computeInitTime() }

    }


    private fun computeInitTime() {
        val tim = UtilityClass.readTimestamp(this, "timer_start.txt")
        val cam = UtilityClass.readTimestamp(this, "camera_start.txt")
        if (tim != null && cam != null) {
            val diff = UtilityClass.differenceInSeconds(tim, cam)
            println("DIFFERENCE: $diff")
        }
    }


    private fun runTimer(timeView: TextView) {
        val handler = Handler()
        running = true

        UtilityClass.saveTimestamp(this,
            "timer_start.txt")

        handler.post(object : Runnable {
            override fun run() {
                timeView.text = seconds.toString()
                if (running) {
                    seconds++
                }
                handler.postDelayed(this, 1000)
            }
        })
    }



    private fun createRecorder() {
        val intent = Intent(this, BackgroundVideoRecorder::class.java)
        startService(intent)
    }


    private fun destroyRecorder() {
        val intent = Intent(this, BackgroundVideoRecorder::class.java)
        stopService(intent)
    }


    private fun checkCreateRecorder() {
        if (isPermissionsGrantedMicrophone &&
            isPermissionsGrantedCamera &&
            isPermissionsGrantedStorage &&
            isPermissionsGrantedAlert &&
            isPermissionsGrantedForeground
        ) {
            ableToRecord = true
        }
    }


    private fun startRecording(timeView: TextView) {
        if (!isRecording && ableToRecord) {
            runTimer(timeView)
            createRecorder()
            isRecording = true
        }
    }


    private fun stopRecording() {
        if (isRecording && ableToRecord) {
            destroyRecorder()
            isRecording = false
        }
    }




    fun getVideosDirectoryPath(): File {
        val appVideosFolder = File(Environment.getExternalStorageDirectory(), "/JamCam/")

        // Create app-private folder if not exists
        if (!appVideosFolder.exists()) appVideosFolder.mkdir()
        return appVideosFolder
    }



    private fun getMicrophonePermission() {
        println("getMicrophonePermission")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO),
                MICROPHONE_PERMISSION_CODE
            )
        } else {
            isPermissionsGrantedMicrophone = true
            checkCreateRecorder()
        }
    }


    private fun getCameraPermission() {
        println("getCameraPermission")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            isPermissionsGrantedCamera = true
            checkCreateRecorder()
        }
    }


    private fun getExternalStoragePermission() {
        println("getExternalStoragePermission")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_CODE
            )
        } else {
            isPermissionsGrantedStorage = true
            checkCreateRecorder()
        }
    }


    private fun getForegroundServicePermission() {
        println("getForegroundServicePermission")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.FOREGROUND_SERVICE),
                FOREGROUND_SERVICE_CODE
            )
        } else {
            isPermissionsGrantedForeground = true
            checkCreateRecorder()
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun getAlertWindowPermission() {
        println("getAlertWindowPermission")
        if (!Settings.canDrawOverlays(this)) {
            // Permission not granted, request it using the intent
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivityForResult(intent, SYSTEM_ALERT_WINDOW_CODE)
        } else {
            // Permission already granted
            isPermissionsGrantedAlert = true
            checkCreateRecorder()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MICROPHONE_PERMISSION_CODE ||
            requestCode == CAMERA_PERMISSION_CODE ||
            requestCode == WRITE_EXTERNAL_STORAGE_CODE ||
            requestCode == SYSTEM_ALERT_WINDOW_CODE ||
            requestCode == FOREGROUND_SERVICE_CODE
        ) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                when (requestCode) {
                    MICROPHONE_PERMISSION_CODE -> {
                        isPermissionsGrantedMicrophone = true
                        checkCreateRecorder()
                    }

                    CAMERA_PERMISSION_CODE -> {
                        isPermissionsGrantedCamera = true
                        checkCreateRecorder()
                    }

                    WRITE_EXTERNAL_STORAGE_CODE -> {
                        isPermissionsGrantedStorage = true
                        checkCreateRecorder()
                    }

                    SYSTEM_ALERT_WINDOW_CODE -> {
                        isPermissionsGrantedAlert = true
                        checkCreateRecorder()
                    }

                    FOREGROUND_SERVICE_CODE -> {
                        isPermissionsGrantedForeground = true
                        checkCreateRecorder()
                    }

                    else -> {
                        println("There is a problem in MainActivity -> onRequestPermissionsResult")
                    }
                }

            } else {
                // Permission denied, handle accordingly
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
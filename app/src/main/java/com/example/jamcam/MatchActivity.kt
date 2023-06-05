package com.example.jamcam

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MatchActivity : AppCompatActivity() {

    private val MICROPHONE_PERMISSION_CODE = 201
    private val CAMERA_PERMISSION_CODE = 202
    private val WRITE_EXTERNAL_STORAGE_CODE = 203
    private val READ_EXTERNAL_STORAGE_CODE = 204
    private val SYSTEM_ALERT_WINDOW_CODE = 205
    private val FOREGROUND_SERVICE_CODE = 206


    private var isPermissionsGrantedMicrophone = false
    private var isPermissionsGrantedCamera = false
    private var isPermissionsGrantedStorageRead = false
    private var isPermissionsGrantedStorageWrite = false
    private var isPermissionsGrantedAlert = false
    private var isPermissionsGrantedForeground = false

    private var ableToRecord = false
    private var isRecording = false

    private var seconds = 0
    private var running = false

    private val timestamps = ArrayList<Int>()
    private val highlightLength: Int = 10


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match)

        val startButton: Button = findViewById(R.id.startButton)
        val stopButton: Button = findViewById(R.id.stopButton)
        val clickButton: Button = findViewById(R.id.clickButton)

        val timeView: TextView = findViewById(R.id.time_view)

        getMicrophonePermission()
        getCameraPermission()
        getReadExternalStoragePermission()
        getWriteExternalStoragePermission()
        getAlertWindowPermission()
        getForegroundServicePermission()

        startButton.setOnClickListener { startRecording(timeView) }
        stopButton.setOnClickListener { stopRecording() }
        clickButton.setOnClickListener { reportHighlight() }
    }

    private fun reportHighlight() {
        val now = UtilityClass.now()
        val cam = UtilityClass.readTimestamp(this, "camera_start.txt")
        if (cam != null) {
            val diff = UtilityClass.differenceInSeconds(now, cam)
            timestamps.add(diff.toInt())
        }
    }

    private fun runTimer(timeView: TextView) {
        val handler = Handler()
        running = true


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
            isPermissionsGrantedStorageRead &&
            isPermissionsGrantedStorageWrite &&
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
            createHighlights()
        }
    }

    private fun createHighlights() {
        val videoEditor = VideoEditor("JamCam", "original.mp4")
        for(startTime in timestamps) {
            val start = UtilityClass.secondsToTimestamp(startTime)
            val stop = UtilityClass.addTime(start, highlightLength)
            videoEditor.createHighlight(this, start, stop)
        }

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

    private fun getWriteExternalStoragePermission() {
        println("getWriteExternalStoragePermission")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_CODE
            )
        } else {
            isPermissionsGrantedStorageWrite = true
            checkCreateRecorder()
        }
    }

    private fun getReadExternalStoragePermission() {
        println("getExternalStoragePermission")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_CODE
            )
        } else {
            isPermissionsGrantedStorageRead = true
            checkCreateRecorder()
        }
    }

    private fun getForegroundServicePermission() {
        println("getForegroundServicePermission")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
            == PackageManager.PERMISSION_DENIED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.FOREGROUND_SERVICE),
                    FOREGROUND_SERVICE_CODE
                )
            }
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
            requestCode == READ_EXTERNAL_STORAGE_CODE ||
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
                        isPermissionsGrantedStorageWrite = true
                        checkCreateRecorder()
                    }

                    READ_EXTERNAL_STORAGE_CODE -> {
                        isPermissionsGrantedStorageRead = true
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
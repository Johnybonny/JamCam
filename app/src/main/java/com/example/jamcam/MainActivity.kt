package com.example.jamcam

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


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




    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val startButton: Button = findViewById(R.id.startButton)
//        val stopButton: Button = findViewById(R.id.stopButton)

        getMicrophonePermission()
        getCameraPermission()
        getExternalStoragePermission()
        getAlertWindowPermission()
        getForegroundServicePermission()

//        startButton.setOnClickListener { startRecording() }
//        stopButton.setOnClickListener { stopRecording() }
    }

    private fun createRecorder() {
        println("createRecorder")
        val intent = Intent(this, BackgroundVideoRecorder::class.java)
        println("Here")
        startService(intent)
    }


    private fun checkcreateRecorder() {
        println("checkcreateRecorder")
        if (isPermissionsGrantedMicrophone &&
            isPermissionsGrantedCamera &&
            isPermissionsGrantedStorage &&
            isPermissionsGrantedAlert &&
            isPermissionsGrantedForeground) {
            createRecorder()
        }
    }


//    private fun startRecording() {
//        if (!isRecording) {
//            mediaRecorder.start()
//            isRecording = true
//        }
//    }
//
//
//    private fun stopRecording() {
//        if (isRecording) {
//            mediaRecorder.stop()
//            mediaRecorder.reset()
//            camera.lock()
//            isRecording = false
//        }
//    }



    private fun getMicrophonePermission() {
        println("getMicrophonePermission")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),
                MICROPHONE_PERMISSION_CODE)
        } else {
            isPermissionsGrantedMicrophone = true
            checkcreateRecorder()
        }
    }

    private fun getCameraPermission() {
        println("getCameraPermission")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE)
        } else {
            isPermissionsGrantedCamera = true
            checkcreateRecorder()
        }
    }

    private fun getExternalStoragePermission() {
        println("getExternalStoragePermission")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_CODE)
        } else {
            isPermissionsGrantedStorage = true
            checkcreateRecorder()
        }
    }

    private fun getForegroundServicePermission() {
        println("getForegroundServicePermission")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
            == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.FOREGROUND_SERVICE),
                FOREGROUND_SERVICE_CODE)
        } else {
            isPermissionsGrantedForeground = true
            checkcreateRecorder()
        }
    }

//    private fun getAlertWindowPermission() {
//        println("getAlertWindowPermission")
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW)
//            == PackageManager.PERMISSION_DENIED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SYSTEM_ALERT_WINDOW),
//                SYSTEM_ALERT_WINDOW_CODE)
//        } else {
//            isPermissionsGrantedAlert = true
//            checkcreateRecorder()
//        }
//    }

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
            checkcreateRecorder()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MICROPHONE_PERMISSION_CODE ||
            requestCode == CAMERA_PERMISSION_CODE ||
            requestCode == WRITE_EXTERNAL_STORAGE_CODE ||
            requestCode == SYSTEM_ALERT_WINDOW_CODE ||
            requestCode == FOREGROUND_SERVICE_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                when (requestCode) {
                    MICROPHONE_PERMISSION_CODE -> {
                        isPermissionsGrantedMicrophone = true
                        checkcreateRecorder()
                    }
                    CAMERA_PERMISSION_CODE -> {
                        isPermissionsGrantedCamera = true
                        checkcreateRecorder()
                    }
                    WRITE_EXTERNAL_STORAGE_CODE -> {
                        isPermissionsGrantedStorage = true
                        checkcreateRecorder()
                    }
                    SYSTEM_ALERT_WINDOW_CODE -> {
                        isPermissionsGrantedAlert = true
                        checkcreateRecorder()
                    }
                    FOREGROUND_SERVICE_CODE -> {
                        isPermissionsGrantedForeground = true
                        checkcreateRecorder()
                    }
                    else -> {
                        println("PROBLEM")
                    }
                }

            } else {
                // Permission denied, handle accordingly
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
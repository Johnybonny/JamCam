package com.example.jamcam

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.core.content.ContextCompat


class MatchActivity : AppCompatActivity() {

    private val MULTIPLE_PERMISSIONS_CODE = 200

    val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val SYSTEM_ALERT_WINDOW_CODE = 205

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


        if (!checkPermissionForSystemAlertWindow()) return

        if (checkPermissions()) {
            startApp()
        }

        startButton.setOnClickListener { startRecording(timeView) }
        stopButton.setOnClickListener { stopRecording() }
        clickButton.setOnClickListener { reportHighlight() }
    }

    private fun startApp() {
        ableToRecord = true
    }

    private fun checkPermissions(): Boolean {
        val listPermissionsNeeded = ArrayList<String>()
        for (p in permissions) {
            val result = ContextCompat.checkSelfPermission(this, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                MULTIPLE_PERMISSIONS_CODE
            )
            return false
        }
        return true
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
        for (startTime in timestamps) {
            val start = UtilityClass.secondsToTimestamp(startTime)
            val stop = UtilityClass.addTime(start, highlightLength)
            videoEditor.createHighlight(this, start, stop)
        }

    }

    private fun checkPermissionForSystemAlertWindow(): Boolean {
        if (!isPermissionForSystemAlertWindowGranted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, SYSTEM_ALERT_WINDOW_CODE)
            }
            return false
        }
        return true
    }

    private fun isPermissionForSystemAlertWindowGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MULTIPLE_PERMISSIONS_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permissions granted
                    startApp()
                } else {
                    // permissions not granted
                    Toast.makeText(
                        this,
                        "Permissions denied. The app cannot start.",
                        Toast.LENGTH_LONG
                    ).show()
                    Toast.makeText(
                        this,
                        "Please re-start Open Dash Cam app and grant the requested permissions.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                return
            }
        }
    }

}
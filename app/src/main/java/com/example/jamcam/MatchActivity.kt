package com.example.jamcam

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class MatchActivity : AppCompatActivity() {

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

    private fun startRecording(timeView: TextView) {
        if (!isRecording) {
            runTimer(timeView)
            createRecorder()
            isRecording = true
        }
    }

    private fun stopRecording() {
        if (isRecording) {
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



}
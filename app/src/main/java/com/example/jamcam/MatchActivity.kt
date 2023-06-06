package com.example.jamcam

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class MatchActivity : AppCompatActivity() {

    private var isRecording = false

    private var seconds = 0
    private var running = false

    private val timestamps = ArrayList<Int>()
    private val highlightLength: Int = 10

    private var playersList: MutableList<PlayersFragment.Player>? = mutableListOf()
    private var matchDescription: String? = null

    private lateinit var playerSpinner: Spinner


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match)

        val endButton: Button = findViewById(R.id.endButton)
        val clickButton: Button = findViewById(R.id.clickButton)

        val timeView: TextView = findViewById(R.id.time_view)
        startRecording(timeView)

        playersList =
            (intent.getSerializableExtra("playersList") as? ArrayList<PlayersFragment.Player>)
        matchDescription = intent.getStringExtra("matchDescription")

        playerSpinner = findViewById(R.id.playerSpinner)

        // Get the player names from the playersList
        val playerLastNames =
            playersList?.map { "${it.firstName} ${it.lastName} (${it.number})" } ?: emptyList()

        // Create an ArrayAdapter and set it as the Spinner adapter
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, playerLastNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        playerSpinner.adapter = adapter


        endButton.setOnClickListener { stopRecording() }
        clickButton.setOnClickListener { reportHighlight() }
    }


    private fun reportHighlight() {
        val now = UtilityClass.now()
        val cam = UtilityClass.readTimestamp(this, "camera_start.txt")
        if (cam != null) {
            val diff = UtilityClass.differenceInSeconds(now, cam)
            if (diff >= highlightLength) {
                timestamps.add(diff.toInt())
            } else {
                timestamps.add(highlightLength)
            }

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
            val stop = UtilityClass.secondsToTimestamp(startTime)
            val start = UtilityClass.substractTime(stop, highlightLength)
            videoEditor.createHighlight(this, start, stop)
        }

    }


}
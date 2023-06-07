package com.example.jamcam

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class MatchActivity : AppCompatActivity() {

    private var isRecording = false
    private var match: Match? = null
    private var matchId = 0

    private var seconds = 0
    private var running = false

    private val timestamps = ArrayList<Int>()
    private var timestampCounter = 0
    private val highlightLength: Int = 10 //TODO: Wybierane w ustawieniach

    val chosenTypes = listOf("block", "assist", "two-pointer") //TODO: Wybierane w ustawieniach

    private var playersList: MutableList<PlayersFragment.Player>? = mutableListOf()
    var selectedPlayerName: String? = null


    private lateinit var playerSpinner: Spinner


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match)


        // Match description
        val descriptionText: TextView = findViewById(R.id.descriptionText)
        val matchDescription = intent.getStringExtra("matchDescription")
        descriptionText.text = matchDescription

        // Match place
        val placeText: TextView = findViewById(R.id.placeText)
        val matchPlace = intent.getStringExtra("matchPlace")
        placeText.text = matchPlace

        // Timer
        val timeView: TextView = findViewById(R.id.time_view)
        startRecording(timeView)

        // Players spinner
        playersList =
            (intent.getSerializableExtra("playersList") as? ArrayList<PlayersFragment.Player>)
        playerSpinner = findViewById(R.id.playerSpinner)
        val players =
            playersList?.map { "${it.firstName} ${it.lastName} (${it.number})" } ?: emptyList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, players)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        playerSpinner.adapter = adapter
        playerSpinner.setSelection(0)
        playerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedPlayerName = players[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedPlayerName = null
            }
        }


        // Buttons
        val endButton: Button = findViewById(R.id.endButton)
        endButton.setOnClickListener { stopRecording() }

        val clickButton: Button = findViewById(R.id.clickButton)
        clickButton.setOnClickListener { reportHighlight() }

        val twoPointerButton: Button = findViewById(R.id.twoPointerButton)
        twoPointerButton.setOnClickListener { eventOccured("two-pointer") }


        // Create a match record in database
        initializeMatch(matchDescription.toString(), matchPlace.toString())


    }

    private fun initializeMatch(matchDescription: String, matchPlace: String) {
        val dbHandler = DBHandler(this, null, null, 1)

        val date = UtilityClass.now().split("_")[0]
        println("$matchDescription, $matchPlace, $date")
        match = Match(matchDescription, matchPlace, date)
        matchId = dbHandler.addMatch(match!!).toInt()

    }


    private fun eventOccured(eventType: String) {
        // Handle the highlights
        var videoName = "no_video"
        if (eventType in chosenTypes) {
            reportHighlight()
            videoName = "trimmed${matchId}_$timestampCounter.mp4"
        }


        val dbHandler = DBHandler(this, null, null, 1)
        timestampCounter += 1
        if (match != null && selectedPlayerName != null) {
            val event = Event(matchId, eventType, selectedPlayerName!!, videoName)
            dbHandler.addEvent(event)
        }
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
            timestampCounter = 0
            createHighlights()
        }
    }

    private fun createHighlights() {
        val videoEditor = VideoEditor("JamCam", "original.mp4")
        for (startTime in timestamps) {
            val stop = UtilityClass.secondsToTimestamp(startTime)
            val start = UtilityClass.substractTime(stop, highlightLength)
            val outputName = "trimmed${matchId}_$timestampCounter.mp4"
            timestampCounter += 1
            videoEditor.createHighlight(this, start, stop, outputName)
        }

    }


}
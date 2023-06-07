package com.example.jamcam

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.util.ArrayDeque

class MatchActivity : AppCompatActivity() {

    // Basic
    private var isRecording = false
    private val highlightLength: Int = 10 //TODO: Wybierane w ustawieniach
    private val chosenTypes =
        listOf("block", "assist", "two-pointer") //TODO: Wybierane w ustawieniach

    // Match
    private var match: Match? = null
    private var matchId = 0

    // Players
    private var playersList: MutableList<PlayersFragment.Player>? = mutableListOf()
    private var selectedPlayer: PlayersFragment.Player? = null
    private lateinit var playerSpinner: Spinner

    // Timer
    private var seconds = 0

    // Highlights
    private val timestamps = ArrayList<Int>()
    private var timestampCounter = 0


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
                selectedPlayer = playersList!![position]
                displayStats()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedPlayer = null
            }
        }


        // Buttons
        val endButton: Button = findViewById(R.id.endButton)
        endButton.setOnClickListener { stopRecording() }

        val onePointerButton: Button = findViewById(R.id.onePointerButton)
        onePointerButton.setOnClickListener { showPopupWindow(onePointerButton, "one-pointer") }
        val twoPointerButton: Button = findViewById(R.id.twoPointerButton)
        twoPointerButton.setOnClickListener { showPopupWindow(twoPointerButton, "two-pointer") }
        val threePointerButton: Button = findViewById(R.id.threePointerButton)
        threePointerButton.setOnClickListener { showPopupWindow(threePointerButton, "three-pointer") }
        val assistButton: Button = findViewById(R.id.assistButton)
        assistButton.setOnClickListener { made("assist") }
        val reboundButton: Button = findViewById(R.id.reboundButton)
        reboundButton.setOnClickListener { made("rebound") }
        val stealButton: Button = findViewById(R.id.stealButton)
        stealButton.setOnClickListener { made("steal") }
        val blockButton: Button = findViewById(R.id.blockButton)
        blockButton.setOnClickListener { made("block") }
        val foulButton: Button = findViewById(R.id.foulButton)
        foulButton.setOnClickListener { made("foul") }


        // Create a match record in database
        initializeMatch(matchDescription.toString(), matchPlace.toString())

    }

    fun displayStats() {

        val selectedPlayerIndex = playersList?.indexOf(selectedPlayer)
        if (selectedPlayerIndex != null && selectedPlayerIndex != -1) {
            val player = playersList!![selectedPlayerIndex]
            // Finding the TextViews
            val pointsView: TextView = findViewById(R.id.pointsView)
            val fieldGoalsView: TextView = findViewById(R.id.fieldGoalsView)
            val onePointersView: TextView = findViewById(R.id.onePointersView)
            val twoPointersView: TextView = findViewById(R.id.twoPointersView)
            val threePointersView: TextView = findViewById(R.id.threePointersView)
            val assistsView: TextView = findViewById(R.id.assistsView)
            val reboundsView: TextView = findViewById(R.id.reboundsView)
            val stealsView: TextView = findViewById(R.id.stealsView)
            val blocksView: TextView = findViewById(R.id.blocksView)
            val foulsView: TextView = findViewById(R.id.foulsView)

            // Useful values
            val totalPoints = player.oneScored + player.twoScored * 2 + player.threeScored * 3
            val totalAttempted =
                player.twoAttempted + player.threeAttempted // For FG stat, one-pointers
            val totalMade =
                player.twoScored + player.threeScored            // are not considered as they
            val totalMissed =
                totalAttempted - totalMade                     // are not field goals :)

            //Sum of points
            pointsView.text = totalPoints.toString()

            //Field goals
            if (totalAttempted == 0) {
                fieldGoalsView.text = "0/0 (0%)"
            } else {
                fieldGoalsView.text =
                    totalMade.toString().plus("/").plus(totalAttempted.toString()).plus(" ")
                        .plus("(")
                        .plus(
                            UtilityClass.roundPercentage(
                                totalMade.toDouble() / totalAttempted.toDouble(),
                                2
                            ).toString()
                        ).plus("%)")
            }

            //1 pointers
            if (player.oneAttempted == 0) {
                onePointersView.text = "0/0 (0%)"
            } else {
                onePointersView.text =
                    player.oneScored.toString().plus("/").plus(player.oneAttempted.toString())
                        .plus(" ")
                        .plus("(").plus(
                            UtilityClass.roundPercentage(
                                player.oneScored.toDouble() / player.oneAttempted.toDouble(),
                                2
                            ).toString()
                        ).plus("%)")
            }

            //2 pointers
            if (player.twoAttempted == 0) {
                twoPointersView.text = "0/0 (0%)"
            } else {
                twoPointersView.text =
                    player.twoScored.toString().plus("/").plus(player.twoAttempted.toString())
                        .plus(" ")
                        .plus("(").plus(
                            UtilityClass.roundPercentage(
                                player.twoScored.toDouble() / player.twoAttempted.toDouble(),
                                2
                            ).toString()
                        ).plus("%)")
            }

            //3 pointers
            if (player.threeAttempted == 0) {
                threePointersView.text = "0/0 (0%)"
            } else {
                threePointersView.text =
                    player.threeScored.toString().plus("/").plus(player.threeAttempted.toString())
                        .plus(" ").plus("(").plus(
                            UtilityClass.roundPercentage(
                                player.threeScored.toDouble() / player.threeAttempted.toDouble(),
                                2
                            ).toString()
                        ).plus("%)")
            }

            //Assists
            assistsView.text = player.assists.toString()

            //Rebounds
            reboundsView.text = player.rebounds.toString()

            //Steals
            stealsView.text = player.steals.toString()

            //Blocks
            blocksView.text = player.blocks.toString()

            //Fouls
            foulsView.text = player.fouls.toString()
        }

    }

    private fun initializeMatch(matchDescription: String, matchPlace: String) {
        val dbHandler = DBHandler(this, null, null, 1)

        val date = UtilityClass.now().split("_")[0]
        println("$matchDescription, $matchPlace, $date")
        match = Match(matchDescription, matchPlace, date)
        matchId = dbHandler.addMatch(match!!).toInt()

    }

    private fun showPopupWindow(button: Button, eventType: String) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_window, null)

        val popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        val missButton = view.findViewById<Button>(R.id.missButton)
        val scoreButton = view.findViewById<Button>(R.id.scoreButton)

        missButton.setOnClickListener {
            popupWindow.dismiss()
            missed(eventType)
        }

        scoreButton.setOnClickListener {
            popupWindow.dismiss()
            made(eventType)
        }

        // Show the popup window anchored to the button
        popupWindow.showAsDropDown(button)
    }

    private fun made(eventType: String) {
        val selectedPlayerIndex = playersList?.indexOf(selectedPlayer)
        if (selectedPlayerIndex != null && selectedPlayerIndex != -1) {
            when (eventType) {
                "one-pointer" -> {
                    playersList?.get(selectedPlayerIndex)!!.oneAttempted += 1
                    playersList?.get(selectedPlayerIndex)!!.oneScored += 1
                }
                "two-pointer" -> {
                    playersList?.get(selectedPlayerIndex)!!.twoAttempted += 1
                    playersList?.get(selectedPlayerIndex)!!.twoScored += 1
                }
                "three-pointer" -> {
                    playersList?.get(selectedPlayerIndex)!!.threeAttempted += 1
                    playersList?.get(selectedPlayerIndex)!!.threeScored += 1
                }
                "assist" -> {
                    playersList?.get(selectedPlayerIndex)!!.assists += 1
                }
                "rebound" -> {
                    playersList?.get(selectedPlayerIndex)!!.rebounds += 1
                }
                "steal" -> {
                    playersList?.get(selectedPlayerIndex)!!.steals += 1
                }
                "block" -> {
                    playersList?.get(selectedPlayerIndex)!!.blocks += 1
                }
                "foul" -> {
                    playersList?.get(selectedPlayerIndex)!!.fouls += 1
                }
            }
        }
        eventOccured(eventType)
        displayStats()
    }

    private fun missed(eventType: String) {
        val selectedPlayerIndex = playersList?.indexOf(selectedPlayer)
        if (selectedPlayerIndex != null && selectedPlayerIndex != -1) {
            when (eventType) {
                "one-pointer" -> {
                    playersList?.get(selectedPlayerIndex)!!.oneAttempted += 1
                }
                "two-pointer" -> {
                    playersList?.get(selectedPlayerIndex)!!.twoAttempted += 1
                }
                "three-pointer" -> {
                    playersList?.get(selectedPlayerIndex)!!.threeAttempted += 1
                }
            }
        }
        displayStats()
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
        if (match != null) {
            val event = Event(
                matchId,
                eventType,
                "${selectedPlayer!!.firstName} ${selectedPlayer!!.lastName} (${selectedPlayer!!.number})",
                videoName
            )
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

        handler.post(object : Runnable {
            override fun run() {
                val hours: Int = seconds / 3600
                val minutes: Int = (seconds % 3600) / 60
                val secs: Int = seconds % 60
                val time: String = String.format("%d:%02d:%02d", hours, minutes, secs)
                timeView.text = time
                seconds++
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
        finish()
    }

}
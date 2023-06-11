package com.example.jamcam

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jamcam.dataclasses.Player
import com.example.jamcam.videorecorder.BackgroundVideoRecorder
import com.example.jamcam.videorecorder.VideoEditor
import java.util.ArrayDeque

class MatchActivity : AppCompatActivity() {

    // Basic
    private var isRecording = false
    private var highlightLength: Int = 11
    private var chosenTypes = listOf("two-pointer", "three-pointer")

    // Match
    private var match: Match? = null
    private var matchId = 0

    // Players
    private var playersList: MutableList<Player>? = mutableListOf()
    private var selectedPlayer: Player? = null
    private lateinit var playerSpinner: Spinner

    // Timer
    private var seconds = 0

    // Highlights
    private var timestampCounter = 0
    private var highlightsCompleted = 0
    private var highlightsCount = 0

    // Undo
    private val moves = ArrayDeque<Move>()
    private lateinit var movesAdapter: MovesAdapter

    // Confirm popup
    private var popupConfirm: PopupWindow? = null

    // Loading animation
    private var popupAnimation: PopupWindow? = null



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
            (intent.getSerializableExtra("playersList") as? ArrayList<Player>)
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


        // Moves
        val movesRecyclerView: RecyclerView = findViewById(R.id.movesRecyclerView)
        movesAdapter = MovesAdapter(emptyList())
        movesRecyclerView.adapter = movesAdapter
        val layoutManager = GridLayoutManager(this, 1)
        movesRecyclerView.layoutManager = layoutManager


        // Buttons
        val endButton: Button = findViewById(R.id.endButton)
        endButton.setOnClickListener { stopRecording() }

        val onePointerButton: Button = findViewById(R.id.onePointerButton)
        onePointerButton.setOnClickListener { showPopupWindow("one-pointer") }
        val twoPointerButton: Button = findViewById(R.id.twoPointerButton)
        twoPointerButton.setOnClickListener { showPopupWindow("two-pointer") }
        val threePointerButton: Button = findViewById(R.id.threePointerButton)
        threePointerButton.setOnClickListener { showPopupWindow("three-pointer") }
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
        val undoButton: Button = findViewById(R.id.undoButton)
        undoButton.setOnClickListener { undo() }

        // Create a match record in database
        initializeMatch(matchDescription.toString(), matchPlace.toString())

        // Get highlight length and chosen types
        highlightLength = intent.getIntExtra("highlightLength", 11)
        val foundChosenTypes = intent.getStringArrayExtra("chosenTypes")
        if(foundChosenTypes != null) {
            chosenTypes = foundChosenTypes.toList()
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        stopRecording()
        popupAnimation?.dismiss()
        popupConfirm?.dismiss()
    }

    private fun displayStats() {

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
            // For FG stat, one-pointers are not considered as they are not field goals :)
            val totalAttempted =
                player.twoAttempted + player.threeAttempted
            val totalMade =
                player.twoScored + player.threeScored

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


            updateMoves(moves.toList())
        }

    }

    private fun updateMoves(moves: List<Move>) {
        movesAdapter.moves = moves
        movesAdapter.notifyDataSetChanged()
    }

    private fun initializeMatch(matchDescription: String, matchPlace: String) {
        val dbHandler = DBHandler(this, null, null, 1)

        val date = UtilityClass.now().split("_")[0]
        match = Match(matchDescription, matchPlace, date)
        matchId = dbHandler.addMatch(match!!).toInt()

    }

    private fun showPopupWindow(eventType: String) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_window, null)

        popupConfirm = PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        val missButton = view.findViewById<Button>(R.id.missButton)
        val scoreButton = view.findViewById<Button>(R.id.scoreButton)

        missButton.setOnClickListener {
            popupConfirm!!.dismiss()
            missed(eventType)
        }

        scoreButton.setOnClickListener {
            popupConfirm!!.dismiss()
            made(eventType)
        }

        val parentView = findViewById<View>(android.R.id.content)
        val gravity = Gravity.CENTER

        popupConfirm!!.showAtLocation(parentView, gravity, 0, 0)
    }

    private fun undo() {
        if (moves.isNotEmpty()) {
            val lastMove = moves.pop()
            val selectedPlayerIndex = playersList?.indexOf(lastMove.player)
            if (selectedPlayerIndex != null && selectedPlayerIndex != -1) {
                val player = playersList!![selectedPlayerIndex]
                when (lastMove.event) {
                    "one-pointer" -> {
                        player.oneAttempted -= 1
                        if (lastMove.result) player.oneScored -= 1
                    }

                    "two-pointer" -> {
                        player.twoAttempted -= 1
                        if (lastMove.result) player.twoScored -= 1
                    }

                    "three-pointer" -> {
                        player.threeAttempted -= 1
                        if (lastMove.result) player.threeScored -= 1
                    }

                    "assist" -> {
                        player.assists -= 1
                    }

                    "rebound" -> {
                        player.rebounds -= 1
                    }

                    "steal" -> {
                        player.steals -= 1
                    }

                    "block" -> {
                        player.blocks -= 1
                    }

                    "foul" -> {
                        player.fouls -= 1
                    }
                }
            }
            val dbHandler = DBHandler(this, null, null, 1)
            if (match != null) {
                val id = dbHandler.findEventId(
                    matchId,
                    lastMove.event,
                    "${selectedPlayer!!.firstName} ${selectedPlayer!!.lastName} (${selectedPlayer!!.number})",
                    lastMove.videoName
                )
                dbHandler.deleteEvent(id)
            }
            displayStats()
        }
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

        var videoName = "no_video"
        var timestamp = -1
        if (eventType in chosenTypes) {
            videoName = "trimmed${matchId}_$timestampCounter.mp4"
            timestamp = reportHighlight()
            timestampCounter += 1
            Toast.makeText(this, "Highlight recorded", Toast.LENGTH_LONG).show()
        }

        val move = Move(playersList!![selectedPlayerIndex!!], eventType, true, videoName, timestamp)
        moves.push(move)

        val dbHandler = DBHandler(this, null, null, 1)
        if (match != null) {
            val event = Event(
                matchId,
                eventType,
                "${playersList!![selectedPlayerIndex].firstName} ${playersList!![selectedPlayerIndex].lastName} (${playersList!![selectedPlayerIndex].number})",
                videoName,
                1
            )
            dbHandler.addEvent(event)
        }

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

        val videoName = "no_video"
        val move = Move(playersList!![selectedPlayerIndex!!], eventType, false, videoName, -1)
        moves.push(move)

        val dbHandler = DBHandler(this, null, null, 1)
        if (match != null) {
            val event = Event(
                matchId,
                eventType,
                "${playersList!![selectedPlayerIndex].firstName} ${playersList!![selectedPlayerIndex].lastName} (${playersList!![selectedPlayerIndex].number})",
                videoName,
                0
            )
            dbHandler.addEvent(event)
        }

        displayStats()
    }

    private fun reportHighlight(): Int {
        val now = UtilityClass.now()
        val cam = UtilityClass.readFile(this, "Timestamps", "camera_start.txt")
        if (cam != null) {
            val diff = UtilityClass.differenceInSeconds(now, cam)
            if (diff >= highlightLength) {
                return diff.toInt()
            }
            return highlightLength
        } else println("No cam file found")
        return -1
    }

    private fun runTimer(timeView: TextView) {
        val handler = Handler()

        handler.post(object : Runnable {
            override fun run() {
                val minutes: Int = (seconds % 3600) / 60
                val secs: Int = seconds % 60
                val time: String = String.format("%02d:%02d", minutes, secs)
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
            createHighlights()
        }
    }

    private fun showPopupLoading() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_loading, null)

        popupAnimation = PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        val loadingView: ImageView = view.findViewById(R.id.loadingView)
        val animation: AnimationDrawable = loadingView.drawable as AnimationDrawable
        animation.start()

        val parentView = findViewById<View>(android.R.id.content)
        val gravity = Gravity.CENTER

        popupAnimation!!.showAtLocation(parentView, gravity, 0, 0)
    }

    private fun checkFinish(videoEditor: VideoEditor) {
        highlightsCompleted += 1
        if (highlightsCompleted == highlightsCount) {
            val handler = Handler()
            handler.postDelayed({
                videoEditor.deleteOriginal(this)
                finish()
            }, 1000)
        }
    }

    private fun createHighlights() {
        val videoEditor = VideoEditor("original.mp4")
        showPopupLoading()

        val movesList = moves.toList()

        // Compute the number of highlights
        for (moveFromList in movesList) {
            if (moveFromList.videoName != "no_video") {
                highlightsCount += 1
            }
        }

        for (i in movesList.indices) {
            val move = movesList[i]
            if (move.videoName != "no_video") {
                val handler = Handler()
                handler.postDelayed({
                    val stop = UtilityClass.secondsToTimestamp(move.timestamp)
                    val start = UtilityClass.substractTime(stop, highlightLength)
                    videoEditor.createHighlight(this, start, stop, move.videoName)
                    checkFinish(videoEditor)
                }, 1000 * i.toLong())
            }

        }
    }


}
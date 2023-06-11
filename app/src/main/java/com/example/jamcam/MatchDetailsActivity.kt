package com.example.jamcam

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import com.example.jamcam.dataclasses.Player

class MatchDetailsActivity : AppCompatActivity() {
    private var matchId: Int = -1
    private lateinit var description: String
    private lateinit var place: String
    private lateinit var date: String

    // Players
    private var playersList: MutableList<Player>? = mutableListOf()
    private var selectedPlayer: Player? = null
    private lateinit var playerSpinner: Spinner

    // Team
    private var team: Player = Player(
        "Total",
        "",
        "all players combined",
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_details)

        // Get match data
        val descriptionGot = intent.getStringExtra("description")
        val placeGot = intent.getStringExtra("place")
        val dateGot = intent.getStringExtra("date")
        if (descriptionGot != null) description = descriptionGot
        if (placeGot != null) place = placeGot
        if (dateGot != null) date = dateGot

        // Set title
        val title: TextView = findViewById(R.id.title)
        title.text = "Here are the key statistics from $description"
        getMatchId()

        // Back button
        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Players spinner
        playersList = computePlayersList()
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
    }

    private fun getMatchId() {
        val dbHandler = DBHandler(this, null, null, 1)
        matchId = dbHandler.getMatchId(description, place, date)
    }

    private fun computePlayersList(): MutableList<Player>? {
        val dbHandler = DBHandler(this, null, null, 1)
        playersList = dbHandler.getPlayersList(matchId)

        for (player in playersList!!) {
            val playerEvents: MutableList<Event> = dbHandler.getMatchPlayerEvents(
                matchId,
                "${player.firstName} ${player.lastName} (${player.number})"
            )

            for(event in playerEvents) {
                when(event.eventType) {
                    "one-pointer" -> {
                        player.oneAttempted += 1
                        team.oneAttempted += 1
                        if(event.result == 1) {
                            player.oneScored += 1
                            team.oneScored += 1
                        }
                    }
                    "two-pointer" -> {
                        player.twoAttempted += 1
                        team.twoAttempted += 1
                        if(event.result == 1) {
                            player.twoScored += 1
                            team.twoScored += 1
                        }
                    }

                    "three-pointer" -> {
                        player.threeAttempted += 1
                        team.threeAttempted += 1
                        if(event.result == 1) {
                            player.threeScored += 1
                            team.threeScored += 1
                        }
                    }

                    "assist" -> {
                        player.assists += 1
                        team.assists += 1
                    }

                    "rebound" -> {
                        player.rebounds += 1
                        team.rebounds += 1
                    }

                    "steal" -> {
                        player.steals += 1
                        team.rebounds += 1
                    }

                    "block" -> {
                        player.blocks += 1
                        team.rebounds += 1
                    }

                    "foul" -> {
                        player.fouls += 1
                        team.rebounds += 1
                    }
                }
            }
        }
        playersList!!.add(0, team)

        return playersList
    }

    private fun displayStats() {
        if (selectedPlayer != null) {
            val player: Player = selectedPlayer as Player
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
        }
    }
}

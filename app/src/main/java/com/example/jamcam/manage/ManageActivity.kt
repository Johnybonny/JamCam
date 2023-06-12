package com.example.jamcam.manage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jamcam.R
import com.example.jamcam.database.DBHandler
import com.example.jamcam.database.Match

class ManageActivity : AppCompatActivity() {

    private var matches: List<Match> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        // Get matches from database
        loadMatches()

        // Display matches
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val adapter = MatchAdapter(matches)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Back button
        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener { finish() }
    }

    private fun loadMatches() {
        val dbHandler = DBHandler(this, null, null, 1)
        matches = dbHandler.getAllMatches()
    }
}
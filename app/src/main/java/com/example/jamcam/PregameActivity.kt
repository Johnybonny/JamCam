package com.example.jamcam

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import java.io.File

class PregameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregame)

        val startMatchButton: Button = findViewById(R.id.startMatchButton)

        startMatchButton.setOnClickListener { startMatch() }

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val playersFragment = PlayersFragment()
        fragmentTransaction.add(R.id.fragment_container, playersFragment)
        fragmentTransaction.commit()


        // Delete previous database
        val dbFile = File("/data/data/com.example.jamcam/databases/jamcamDB.db")
        val journalFile = File("/data/data/com.example.jamcam/databases/jamcamDB.db-journal")

        // Deleting previous files (comment if not needed)
//        dbFile.delete()
//        journalFile.delete()

    }

    private fun startMatch() {
        val intent = Intent(this, MatchActivity::class.java)

        val playersList =
            (supportFragmentManager.findFragmentById(R.id.fragment_container) as? PlayersFragment)?.getPlayersList()
        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
        val matchDescription: String = descriptionEditText.text.toString()
        val placeEditText = findViewById<EditText>(R.id.placeEditText)
        val matchPlace: String = placeEditText.text.toString()
        intent.putExtra("playersList", ArrayList(playersList))
        intent.putExtra("matchDescription", matchDescription)
        intent.putExtra("matchPlace", matchPlace)

        startActivity(intent)
        finish()
    }


}
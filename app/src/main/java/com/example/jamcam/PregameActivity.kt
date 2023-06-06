package com.example.jamcam

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

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

    }

    private fun startMatch() {
        val intent = Intent(this, MatchActivity::class.java)

        val playerList =
            (supportFragmentManager.findFragmentById(R.id.fragment_container) as? PlayersFragment)?.getPlayerList()
        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
        val matchDescription: String =descriptionEditText.text.toString()
        intent.putExtra("playerList", ArrayList(playerList))
        intent.putExtra("matchDescription", matchDescription)

        startActivity(intent)
        finish()
    }


}
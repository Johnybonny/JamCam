package com.example.jamcam

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class PregameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregame)

        val startMatchButton: Button = findViewById(R.id.startMatchButton)

        startMatchButton.setOnClickListener { startMatch() }
    }

    private fun startMatch() {
        val intent = Intent(this, MatchActivity::class.java)
        startActivity(intent)
        finish()
    }
}
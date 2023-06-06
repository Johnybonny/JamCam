package com.example.jamcam

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startPregameButton: Button = findViewById(R.id.startPregameButton)

        startPregameButton.setOnClickListener { startPregame() }
    }

    private fun startPregame() {
        val intent = Intent(this, PregameActivity::class.java)
        startActivity(intent)
    }
}
package com.example.jamcam

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jamcam.dataclasses.PlayType

class SettingsActivity : AppCompatActivity() {

    // Highlight length
    private var highlightLength = 10
    private val values = arrayOf(5, 10, 15)

    // Highlight type
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlayTypeAdapter
    private val playTypes: List<PlayType> = listOf(
        PlayType("one-pointer", false),
        PlayType("two-pointer", true),
        PlayType("three-pointer", true),
        PlayType("assist", false),
        PlayType("rebound", false),
        PlayType("steal", false),
        PlayType("block", false),
        PlayType("foul", false)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Highlights length
        val valueTextView: TextView = findViewById(R.id.valueTextView)
        val slider: SeekBar = findViewById(R.id.slider)
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                highlightLength = values[progress]
                valueTextView.text = highlightLength.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Not needed for this example
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Not needed for this example
            }
        })
        val initialHighlightLength = UtilityClass.readFile(this, "Settings", "highlight_length.txt")
        if (initialHighlightLength != null) {
            highlightLength = initialHighlightLength.toInt()
        }
        slider.progress = values.indexOf(highlightLength - 1)
        valueTextView.text = values[slider.progress].toString()

        // Highlight type
        val initialPlayTypes = UtilityClass.readFile(this, "Settings", "highlight_types.txt")
        if (initialPlayTypes != null) {
            val readTypes = initialPlayTypes.split(",")
            for (type in playTypes) {
                type.highlight = type.name in readTypes
            }
        }

        recyclerView = findViewById(R.id.recyclerView)
        adapter = PlayTypeAdapter(playTypes, convertToList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Save settings
        val saveButton: Button = findViewById(R.id.saveButton)
        saveButton.setOnClickListener { saveAndQuit() }
    }


    private fun convertToList(): List<String> {
        val playTypesList: MutableList<String> = mutableListOf()
        for (playType in playTypes) {
            if (playType.highlight) {
                playTypesList.add(playType.name)
            }
        }
        return playTypesList
    }


    private fun convertToString(): String {
        val playTypesString = ""
        for (playType in playTypes) {
            if (playType.highlight) {
                playTypesString.plus("${playType.name},")
            }
        }
        println(playTypesString)
        return playTypesString
    }

    private fun convertToArray(): Array<String> {
        val playTypesList: MutableList<String> = mutableListOf()
        for (playType in playTypes) {
            if (playType.highlight) {
                playTypesList.add(playType.name)
            }
        }

        return playTypesList.toTypedArray()
    }

//    private fun saveSettings() {
//        println(highlightLength.toString())
//        println(convertToString())
//
//        UtilityClass.saveToFile(
//            this@SettingsActivity, "Settings", "highlight_length.txt",
//            highlightLength.toString()
//        )
//
//        println(convertToString())
//        UtilityClass.saveToFile(
//            this@SettingsActivity, "Settings", "highlight_types.txt",
//            convertToString()
//        )
//    }


    private fun saveAndQuit() {
        val playTypesList = convertToArray()
//        saveSettings()
        val returnIntent = Intent()
        returnIntent.putExtra("highlightLength", highlightLength + 1)
        returnIntent.putExtra("chosenTypes", playTypesList)
        setResult(Activity.RESULT_OK, returnIntent)

        finish()
    }


}
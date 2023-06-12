package com.example.jamcam.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jamcam.R
import com.example.jamcam.dataclasses.PlayType

class PlayTypeAdapter(
    private val playTypes: List<PlayType>,
    private val highlightedTypes: List<String>
) :
    RecyclerView.Adapter<PlayTypeAdapter.PlayTypeViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayTypeViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.play_type_item, parent, false)
        return PlayTypeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlayTypeViewHolder, position: Int) {
        val playType = playTypes[position]
        holder.playNameTextView.text = playType.name
        holder.playCheckBox.isChecked = playType.name in highlightedTypes

        println("${playType.name} ${holder.playCheckBox.isChecked}")

        holder.playCheckBox.setOnCheckedChangeListener(null) // Remove previous listener to prevent unwanted triggering

        holder.playCheckBox.setOnCheckedChangeListener { _, isChecked ->
            playType.highlight = isChecked
        }
    }

    override fun getItemCount(): Int {
        return playTypes.size
    }

    inner class PlayTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playCheckBox: CheckBox = itemView.findViewById(R.id.playCheckBox)
        val playNameTextView: TextView = itemView.findViewById(R.id.playNameTextView)
    }
}
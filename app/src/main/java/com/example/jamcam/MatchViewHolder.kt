package com.example.jamcam

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
    val placeTextView: TextView = itemView.findViewById(R.id.placeTextView)
    val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
}
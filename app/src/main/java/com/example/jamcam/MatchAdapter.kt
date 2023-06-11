package com.example.jamcam

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MatchAdapter(private val matches: List<Match>) : RecyclerView.Adapter<MatchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_match, parent, false)
        return MatchViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]
        holder.descriptionTextView.text = match.description
        holder.placeTextView.text = match.place
        holder.dateTextView.text = match.date

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MatchDetailsActivity::class.java)
            intent.putExtra("description", match.description)
            intent.putExtra("place", match.place)
            intent.putExtra("date", match.date)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return matches.size
    }
}
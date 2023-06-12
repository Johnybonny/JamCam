package com.example.jamcam.match

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jamcam.R
import com.example.jamcam.dataclasses.Move

class MovesAdapter(var moves: List<Move>) : RecyclerView.Adapter<MovesAdapter.MoveViewHolder>() {

    class MoveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val moveLinearLayout: LinearLayout = itemView.findViewById(R.id.moveLinearLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoveViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_move, parent, false)
        return MoveViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MoveViewHolder, position: Int) {
        val move = moves[position]
        val moveTextView = holder.moveLinearLayout.findViewById<TextView>(R.id.moveTextView)

        var moveText = ""
        moveText = if (move.result) "${move.event} by ${move.player.lastName} (${move.player.number})"
        else "Missed ${move.event} by ${move.player.firstName} ${move.player.lastName} (${move.player.number})"
        moveTextView.text= moveText

    }



    override fun getItemCount(): Int {
        return moves.size
    }


}

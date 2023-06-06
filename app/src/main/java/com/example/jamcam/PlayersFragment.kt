package com.example.jamcam

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.io.Serializable

class PlayersFragment : Fragment() {
    private lateinit var playerAdapter: PlayerAdapter
    private val playerList: MutableList<Player> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_players, container, false)

        // Initialize the adapter and set it to the ListView
        playerAdapter = PlayerAdapter(requireContext(), playerList)
        val listView: ListView = view.findViewById(R.id.listView_players)
        listView.adapter = playerAdapter

        // Add player button
        val addButton: Button = view.findViewById(R.id.button_add_player)
        addButton.setOnClickListener {
            addPlayer()
        }

        return view
    }

    fun getPlayerList(): MutableList<Player> {
        return playerList
    }


    private fun addPlayer() {
        val firstNameEditText: EditText = requireView().findViewById(R.id.editText_first_name)
        val lastNameEditText: EditText = requireView().findViewById(R.id.editText_last_name)
        val numberEditText: EditText = requireView().findViewById(R.id.editText_number)

        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val number = numberEditText.text.toString().trim()

        if (firstName.isNotEmpty() && lastName.isNotEmpty() && number.isNotEmpty()) {
            val player = Player(firstName, lastName, number)
            playerList.add(player)
            playerAdapter.notifyDataSetChanged()

            // Clear input fields
            firstNameEditText.text.clear()
            lastNameEditText.text.clear()
            numberEditText.text.clear()
        }
    }

    inner class PlayerAdapter(
        private val context: Context,
        private val players: List<Player>
    ) : ArrayAdapter<Player>(context, 0, players) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            if (itemView == null) {
                itemView = LayoutInflater.from(context).inflate(R.layout.item_player, parent, false)
            }

            val player = players[position]

            // Set player information to the item view
            val firstNameTextView: TextView = itemView!!.findViewById(R.id.textView_first_name)
            val lastNameTextView: TextView = itemView.findViewById(R.id.textView_last_name)
            val numberTextView: TextView = itemView.findViewById(R.id.textView_number)

            firstNameTextView.text = player.firstName
            lastNameTextView.text = player.lastName
            numberTextView.text = player.number

            // Remove player button
            val removeButton: Button = itemView.findViewById(R.id.button_remove_player)
            removeButton.setOnClickListener {
                removePlayer(position)
            }

            return itemView
        }

        private fun removePlayer(position: Int) {
            playerList.removeAt(position)
            notifyDataSetChanged()
        }
    }

    data class Player(val firstName: String, val lastName: String, val number: String) :
        Serializable
}

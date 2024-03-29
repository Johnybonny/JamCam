package com.example.jamcam.pregame

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.jamcam.R
import com.example.jamcam.UtilityClass
import com.example.jamcam.dataclasses.Player

class PlayersFragment : Fragment() {
    private lateinit var playerAdapter: PlayerAdapter
    private val playersList: MutableList<Player> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_players, container, false)

        // Initialize the adapter and set it to the ListView
        playerAdapter = PlayerAdapter(requireContext(), playersList)
        val listView: ListView = view.findViewById(R.id.listView_players)
        listView.adapter = playerAdapter

        // Add player button
        val addButton: Button = view.findViewById(R.id.button_add_player)
        addButton.setOnClickListener {
            addPlayer()
        }

        val firstNameEditText: EditText = view.findViewById(R.id.editText_first_name)
        val numberEditText: EditText = view.findViewById(R.id.editText_number)
        val lastNameEditText: EditText = view.findViewById(R.id.editText_last_name)

        firstNameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == 0) {
                lastNameEditText.requestFocus()
                true
            } else {
                false
            }
        }

        lastNameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == 0) {
                numberEditText.requestFocus()
                true
            } else {
                false
            }
        }

        return view
    }

    fun getPlayersList(): MutableList<Player> {
        return playersList
    }


    private fun addPlayer() {
        println("Hello")
        val firstNameEditText: EditText = requireView().findViewById(R.id.editText_first_name)
        val lastNameEditText: EditText = requireView().findViewById(R.id.editText_last_name)
        val numberEditText: EditText = requireView().findViewById(R.id.editText_number)

        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val number = numberEditText.text.toString().trim()

        if (firstName.isNotEmpty() && lastName.isNotEmpty() && number.isNotEmpty()) {
            if (UtilityClass.isNumeric(number)) {
                if (UtilityClass.isAlpha(firstName) && UtilityClass.isAlpha(lastName)) {
                    val player = Player(
                        firstName,
                        lastName,
                        number,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0
                    )
                    playersList.add(player)
                    playerAdapter.notifyDataSetChanged()

                    // Clear input fields
                    firstNameEditText.text.clear()
                    lastNameEditText.text.clear()
                    numberEditText.text.clear()
                } else {
                    Toast.makeText(requireContext(), "Name must be alphanumeric", Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                Toast.makeText(requireContext(), "Number is incorrect", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(requireContext(), "Player data is incomplete", Toast.LENGTH_LONG).show()
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
            playersList.removeAt(position)
            notifyDataSetChanged()
        }
    }

}

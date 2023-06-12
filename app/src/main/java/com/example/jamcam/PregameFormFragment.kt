package com.example.jamcam

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class PregameFormFragment : Fragment() {

    private var moveRightListener: MoveRightListener? = null

    lateinit var placeEditText: EditText
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val place = data.getStringExtra("address")
                    if (place != null) {
                        placeEditText.setText(place, TextView.BufferType.EDITABLE)
                    }
                }
            }
        }

    interface MoveRightListener {
        fun onMoveRight()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MoveRightListener) {
            moveRightListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        moveRightListener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val moveRightButton: Button = view.findViewById(R.id.moveRightButton)
        placeEditText = view.findViewById(R.id.etPlace)
        moveRightButton.setOnClickListener {
            moveRightListener?.onMoveRight()
        }
        placeEditText.setOnClickListener {
            startMap()
        }
    }

    private fun startMap() {
        val intent = Intent(requireContext(), MapActivity::class.java)
        resultLauncher.launch(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pregame_form, container, false)
    }

}
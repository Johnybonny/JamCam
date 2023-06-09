package com.example.jamcam

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class PregameFormFragment : Fragment() {

    private var moveRightListener: MoveRightListener? = null

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
        moveRightButton.setOnClickListener {
            moveRightListener?.onMoveRight()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pregame_form, container, false)
    }

}
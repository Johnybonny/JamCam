package com.example.jamcam

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import java.io.File

class PregameActivity : AppCompatActivity(), TabLayout.OnTabSelectedListener, PregameFormFragment.MoveRightListener {

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private var currentTabPosition = 0

    private var highlightLength: Int = 11
    private var chosenTypes = listOf("two-pointer", "three-pointer")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregame)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        // Set up the ViewPager with the adapter
        val adapter = PregamePagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter

        // Link the TabLayout and ViewPager
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.addOnTabSelectedListener(this)

        // Set custom tab indicators
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            tab!!.id = i
        }

        val startMatchButton: Button = findViewById(R.id.startMatchButton)
        startMatchButton.setOnClickListener { startMatch() }

        highlightLength = intent.getIntExtra("highlightLength", 11)
        val foundChosenTypes = intent.getStringArrayExtra("chosenTypes")
        if(foundChosenTypes != null) {
            chosenTypes = foundChosenTypes.toList()
        }


    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        val tabId = tab?.id
        if(tabId!! == 0) {
            val dot1: ImageView = findViewById(R.id.dot1)
            dot1.setImageResource(R.drawable.tab_indicator_dot_selected)

        } else {
            val dot2: ImageView = findViewById(R.id.dot2)
            dot2.setImageResource(R.drawable.tab_indicator_dot_selected)
        }
        currentTabPosition = tabId
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        val tabId = tab?.id
        println(tabId)
        if(tabId == 0) {
            val dot1: ImageView = findViewById(R.id.dot1)
            dot1.setImageResource(R.drawable.tab_indicator_dot_unselected)
        } else {
            val dot2: ImageView = findViewById(R.id.dot2)
            dot2.setImageResource(R.drawable.tab_indicator_dot_unselected)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        // Handle tab reselection if needed
    }

    // Custom FragmentPagerAdapter for the tabs
    inner class PregamePagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount(): Int {
            return 2
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> PregameFormFragment()
                1 -> PlayersFragment()
                else -> throw IllegalStateException("Invalid tab position")
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> "Form"
                1 -> "Players"
                else -> null
            }
        }


    }

    override fun onMoveRight() {
        moveRight()
    }

    private fun moveRight() {
        currentTabPosition++
        if (currentTabPosition < viewPager.adapter?.count ?: 0) {
            viewPager.currentItem = currentTabPosition
        }
    }

    private fun startMatch() {



        val playersList =
            (supportFragmentManager.findFragmentById(R.id.viewPager) as? PlayersFragment)?.getPlayersList()
        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
        val matchDescription: String = descriptionEditText.text.toString()
        val placeEditText = findViewById<EditText>(R.id.placeEditText)
        val matchPlace: String = placeEditText.text.toString()

        if (playersList!!.isEmpty()) {
                Toast.makeText(this, "Add players to continue", Toast.LENGTH_LONG).show()
        } else if (matchDescription.isBlank()) {
            Toast.makeText(this, "Add match description to continue", Toast.LENGTH_LONG).show()
        } else if (matchPlace.isBlank()) {
            Toast.makeText(this, "Add match place to continue", Toast.LENGTH_LONG).show()
        } else {
            val intent = Intent(this, MatchActivity::class.java)
            intent.putExtra("playersList", ArrayList(playersList))
            intent.putExtra("matchDescription", matchDescription)
            intent.putExtra("matchPlace", matchPlace)
            intent.putExtra("highlightLength", highlightLength)
            intent.putExtra("chosenTypes", chosenTypes.toTypedArray())

            startActivity(intent)
            finish()
        }


    }


}
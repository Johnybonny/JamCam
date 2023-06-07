package com.example.jamcam

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import java.io.File

class ReplaysActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_replays)

        val viewPagerReplays: ViewPager2 = findViewById(R.id.viewPagerReplays)

        val path = "${applicationContext.filesDir.path}/replays"
        val replays = mutableListOf<Replay>()

        val replayDirectory = File(path)
        if (replayDirectory.exists() && replayDirectory.isDirectory) {
            replayDirectory.listFiles()?.let { files ->
                for (file in files) {
                    val replay = Replay(
                        file.absolutePath,
                        file.name,
                        "Short video" // You can customize this description as needed
                    )
                    replays.add(replay)
                }
            }
        }

        viewPagerReplays.adapter = ReplaysAdapter(replays)

    }
}
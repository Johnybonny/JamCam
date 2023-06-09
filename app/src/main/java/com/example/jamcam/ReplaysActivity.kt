package com.example.jamcam

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.jamcam.databinding.ActivityReplaysBinding
import com.example.jamcam.videoplayer.ExoPlayerItem
import com.example.jamcam.videoplayer.Video
import com.example.jamcam.videoplayer.VideoAdapter
import java.io.File

class ReplaysActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReplaysBinding
    private lateinit var adapter: VideoAdapter
    private val videos = ArrayList<Video>()
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReplaysBinding.inflate(layoutInflater)

        binding.fab.setOnClickListener {
            finish()
        }

        binding.fab2.setOnClickListener {
            val nowPlayingIndex = exoPlayerItems.indexOfFirst { it.exoPlayer.isPlaying }
            if (nowPlayingIndex != -1) {
                val video = videos[nowPlayingIndex]

                val requestFile = File(video.url)
                val fileUri: Uri? = try {
                    FileProvider.getUriForFile(
                        this@ReplaysActivity,
                        "com.example.jamcam.fileprovider",
                        requestFile
                    )
                } catch (e: IllegalArgumentException) {
                    Log.e(
                        "File Selector",
                        "The selected file can't be shared: $requestFile"
                    )
                    null
                }

                startActivity(
                    Intent.createChooser(
                        Intent().setAction(Intent.ACTION_SEND)
                            .setType("video/*")
                            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            .putExtra(
                                Intent.EXTRA_STREAM,
                                fileUri
                            ), resources.getString(R.string.share_video)
                    )
                )
            }
        }
        setContentView(binding.root)

        val path = "${applicationContext.filesDir.path}/replays"

        val replayDirectory = File(path)
        if (replayDirectory.exists() && replayDirectory.isDirectory) {
            replayDirectory.listFiles()?.let { files ->
                for (file in files) {

                    val videoName = file.name
                    val regex = Regex("""\d+(?=\D)""")
                    val numbers = regex.findAll(videoName)
                        .map { it.value.toInt() }
                        .toList()
                    val matchId = numbers[0]

                    val dbHandler = DBHandler(this, null, null, 1)
                    val match = dbHandler.getMatch(matchId)
                    val event = dbHandler.getEvent(videoName)

                    val videoTitle = match.description.capitalize()
                    val videoDescription = "${event.eventType.capitalize()} by ${event.player}"

                    val video = Video(
                        videoTitle,
                        videoDescription,
                        file.absolutePath,
                    )
                    videos.add(video)
                }
            }
        }

        adapter = VideoAdapter(this, videos, object : VideoAdapter.OnVideoPreparedListener {
            override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                exoPlayerItems.add(exoPlayerItem)
            }
        })

        binding.viewPager2.adapter = adapter

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val previousIndex = exoPlayerItems.indexOfFirst { it.exoPlayer.isPlaying }
                if (previousIndex != -1) {
                    val player = exoPlayerItems[previousIndex].exoPlayer
                    player.pause()
                    player.playWhenReady = false
                }
                val newIndex = exoPlayerItems.indexOfFirst { it.position == position }
                if (newIndex != -1) {
                    val player = exoPlayerItems[newIndex].exoPlayer
                    player.seekTo(0)
                    player.playWhenReady = true
                    player.play()
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()

        val index = exoPlayerItems.indexOfFirst { it.position == binding.viewPager2.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.pause()
            player.playWhenReady = false
        }
    }

    override fun onResume() {
        super.onResume()

        val index = exoPlayerItems.indexOfFirst { it.position == binding.viewPager2.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.playWhenReady = true
            player.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (exoPlayerItems.isNotEmpty()) {
            for (item in exoPlayerItems) {
                val player = item.exoPlayer
                player.stop()
                player.clearMediaItems()
            }
        }
    }
}
package com.example.jamcam

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
        setContentView(binding.root)

        binding.fabBack.setOnClickListener {
            finish()
        }

        binding.fabShare.setOnClickListener {
            shareCurrentVideo()
        }

        binding.fabDelete.setOnClickListener {
            deleteCurrentVideo()
        }

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
                val previousIndex = exoPlayerItems.indexOfFirst { it.exoPlayer.playWhenReady }
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

    private fun shareCurrentVideo() {
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

    private fun deleteCurrentVideo() {
        val nowPlayingIndex = binding.viewPager2.currentItem
        if (nowPlayingIndex != -1) {
            println("nowPlayingIndex: $nowPlayingIndex")

            // pause the video
            var player = exoPlayerItems[nowPlayingIndex].exoPlayer
            player.pause()
            player.playWhenReady = false
            val video = videos[nowPlayingIndex]

            // figure out the next video to play
            val nextPlayingIndex: Int = if (nowPlayingIndex == videos.size - 1) {
                // last video, move to the previous one
                nowPlayingIndex - 1
            } else {
                // play next video, video[nowPlayingIndex] will be deleted
                nowPlayingIndex
            }
            println(nextPlayingIndex)

            if (nextPlayingIndex == -1) {
                // if this is the last video, delete it and exit activity
                // delete file
                val fileToDelete = File(video.url)
                fileToDelete.delete()
                // exit
                finish()
            } else {
                // there is another video to play!
                // delete data from videos and exoPlayerItems
                videos.removeAt(nowPlayingIndex)
                exoPlayerItems.removeAt(nowPlayingIndex)

                // notify adapter about the change, move to the next video
                binding.viewPager2.adapter?.notifyItemRemoved(nowPlayingIndex)
                binding.viewPager2.setCurrentItem(nextPlayingIndex, true)

                // update exoplayeritems positions
                updateExoPlayerItems(nowPlayingIndex)

                // play the next video if it is loaded into exoPlayerItems
                // if it's not, it will be played automatically
                val exists = exoPlayerItems.any { it.position == nextPlayingIndex }
                if (exists) {
                    player = exoPlayerItems[nextPlayingIndex].exoPlayer
                    player.seekTo(0)
                    player.playWhenReady = true
                    player.play()
                }

                // delete file
                val fileToDelete = File(video.url)
                fileToDelete.delete()
            }
        }
    }

    private fun printVideos() {
        println("----------- VIDEOS -------------")
        for (video: Video in videos) {
            println(video.title + " " + video.description + " " + video.url)
        }
        println("---")
    }

    private fun printExoPlayers() {
        println("----------- EXOPLAYERS -------------")
        for (exoplayer: ExoPlayerItem in exoPlayerItems) {
            println(exoplayer.position.toString() + " " + exoplayer.exoPlayer.playWhenReady.toString())
        }
        println("---")
    }

    private fun updateExoPlayerItems(indexOfDeleted: Int) {
        for (i in indexOfDeleted until exoPlayerItems.size) {
            exoPlayerItems[i].position = i
        }
    }
}

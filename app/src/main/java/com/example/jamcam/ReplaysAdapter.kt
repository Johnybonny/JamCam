package com.example.jamcam

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView

class ReplaysAdapter(replaysIn: List<Replay>) :
    RecyclerView.Adapter<ReplaysAdapter.ReplayViewHolder>() {

    var replays: List<Replay> = replaysIn

    class ReplayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var videoView: VideoView
        var textVideoTitle: TextView
        var textVideoDescription: TextView
        var videoProgressBar: ProgressBar

        init {
            videoView = itemView.findViewById(R.id.videoView)
            textVideoTitle = itemView.findViewById(R.id.textVideoTitle)
            textVideoDescription = itemView.findViewById(R.id.textVideoDescription)
            videoProgressBar = itemView.findViewById(R.id.videoProgressBar)
        }

        fun setVideoData(replay: Replay) {
            textVideoTitle.text = "replay.videoTitle"
            textVideoDescription.text = replay.videoDescription
            videoView.setVideoPath(replay.videoPath)
            videoView.setOnPreparedListener { mp ->
                videoProgressBar.visibility = View.GONE
                mp.start()

                val videoRatio = mp.videoWidth.toFloat() / mp.videoHeight
                val screenRatio = videoView.width.toFloat() / mp.videoHeight
                val scale = videoRatio / screenRatio
                if (scale >= 1F) {
                    videoView.scaleX = scale
                } else {
                    videoView.scaleY = 1F / scale
                }
            }
            videoView.setOnCompletionListener { mp ->
                mp.start()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplayViewHolder {
        return ReplayViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_container_video,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return this.replays.size
    }

    override fun onBindViewHolder(holder: ReplayViewHolder, position: Int) {
        holder.setVideoData(replays[position])
    }
}
package com.example.jamcam

class Move (
    player: PlayersFragment.Player,
    event: String,
    result: Boolean,
    videoName: String,
    timestamp: Int
) {
    var player: PlayersFragment.Player
    var event: String
    var result: Boolean
    var videoName: String
    var timestamp: Int

    init {
        this.player = player
        this.event = event
        this.result = result
        this.videoName = videoName
        this.timestamp = timestamp
    }

}
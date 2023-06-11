package com.example.jamcam

data class Move (
    var player: PlayersFragment.Player,
    var event: String,
    var result: Boolean,
    var videoName: String,
    var timestamp: Int
)
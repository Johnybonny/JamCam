package com.example.jamcam

import com.example.jamcam.dataclasses.Player

data class Move (
    var player: Player,
    var event: String,
    var result: Boolean,
    var videoName: String,
    var timestamp: Int
)
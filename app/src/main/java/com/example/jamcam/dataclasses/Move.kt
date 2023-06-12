package com.example.jamcam.dataclasses

data class Move (
    var player: Player,
    var event: String,
    var result: Boolean,
    var videoName: String,
    var timestamp: Int
)
package com.example.jamcam.database

data class Event (
    var matchId: Int,
    var eventType: String,
    var player: String,
    var video: String,
    var result: Int // 1 or 0
    )
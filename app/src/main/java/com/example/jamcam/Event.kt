package com.example.jamcam

class Event (
    matchId: Int,
    eventType: String,
    player: String,
    video: String
    ) {
        var matchId: Int
        var eventType: String
        var player: String
        var video: String

        init {
            this.matchId = matchId
            this.eventType = eventType
            this.player = player
            this.video = video
        }
    }
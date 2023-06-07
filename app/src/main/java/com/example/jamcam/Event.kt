package com.example.jamcam

class Event (
//    id: Int,
    matchId: Int,
    eventType: String,
    player: String,
    video: String
    ) {
//        var id: Int
        var matchId: Int
        var eventType: String
        var player: String
        var video: String

        init {
//            this.id = id
            this.matchId = matchId
            this.eventType = eventType
            this.player = player
            this.video = video
        }
    }
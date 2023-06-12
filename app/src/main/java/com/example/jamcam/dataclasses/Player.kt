package com.example.jamcam.dataclasses

import java.io.Serializable

data class Player(
    val firstName: String,
    val lastName: String,
    val number: String, var oneAttempted: Int,
    var oneScored: Int,
    var twoAttempted: Int,
    var twoScored: Int,
    var threeAttempted: Int,
    var threeScored: Int,
    var assists: Int,
    var blocks: Int,
    var steals: Int,
    var fouls: Int,
    var rebounds: Int
) :
    Serializable
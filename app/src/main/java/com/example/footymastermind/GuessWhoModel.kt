package com.example.footymastermind

import kotlin.random.Random

data class Player(
    val name: String = "",
    val image: String = ""
)

data class GuessWhoModel(
    var gameId: String = "-1",
    var winner: String = "",
    var gameStatus: GuessGameStatus = GuessGameStatus.CREATED,
    var currentPlayer: String = (arrayOf("Green", "Red"))[Random.nextInt(2)],
    var selectedPlayers: List<Player> = emptyList(),
    val targetPlayerRed: Player? = null,
    val targetPlayerGreen: Player? = null,
    val currentQuestion: String = ""
)

enum class GuessGameStatus {
    CREATED,
    JOINED,
    INPROGRESS,
    FINISHED
}
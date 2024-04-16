package com.example.footymastermind

import kotlin.random.Random

data class TicTacToeModel (
    var gameId : String = "-1",
    var filledPos : MutableList<String> = mutableListOf("","","","","","","","","","","","","","","",""),
    var winner : String = "",
    var gameStatus : GameStatus = GameStatus.CREATED,
    var currentPlayer : String = (arrayOf("Green","Red"))[Random.nextInt(2)]
)

enum class GameStatus {
    CREATED,
    JOINED,
    INPROGRESS,
    FINISHED
}

package com.example.footymastermind

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.footymastermind.databinding.ActivityStartGuessWhoBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator

class GuessWhoStart : AppCompatActivity(), View.OnClickListener {
    lateinit var guessWhoBinding: ActivityStartGuessWhoBinding

    private var guessWhoModel : GuessWhoModel? = null

    val database =
        FirebaseDatabase.getInstance("https://footymastermindapp-default-rtdb.europe-west1.firebasedatabase.app/")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        guessWhoBinding = ActivityStartGuessWhoBinding.inflate(layoutInflater)
        setContentView(guessWhoBinding.root)

        GuessWhoData.fetchGuessWhoModel()

        guessWhoBinding.startGameButton.setOnClickListener {
            startGame()
        }

        GuessWhoData.guessWhoModel.observe(this){
            guessWhoModel = it
            setUI()
        }

    }

    fun setUI() {
        guessWhoModel?.apply{

            guessWhoBinding.startGameButton.visibility = View.VISIBLE

            guessWhoBinding.gameStatusText.text =
                when(gameStatus){
                    GameStatus.CREATED -> {
                        guessWhoBinding.startGameButton.visibility = View.INVISIBLE
                        "Game ID: " + gameId
                    }
                    GameStatus.JOINED -> {
                        "Click on start game"
                    }
                    GameStatus.INPROGRESS -> {
                        guessWhoBinding.startGameButton.visibility = View.INVISIBLE
                        when(GuessWhoData.myID) {
                            currentPlayer -> "Your turn"
                            else -> currentPlayer + " turn"
                        }
                    }
                    GameStatus.FINISHED -> {
                        if(winner.isNotEmpty()) {
                            when(GuessWhoData.myID){
                                winner -> "You won!"
                                else -> winner + "won!"
                            }
                        }
                        else "DRAW"
                    }
                }
        }
    }

    fun startGame() {

        guessWhoModel?.apply {
            updateGameData(
                TicTacToeModel(
                    gameId = gameId,
                    filledPos = filledPos,
                    winner = winner,
                    gameStatus = GameStatus.INPROGRESS,
                    currentPlayer = currentPlayer
                )
            )
        }

    }


    fun updateGameData(model : TicTacToeModel){
        TicTacToeData.saveTicTacToeModel(model)
    }

    override fun onClick(v: View?) {
        guessWhoModel?.apply {
            if(gameStatus!= GameStatus.INPROGRESS){
                Toast.makeText(applicationContext,"Game not started",Toast.LENGTH_SHORT).show()
                return
            }

            //game is in progress
            if(gameId!="-1" && currentPlayer!=GuessWhoData.myID ){
                Toast.makeText(applicationContext,"Not your turn",Toast.LENGTH_SHORT).show()
                return
            }

        }
    }

}

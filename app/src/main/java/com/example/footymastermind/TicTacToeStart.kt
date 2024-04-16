package com.example.footymastermind

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.footymastermind.databinding.ActivityStartTicTacToeBinding

class TicTacToeStart : AppCompatActivity(), View.OnClickListener {

    lateinit var ticTacToeBinding: ActivityStartTicTacToeBinding

    private var ticTacToeModel : TicTacToeModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ticTacToeBinding = ActivityStartTicTacToeBinding.inflate(layoutInflater)
        setContentView(ticTacToeBinding.root)

        TicTacToeData.fetchTicTacToeModel()

        ticTacToeBinding.gr22.setOnClickListener(this)
        ticTacToeBinding.gr23.setOnClickListener(this)
        ticTacToeBinding.gr24.setOnClickListener(this)
        ticTacToeBinding.gr33.setOnClickListener(this)
        ticTacToeBinding.gr44.setOnClickListener(this)


        ticTacToeBinding.startGameButton.setOnClickListener {
            startGame()
        }

        TicTacToeData.ticTacToeModel.observe(this){
            ticTacToeModel = it
            setUI()
        }
    }

    fun setUI() {
        ticTacToeModel?.apply{
//            ticTacToeBinding.gr12.text = Editable.Factory.getInstance().newEditable(filledPos[6])
//            ticTacToeBinding.gr13.text = Editable.Factory.getInstance().newEditable(filledPos[2])
//            ticTacToeBinding.gr14.text = Editable.Factory.getInstance().newEditable(filledPos[3])
//            ticTacToeBinding.gr21.text = Editable.Factory.getInstance().newEditable(filledPos[4])
//            ticTacToeBinding.gr22.text = Editable.Factory.getInstance().newEditable(filledPos[5])
////            ticTacToeBinding.gr23.setText(filledPos[6])
////            ticTacToeBinding.gr23.setBackgroundColor(ContextCompat.getColor(ticTacToeBinding.root.context, R.color.green))
//            ticTacToeBinding.gr24.text = Editable.Factory.getInstance().newEditable(filledPos[7])
//            ticTacToeBinding.gr31.text = Editable.Factory.getInstance().newEditable(filledPos[8])
//            ticTacToeBinding.gr32.text = Editable.Factory.getInstance().newEditable(filledPos[9])
//            ticTacToeBinding.gr33.text = Editable.Factory.getInstance().newEditable(filledPos[10])
//            ticTacToeBinding.gr34.text = Editable.Factory.getInstance().newEditable(filledPos[11])
//            ticTacToeBinding.gr41.text = Editable.Factory.getInstance().newEditable(filledPos[12])
//            ticTacToeBinding.gr42.text = Editable.Factory.getInstance().newEditable(filledPos[13])
//            ticTacToeBinding.gr43.text = Editable.Factory.getInstance().newEditable(filledPos[14])
//            ticTacToeBinding.gr44.text = Editable.Factory.getInstance().newEditable(filledPos[15])

//            ticTacToeBinding.gr22.text = filledPos[0]
            when (filledPos[0]) {
                "Green" -> ticTacToeBinding.gr22.backgroundTintList = ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.green)
                "Red" -> ticTacToeBinding.gr22.backgroundTintList = ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.black)
                else -> ticTacToeBinding.gr22.backgroundTintList = ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.white)
            }
            when (filledPos[1]) {
                "Green" -> ticTacToeBinding.gr23.backgroundTintList = ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.green)
                "Red" -> ticTacToeBinding.gr23.backgroundTintList = ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.black)
                else -> ticTacToeBinding.gr23.backgroundTintList = ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.white)
            }
            when (filledPos[2]) {
                "Green" -> ticTacToeBinding.gr24.backgroundTintList = ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.green)
                "Red" -> ticTacToeBinding.gr24.backgroundTintList = ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.black)
                else -> ticTacToeBinding.gr24.backgroundTintList = ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.white)
            }
            when (filledPos[4]) {
                "Green" -> ticTacToeBinding.gr33.backgroundTintList = ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.green)
                "Red" -> ticTacToeBinding.gr33.backgroundTintList = ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.black)
                else -> ticTacToeBinding.gr33.backgroundTintList = ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.white)
            }
            when (filledPos[8]) {
                "Green" -> ticTacToeBinding.gr44.backgroundTintList = ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.green)
                "Red" -> ticTacToeBinding.gr44.backgroundTintList = ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.black)
                else -> ticTacToeBinding.gr44.backgroundTintList = ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.white)
            }
//            ticTacToeBinding.gr23.text = filledPos[1]
//            ticTacToeBinding.gr24.text = filledPos[2]
//            ticTacToeBinding.gr33.text = filledPos[4]
//            ticTacToeBinding.gr44.text = filledPos[8]

            ticTacToeBinding.startGameButton.visibility = View.VISIBLE

            ticTacToeBinding.gameStatusText.text =
                when(gameStatus){
                    GameStatus.CREATED -> {
                        ticTacToeBinding.startGameButton.visibility = View.INVISIBLE
                        "Game ID: " + gameId
                    }
                    GameStatus.JOINED -> {
                        "Click on start game"
                    }
                    GameStatus.INPROGRESS -> {
                        ticTacToeBinding.startGameButton.visibility = View.INVISIBLE
                        when(TicTacToeData.myID) {
                            currentPlayer -> "Your turn"
                            else -> currentPlayer + " turn"
                        }
                    }
                    GameStatus.FINISHED -> {
                        if(winner.isNotEmpty()) {
                            when(TicTacToeData.myID){
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
        ticTacToeModel?.apply {
            updateGameData(
                TicTacToeModel(
                    gameId = gameId,
                    gameStatus = GameStatus.INPROGRESS
                )
            )
        }
    }

    fun updateGameData(model : TicTacToeModel){
        TicTacToeData.saveTicTacToeModel(model)
    }

    fun checkForWinner() {
        val winningPos = arrayOf(
            intArrayOf(0, 1, 2),
            intArrayOf(3, 4, 5),
            intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6),
            intArrayOf(1, 4, 7),
            intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8),
            intArrayOf(2, 4, 6),
        )

        ticTacToeModel?.apply {
            for (i in winningPos) {
                if (
                    filledPos[i[0]] == filledPos[i[1]] &&
                    filledPos[i[1]] == filledPos[i[2]] &&
                    filledPos[i[0]].isNotEmpty()
                ) {
                    gameStatus = GameStatus.FINISHED
                    winner = filledPos[i[0]]
                }
            }

            if (filledPos.none() { it.isEmpty() }) {
                gameStatus = GameStatus.FINISHED
            }


            updateGameData(this)

        }
    }

    override fun onClick(v: View?) {
        ticTacToeModel?.apply {
            if(gameStatus!= GameStatus.INPROGRESS){
                Toast.makeText(applicationContext,"Game not started",Toast.LENGTH_SHORT).show()
                return
            }

            //game is in progress
            if(gameId!="-1" && currentPlayer!=TicTacToeData.myID ){
                Toast.makeText(applicationContext,"Not your turn",Toast.LENGTH_SHORT).show()
                return
            }

            val clickedPos =(v?.tag  as String).toInt()
            if(filledPos[clickedPos].isEmpty()){
                filledPos[clickedPos] = currentPlayer
                currentPlayer = if(currentPlayer=="Red") "Green" else "Red"
                checkForWinner()
                updateGameData(this)
            }

        }
    }
}
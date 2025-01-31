package com.example.footymastermind

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.footymastermind.databinding.ActivityStartTicTacToeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlin.random.Random

class TicTacToeStart : AppCompatActivity(), View.OnClickListener {

    lateinit var ticTacToeBinding: ActivityStartTicTacToeBinding

    private var ticTacToeModel: TicTacToeModel? = null

    val database =
        FirebaseDatabase.getInstance("https://footymastermindapp-default-rtdb.europe-west1.firebasedatabase.app/")
    private var turnStartTime: Long = 0
    private var switchCount: Int = 0
    private val TURN_DURATION_MS = 55000L // 10 seconds
    private val MAX_SWITCHES = 20


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ticTacToeBinding = ActivityStartTicTacToeBinding.inflate(layoutInflater)
        setContentView(ticTacToeBinding.root)

        TicTacToeData.fetchTicTacToeModel()

        ticTacToeBinding.gr22.setOnClickListener(this)
        ticTacToeBinding.gr23.setOnClickListener(this)
        ticTacToeBinding.gr24.setOnClickListener(this)
        ticTacToeBinding.gr32.setOnClickListener(this)
        ticTacToeBinding.gr33.setOnClickListener(this)
        ticTacToeBinding.gr34.setOnClickListener(this)
        ticTacToeBinding.gr42.setOnClickListener(this)
        ticTacToeBinding.gr43.setOnClickListener(this)
        ticTacToeBinding.gr44.setOnClickListener(this)


        ticTacToeBinding.startGameButton.setOnClickListener {
            startGame()
        }

        ticTacToeBinding.exitButton.setOnClickListener {
            redirectToLoss()
        }

        TicTacToeData.ticTacToeModel.observe(this) {
            ticTacToeModel = it
            setUI()
        }
    }

    fun setUI() {
        ticTacToeModel?.apply {

            ticTacToeBinding.gr21.setText(selectedClubs.getOrNull(0) ?: "")
            ticTacToeBinding.gr31.setText(selectedClubs.getOrNull(1) ?: "")
            ticTacToeBinding.gr41.setText(selectedClubs.getOrNull(2) ?: "")

            // Update columns (countries) for both players
            ticTacToeBinding.gr12.setText(selectedCountries.getOrNull(0) ?: "")
            ticTacToeBinding.gr13.setText(selectedCountries.getOrNull(1) ?: "")
            ticTacToeBinding.gr14.setText(selectedCountries.getOrNull(2) ?: "")

            when (filledPos[0]) {
                "Green" -> ticTacToeBinding.gr22.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.green)

                "Red" -> ticTacToeBinding.gr22.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.red)

                else -> ticTacToeBinding.gr22.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.white)
            }
            when (filledPos[1]) {
                "Green" -> ticTacToeBinding.gr23.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.green)

                "Red" -> ticTacToeBinding.gr23.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.red)

                else -> ticTacToeBinding.gr23.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.white)
            }
            when (filledPos[2]) {
                "Green" -> ticTacToeBinding.gr24.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.green)

                "Red" -> ticTacToeBinding.gr24.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.red)

                else -> ticTacToeBinding.gr24.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.white)
            }
            when (filledPos[3]) {
                "Green" -> ticTacToeBinding.gr32.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.green)

                "Red" -> ticTacToeBinding.gr32.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.red)

                else -> ticTacToeBinding.gr32.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.white)
            }
            when (filledPos[4]) {
                "Green" -> ticTacToeBinding.gr33.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.green)

                "Red" -> ticTacToeBinding.gr33.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.red)

                else -> ticTacToeBinding.gr33.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.white)
            }
            when (filledPos[5]) {
                "Green" -> ticTacToeBinding.gr34.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.green)

                "Red" -> ticTacToeBinding.gr34.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.red)

                else -> ticTacToeBinding.gr34.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.white)
            }
            when (filledPos[6]) {
                "Green" -> ticTacToeBinding.gr42.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.green)

                "Red" -> ticTacToeBinding.gr42.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.red)

                else -> ticTacToeBinding.gr42.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.white)
            }
            when (filledPos[7]) {
                "Green" -> ticTacToeBinding.gr43.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.green)

                "Red" -> ticTacToeBinding.gr43.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.red)

                else -> ticTacToeBinding.gr43.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.white)
            }
            when (filledPos[8]) {
                "Green" -> ticTacToeBinding.gr44.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.green)

                "Red" -> ticTacToeBinding.gr44.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.red)

                else -> ticTacToeBinding.gr44.backgroundTintList =
                    ContextCompat.getColorStateList(ticTacToeBinding.root.context, R.color.white)
            }

            ticTacToeBinding.startGameButton.visibility = View.VISIBLE

            ticTacToeBinding.gameStatusText.text =
                when (gameStatus) {
                    GameStatus.CREATED -> {
                        ticTacToeBinding.startGameButton.visibility = View.INVISIBLE
                        ticTacToeBinding.exitButton.visibility = View.GONE
                        "Game ID: " + gameId
                    }

                    GameStatus.JOINED -> {
                        ticTacToeBinding.startGameButton.visibility = View.VISIBLE
                        ticTacToeBinding.exitButton.visibility = View.GONE
                        "Click on start game"
                    }

                    GameStatus.INPROGRESS -> {
                        ticTacToeBinding.startGameButton.visibility = View.INVISIBLE
                        ticTacToeBinding.exitButton.visibility = View.GONE
                        when (TicTacToeData.myID) {
                            currentPlayer -> "Your turn"
                            else -> currentPlayer + " turn"
                        }
                    }

                    GameStatus.FINISHED -> {
                        ticTacToeBinding.startGameButton.visibility = View.INVISIBLE
                        ticTacToeBinding.exitButton.visibility = View.VISIBLE
                        if (winner.isNotEmpty()) {
                            when (TicTacToeData.myID) {
                                winner -> "You won!"
                                else -> winner + " won!"
                            }
                        } else "DRAW"
                    }
                }
        }
    }

//    private val footballClubs = listOf(
//        "Barcelona", "Real Madrid", "Manchester United",
//        "Bayern Munich", "Juventus", "Paris Saint-Germain",
//        "Chelsea", "Liverpool", "Manchester City", "Brentford"
//    )
//
//    private val countries = listOf(
//        "Spain", "England", "Germany",
//        "Italy", "France", "Brazil",
//        "Argentina", "Portugal", "Netherlands"
//    )

    private val footballClubs = listOf(
        "Liverpool", "Barcelona", "Brentford"
    )

    private val countries = listOf(
        "Spain", "England", "Brazil"
    )


    fun startGame() {

        val selectedClubs = footballClubs.shuffled().take(3)
        val selectedCountries = countries.shuffled().take(3)

        ticTacToeModel?.apply {

            val updatedModel = TicTacToeModel(
                gameId = gameId,
                filledPos = filledPos,
                winner = winner,
                gameStatus = GameStatus.INPROGRESS,
                currentPlayer = currentPlayer,
                selectedClubs = selectedClubs,
                selectedCountries = selectedCountries
            )

            updateGameData(updatedModel)
        }

        ticTacToeBinding.gr21.setText(selectedClubs[0])
        ticTacToeBinding.gr31.setText(selectedClubs[1])
        ticTacToeBinding.gr41.setText(selectedClubs[2])

        ticTacToeBinding.gr12.setText(selectedCountries[0])
        ticTacToeBinding.gr13.setText(selectedCountries[1])
        ticTacToeBinding.gr14.setText(selectedCountries[2])

        startTurnTimer()

    }

    private fun startTurnTimer() {
        val handler = android.os.Handler()
        val runnable = object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - turnStartTime >= TURN_DURATION_MS) {
                    switchPlayer()
                    if (switchCount > MAX_SWITCHES) {
                        ticTacToeModel?.apply {
                            gameStatus = GameStatus.FINISHED
                            winner = ""
                            Toast.makeText(
                                this@TicTacToeStart,
                                "Game ended in a draw!",
                                Toast.LENGTH_SHORT
                            ).show()
                            updateGameData(this)
                        }
                        return
                    }
                    turnStartTime = System.currentTimeMillis()
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    fun updateGameData(model: TicTacToeModel) {
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
                    redirectToPrize()
                    return
                }
            }

            if (filledPos.none() { it.isEmpty() }) {
                gameStatus = GameStatus.FINISHED
                winner = ""
                redirectToLoss()
            }


            updateGameData(this)

        }
    }

    private fun redirectToPrize() {
        val intentWinner = Intent(this, ResultActivity::class.java)
        val intentLoser = Intent(this, NotResultActivity::class.java)

        ticTacToeModel?.apply {
            if (TicTacToeData.myID == winner) {
                startActivity(intentWinner)
            } else {
                startActivity(intentLoser)
            }
            finish()
        }
    }

    private fun redirectToLoss() {
        val intent = Intent(this, NotResultActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onClick(v: View?) {
        ticTacToeModel?.apply {
            if (gameStatus != GameStatus.INPROGRESS) {
                Toast.makeText(applicationContext, "Game not started", Toast.LENGTH_SHORT).show()
                return
            }


            if (gameId != "-1" && currentPlayer != TicTacToeData.myID) {
                Toast.makeText(applicationContext, "Not your turn", Toast.LENGTH_SHORT).show()
                return
            }

            val clickedPos = (v?.tag as String).toInt()


            val selectedCountry = when (clickedPos) {
                0 -> ticTacToeBinding.gr12.text.toString()
                1 -> ticTacToeBinding.gr13.text.toString()
                2 -> ticTacToeBinding.gr14.text.toString()
                3 -> ticTacToeBinding.gr12.text.toString()
                4 -> ticTacToeBinding.gr13.text.toString()
                5 -> ticTacToeBinding.gr14.text.toString()
                6 -> ticTacToeBinding.gr12.text.toString()
                7 -> ticTacToeBinding.gr13.text.toString()
                8 -> ticTacToeBinding.gr14.text.toString()
                else -> ""
            }

            val selectedClub = when (clickedPos) {
                0 -> ticTacToeBinding.gr21.text.toString()
                1 -> ticTacToeBinding.gr21.text.toString()
                2 -> ticTacToeBinding.gr21.text.toString()
                3 -> ticTacToeBinding.gr31.text.toString()
                4 -> ticTacToeBinding.gr31.text.toString()
                5 -> ticTacToeBinding.gr31.text.toString()
                6 -> ticTacToeBinding.gr41.text.toString()
                7 -> ticTacToeBinding.gr41.text.toString()
                8 -> ticTacToeBinding.gr41.text.toString()
                else -> ""
            }

            if (filledPos[clickedPos].isEmpty()) {
                showInputDialog(clickedPos, selectedCountry, selectedClub)
            }

        }
    }

    private var activeDialog: AlertDialog? = null

    private fun showInputDialog(position: Int, selectedCountry: String, selectedClub: String) {

        val input = EditText(this)

        activeDialog = AlertDialog.Builder(this)
            .setTitle("Enter player's name")
            .setMessage("The player must match both requirements!")
            .setView(input)
            .setPositiveButton("OK") { dialog, _ ->
                val userInput = input.text.toString()
                validateInput(userInput, selectedCountry, selectedClub) { isValid ->
                    if (isValid) {
                        colorSquare(position)
                    } else {
                        Toast.makeText(
                            this,
                            "Incorrect code, you lose your turn.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    switchPlayer()
                    updateGameData(ticTacToeModel!!)
                }
                activeDialog = null
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                activeDialog = null
                dialog.cancel()
            }
            .create()


        activeDialog?.show()
    }

    private fun validateInput(
        input: String,
        expectedCountry: String,
        expectedClub: String,
        onValidationComplete: (Boolean) -> Unit
    ) {

        val database =
            FirebaseDatabase.getInstance("https://footymastermindapp-default-rtdb.europe-west1.firebasedatabase.app/")
        val databaseReference = database.reference.child("players")


        databaseReference.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {

                val listType = object : GenericTypeIndicator<List<String>>() {}


                val normalizedInput = input.trim().lowercase()


                for (playerSnapshot in dataSnapshot.children) {
                    val playerName = playerSnapshot.key?.trim()?.lowercase()
                    val playerCountry =
                        playerSnapshot.child("country").getValue(String::class.java)?.trim()
                            ?.lowercase()
                    val playerClubs =
                        playerSnapshot.child("clubs").getValue(listType) ?: emptyList<String>()

                    if (playerName != null && playerCountry == expectedCountry.lowercase() && playerClubs.isNotEmpty()) {

                        val isClubCorrect =
                            playerClubs.any { it.equals(expectedClub, ignoreCase = true) }

                        val isNameCorrect =
                            playerName.contains(normalizedInput) || playerName.split(" ")
                                .any { it.startsWith(normalizedInput) }

                        if (isNameCorrect && isClubCorrect) {

                            onValidationComplete(true)
                            return@addOnSuccessListener
                        }
                    }
                }
            }
            onValidationComplete(false)
        }.addOnFailureListener {

            onValidationComplete(false)
        }
    }

    private fun colorSquare(position: Int) {
        ticTacToeModel?.apply {
            filledPos[position] = currentPlayer
            currentPlayer = if (currentPlayer == "Red") "Green" else "Red"
            checkForWinner()
            updateGameData(this)
            switchPlayer()
        }
    }

    private fun switchPlayer() {

        activeDialog?.dismiss()
        activeDialog = null

        ticTacToeModel?.apply {
            currentPlayer = if (currentPlayer == "Red") "Green" else "Red"
            switchCount++

            if (switchCount > MAX_SWITCHES) {

                gameStatus = GameStatus.FINISHED
                winner = ""
                Toast.makeText(this@TicTacToeStart, "Game ended in a draw!", Toast.LENGTH_SHORT)
                    .show()
                updateGameData(this)
            } else {
                updateGameData(this)

                turnStartTime = System.currentTimeMillis()
            }
        }
    }

}

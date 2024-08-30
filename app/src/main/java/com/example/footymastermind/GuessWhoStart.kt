package com.example.footymastermind

import android.os.Bundle
import android.util.Log
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.footymastermind.databinding.ActivityStartGuessWhoBinding
import com.google.firebase.database.*

class GuessWhoStart : AppCompatActivity(), View.OnClickListener {
    lateinit var guessWhoBinding: ActivityStartGuessWhoBinding

    private var guessWhoModel: GuessWhoModel? = null
    private var cachedSelectedPlayers: List<Player>? = null

    private val database = FirebaseDatabase.getInstance("https://footymastermindapp-default-rtdb.europe-west1.firebasedatabase.app/")
    private val databaseReference = database.reference.child("players")

    private val imageViews = arrayOfNulls<ImageView>(15)
    private val buttons = arrayOfNulls<Button>(15)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        guessWhoBinding = ActivityStartGuessWhoBinding.inflate(layoutInflater)
        setContentView(guessWhoBinding.root)

        for (i in 0 until 15) {
            val imageId = resources.getIdentifier("image_${i + 1}", "id", packageName)
            val buttonId = resources.getIdentifier("button_${i + 1}", "id", packageName)

            val imageView = findViewById<ImageView>(imageId)
            val button = findViewById<Button>(buttonId)

            // Initially hide the buttons
            button.visibility = View.INVISIBLE

            // Set OnClickListener for each ImageView to toggle the corresponding Button
            imageView.setOnClickListener {
                button.visibility = View.VISIBLE
            }

            // Set OnClickListener for each Button to hide itself when clicked
            button.setOnClickListener {
                button.visibility = View.INVISIBLE
            }
        }

        GuessWhoData.fetchGuessWhoModel()

        guessWhoBinding.startGameButton.setOnClickListener {
            startGame()
        }

        guessWhoBinding.submitButton.setOnClickListener {
            handleSubmit()
        }

        guessWhoBinding.exitButton.setOnClickListener {
            redirectToLoserScreen()
        }

        GuessWhoData.guessWhoModel.observe(this) {
            guessWhoModel = it
            setUI()
        }


        listenForGameUpdates()
    }

    private fun updatePlayers(callback: (List<Player>) -> Unit) {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val playerSnapshots = snapshot.children.toList()
                if (playerSnapshots.size >= 15) {
                    // Shuffle and select 15 random players
                    val selectedPlayers = playerSnapshots.shuffled().take(15).map { playerSnapshot ->
                        val playerName = playerSnapshot.key ?: "Unknown Name"
                        val playerImage = playerSnapshot.child("image").getValue(String::class.java) ?: ""

                        // Return a Player object with the name and image URL
                        Player(playerName, playerImage)
                    }

                    // Use the callback to return the selected players
                    callback(selectedPlayers)
                } else {
                    Log.e("GuessWhoStart", "Not enough players found in the database.")
                    callback(emptyList())  // Return an empty list if not enough players are found
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GuessWhoStart", "Database error: ${error.message}")
                callback(emptyList())  // Return an empty list in case of an error
            }
        })
    }

    fun setUI() {
        guessWhoModel?.apply {
            // Ensure the model's selectedPlayers list is updated
            cachedSelectedPlayers = guessWhoModel?.selectedPlayers
            cachedSelectedPlayers?.let { selectedPlayers ->
                // Update UI with the selected players
                updateUIWithPlayers(selectedPlayers)
            } ?: run {
                Log.e("GuessWhoStart", "Cached selected players not available.")
            }

            guessWhoBinding.startGameButton.visibility = if (gameStatus == GuessGameStatus.INPROGRESS) View.INVISIBLE else View.VISIBLE
            guessWhoBinding.exitButton.visibility = if (gameStatus == GuessGameStatus.FINISHED) View.VISIBLE else View.GONE

            guessWhoBinding.gameStatusText.text = when (gameStatus) {
                GuessGameStatus.CREATED -> {
                    guessWhoBinding.startGameButton.visibility = View.INVISIBLE
                    guessWhoBinding.exitButton.visibility = View.GONE
                    "Game ID: $gameId"
                }
                GuessGameStatus.JOINED -> {
                    guessWhoBinding.startGameButton.visibility = View.VISIBLE
                    guessWhoBinding.exitButton.visibility = View.GONE
                    "Click on start game"
                }
                GuessGameStatus.INPROGRESS -> {
                    guessWhoBinding.startGameButton.visibility = View.INVISIBLE
                    guessWhoBinding.exitButton.visibility = View.GONE
                    when (GuessWhoData.myID) {
                        currentPlayer -> "Your turn"
                        else -> "$currentPlayer's turn"
                    }
                }
                GuessGameStatus.FINISHED -> {
                    guessWhoBinding.startGameButton.visibility = View.GONE
                    guessWhoBinding.exitButton.visibility = View.VISIBLE
                    when (winner) {
                        GuessWhoData.myID -> "You won!"
                        else -> "$winner won!"
                    }
                }

                }

            guessWhoBinding.answerInput.isEnabled = GuessWhoData.myID == currentPlayer

            if (gameStatus == GuessGameStatus.INPROGRESS) {
                updateTargetPlayerUI()
            } else {
                // Clear the target player info if not in progress
                guessWhoBinding.targetName.text = ""
                guessWhoBinding.targetImage.setImageDrawable(null)
            }
        }
    }

    fun startGame() {
        updatePlayers { selectedPlayers ->
            if (selectedPlayers.isNotEmpty()) {
                guessWhoModel?.apply {
                    cachedSelectedPlayers = selectedPlayers

                    // Randomly select a target player for Red and Green
                    val targetPlayerRed = selectedPlayers.random()
                    val targetPlayerGreen = selectedPlayers.random()

                    Log.d("GuessWhoStart", "Target Player Red: $targetPlayerRed")
                    Log.d("GuessWhoStart", "Target Player Green: $targetPlayerGreen")

                    // Update the game model with the target players
                    val updatedModel = GuessWhoModel(
                        gameId = gameId,
                        winner = winner,
                        gameStatus = GuessGameStatus.INPROGRESS,
                        selectedPlayers = selectedPlayers,
                        currentPlayer = currentPlayer,
                        targetPlayerRed = targetPlayerRed,  // Assign target player for Red
                        targetPlayerGreen = targetPlayerGreen  // Assign target player for Green
                    )

                    // Update the game data in Firebase
                    updateGameData(updatedModel)

                    // Update the UI to reflect the new target players
                    updateUIWithPlayers(selectedPlayers)
                    updateTargetPlayerUI()  // New function to update target player UI
                }

                listenForGameUpdates()
            } else {
                Toast.makeText(this, "Not enough players to start the game.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateTargetPlayerUI() {
        guessWhoModel?.apply {
            val targetPlayer = when (GuessWhoData.myID) {
                "Red" -> targetPlayerRed
                "Green" -> targetPlayerGreen
                else -> null
            }

            if (targetPlayer == null) {
                Log.e("GuessWhoStart", "Target player is null.")
            } else {
                Log.d("GuessWhoStart", "Target player: $targetPlayer")
            }

            targetPlayer?.let {
                // Find and update the target player ImageView and TextView
                val targetImageView: ImageView? = guessWhoBinding.targetImage
                val targetNameView: TextView? = guessWhoBinding.targetName

                Log.d("GuessWhoStart", "Updating UI with target player: ${it.name}")

                targetNameView?.text = it.name
                targetImageView?.let { imageView ->
                    Glide.with(this@GuessWhoStart)
                        .load(it.image)
                        .into(imageView)
                }
            }
        }
    }


    // Helper function to update the UI with selected players
    private fun updateUIWithPlayers(selectedPlayers: List<Player>) {
        if (selectedPlayers.isNotEmpty()) {
            for (i in selectedPlayers.indices) {
                val player = selectedPlayers[i]
                val j = i + 1

                // Find the corresponding ImageView and TextView for each player
                val imageViewId = resources.getIdentifier("image_$j", "id", packageName)
                val nameViewId = resources.getIdentifier("name_$j", "id", packageName)

                val imageView: ImageView? = findViewById(imageViewId)
                val nameView: TextView? = findViewById(nameViewId)

                // Set the player's name and image in the UI
                nameView?.text = player.name
                imageView?.let {
                    Glide.with(this@GuessWhoStart)
                        .load(player.image)
                        .into(it)
                }
            }
        } else {
            Log.e("GuessWhoStart", "No selected players found.")
        }
    }

    private fun handleSubmit() {
        val answer = guessWhoBinding.answerInput.text.toString()
        if (answer.isNotBlank()) {
            guessWhoModel?.apply {
                // Determine the opponent's target player based on the current player
                val opponentTargetPlayer = if (GuessWhoData.myID == "Red") targetPlayerGreen else targetPlayerRed
                val isCorrect = answer.equals(opponentTargetPlayer?.name, ignoreCase = true)

                if (isCorrect) {
                    // If the answer is correct, update the model to declare the current player as the winner
                    val updatedModel = copy(
                        gameStatus = GuessGameStatus.FINISHED,
                        winner = GuessWhoData.myID  // Current player is the winner
                    )

                    // Save the updated model to Firebase
                    GuessWhoData.saveGuessWhoModel(updatedModel)

                    // Redirect to the winner's screen
                    redirectToWinnerScreen()
                } else {
                    // If the answer is incorrect, update the model with the current question and switch turn
                    val updatedModel = copy(
                        currentQuestion = answer,
                        currentPlayer = if (GuessWhoData.myID == "Red") "Green" else "Red"  // Switch turn
                    )

                    // Save the updated model to Firebase
                    GuessWhoData.saveGuessWhoModel(updatedModel)

                    // Clear the input field
                    guessWhoBinding.answerInput.text.clear()

                    // Redirect based on game status if it changes to finished
                    // (No redirection here, only switch turns)
                }
            }
        } else {
            Toast.makeText(this, "Please enter a question or answer.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun redirectToWinnerScreen() {
        // Intent to start the WinnerActivity (replace with your actual activity class)
        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
        finish()  // Optional: finish the current activity if you want to close it
    }

    private fun redirectToLoserScreen() {
        // Intent to start the LoserActivity (replace with your actual activity class)
        val intent = Intent(this, NotResultActivity::class.java)
        startActivity(intent)
        finish()  // Optional: finish the current activity if you want to close it
    }

    private fun updateGameQuestion(answer: String) {
        guessWhoModel?.apply {
            // Update the current question/answer in the model
            val updatedModel = copy(currentQuestion = answer)

            // Save the updated model to Firebase
            GuessWhoData.saveGuessWhoModel(updatedModel)
        }
    }

    fun updateGameData(model: GuessWhoModel) {
        GuessWhoData.saveGuessWhoModel(model)
    }



    override fun onClick(v: View?) {
        guessWhoModel?.apply {
            if (gameStatus != GuessGameStatus.INPROGRESS) {
                Toast.makeText(applicationContext, "Game not started", Toast.LENGTH_SHORT).show()
                return
            }

            // Game is in progress
            if (gameId != "-1" && currentPlayer != GuessWhoData.myID) {
                Toast.makeText(applicationContext, "Not your turn", Toast.LENGTH_SHORT).show()
                return
            }

        }
    }

    // Method to listen for real-time game updates
    fun listenForGameUpdates() {
        guessWhoModel?.gameId?.let { gameId ->
            val gameRef = database.reference.child("games").child(gameId)
            gameRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val updatedModel = snapshot.getValue(GuessWhoModel::class.java)
                    updatedModel?.let {
                        guessWhoModel = it
                        cachedSelectedPlayers = it.selectedPlayers
                        setUI()  // Update UI based on the new game state
                        updateTargetPlayerUI()

                        if (it.gameStatus == GuessGameStatus.FINISHED) {
                            if (it.winner == GuessWhoData.myID) {
                                redirectToWinnerScreen()
                            } else {
                                redirectToLoserScreen()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("GuessWhoStart", "Database error: ${error.message}")
                }
            })
        }
    }
}
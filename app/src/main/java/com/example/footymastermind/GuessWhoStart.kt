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

    private val database =
        FirebaseDatabase.getInstance("https://footymastermindapp-default-rtdb.europe-west1.firebasedatabase.app/")
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
                    val selectedPlayers =
                        playerSnapshots.shuffled().take(15).map { playerSnapshot ->
                            val playerName = playerSnapshot.key ?: "Unknown Name"
                            val playerImage =
                                playerSnapshot.child("image").getValue(String::class.java) ?: ""

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

            guessWhoBinding.startGameButton.visibility =
                if (gameStatus == GuessGameStatus.INPROGRESS) View.INVISIBLE else View.VISIBLE
            guessWhoBinding.exitButton.visibility =
                if (gameStatus == GuessGameStatus.FINISHED) View.VISIBLE else View.GONE

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
                        currentPlayer -> "Your turn: $currentQuestion"
                        else -> "$currentPlayer's turn: $currentQuestion"
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


                    val targetPlayerRed = selectedPlayers.random()
                    val targetPlayerGreen = selectedPlayers.random()

                    Log.d("GuessWhoStart", "Target Player Red: $targetPlayerRed")
                    Log.d("GuessWhoStart", "Target Player Green: $targetPlayerGreen")


                    val updatedModel = GuessWhoModel(
                        gameId = gameId,
                        winner = winner,
                        gameStatus = GuessGameStatus.INPROGRESS,
                        selectedPlayers = selectedPlayers,
                        currentPlayer = currentPlayer,
                        targetPlayerRed = targetPlayerRed,
                        targetPlayerGreen = targetPlayerGreen
                    )


                    updateGameData(updatedModel)

                    updateUIWithPlayers(selectedPlayers)
                    updateTargetPlayerUI()
                }

                listenForGameUpdates()
            } else {
                Toast.makeText(this, "Not enough players to start the game.", Toast.LENGTH_SHORT)
                    .show()
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


    private fun updateUIWithPlayers(selectedPlayers: List<Player>) {
        if (selectedPlayers.isNotEmpty()) {
            for (i in selectedPlayers.indices) {
                val player = selectedPlayers[i]
                val j = i + 1

                val imageViewId = resources.getIdentifier("image_$j", "id", packageName)
                val nameViewId = resources.getIdentifier("name_$j", "id", packageName)

                val imageView: ImageView? = findViewById(imageViewId)
                val nameView: TextView? = findViewById(nameViewId)

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
                val opponentTargetPlayer =
                    if (GuessWhoData.myID == "Red") targetPlayerGreen else targetPlayerRed

                val isCorrect = opponentTargetPlayer?.name?.let { targetName ->
                    val cleanedAnswer = answer.trim().trimEnd('.', '!', '?')
                    cleanedAnswer.endsWith(targetName, ignoreCase = true)
                } ?: false

                if (isCorrect) {
                    val updatedModel = copy(
                        gameStatus = GuessGameStatus.FINISHED,
                        winner = GuessWhoData.myID
                    )

                    GuessWhoData.saveGuessWhoModel(updatedModel)

                    redirectToWinnerScreen()
                } else {
                    val updatedModel = copy(
                        currentQuestion = answer,
                        currentPlayer = if (GuessWhoData.myID == "Red") "Green" else "Red"
                    )

                    GuessWhoData.saveGuessWhoModel(updatedModel)

                    guessWhoBinding.answerInput.text.clear()

                }
            }
        } else {
            Toast.makeText(this, "Please enter a question or answer.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun redirectToWinnerScreen() {
        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun redirectToLoserScreen() {
        val intent = Intent(this, NotResultActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateGameQuestion(answer: String) {
        guessWhoModel?.apply {
            val updatedModel = copy(currentQuestion = answer)

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

            if (gameId != "-1" && currentPlayer != GuessWhoData.myID) {
                Toast.makeText(applicationContext, "Not your turn", Toast.LENGTH_SHORT).show()
                return
            }

        }
    }

    fun listenForGameUpdates() {
        guessWhoModel?.gameId?.let { gameId ->
            val gameRef = database.reference.child("games").child(gameId)
            gameRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val updatedModel = snapshot.getValue(GuessWhoModel::class.java)
                    updatedModel?.let {
                        guessWhoModel = it
                        cachedSelectedPlayers = it.selectedPlayers
                        setUI()
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
package com.example.footymastermind

import android.os.Bundle
import android.util.Log
import android.view.View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        guessWhoBinding = ActivityStartGuessWhoBinding.inflate(layoutInflater)
        setContentView(guessWhoBinding.root)

        GuessWhoData.fetchGuessWhoModel()

        guessWhoBinding.startGameButton.setOnClickListener {
            startGame()
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

            guessWhoBinding.startGameButton.visibility = View.VISIBLE

            guessWhoBinding.gameStatusText.text =
                when (gameStatus) {
                    GuessGameStatus.CREATED -> {
                        guessWhoBinding.startGameButton.visibility = View.INVISIBLE
                        "Game ID: $gameId"
                    }
                    GuessGameStatus.JOINED -> {
                        "Click on start game"
                    }
                    GuessGameStatus.INPROGRESS -> {
                        guessWhoBinding.startGameButton.visibility = View.INVISIBLE
                        when (GuessWhoData.myID) {
                            currentPlayer -> "Your turn"
                            else -> "$currentPlayer's turn"
                        }
                    }
                    GuessGameStatus.FINISHED -> {
                        if (winner.isNotEmpty()) {
                            when (GuessWhoData.myID) {
                                winner -> "You won!"
                                else -> "$winner won!"
                            }
                        } else "DRAW"
                    }
                }
        }
    }

    fun startGame() {
        // Fetch and update the selected players before starting the game
        updatePlayers { selectedPlayers ->
            guessWhoModel?.apply {
                cachedSelectedPlayers = selectedPlayers
                // Update the game status to INPROGRESS with the updated selected players
                val updatedModel = GuessWhoModel(
                    gameId = gameId,
                    winner = winner,
                    gameStatus = GuessGameStatus.INPROGRESS,
                    selectedPlayers = selectedPlayers,  // Updated selectedPlayers
                    currentPlayer = currentPlayer
                )

                // Update the game data in Firebase
                updateGameData(updatedModel)

                // Directly update the UI with the updated selected players
                updateUIWithPlayers(selectedPlayers)
            }

            listenForGameUpdates()

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
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("GuessWhoStart", "Database error: ${error.message}")
                }
            })
        }
    }
}
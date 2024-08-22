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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GuessWhoStart : AppCompatActivity(), View.OnClickListener {
    lateinit var guessWhoBinding: ActivityStartGuessWhoBinding

    private var guessWhoModel: GuessWhoModel? = null

    private val database = FirebaseDatabase.getInstance("https://footymastermindapp-default-rtdb.europe-west1.firebasedatabase.app/")
    private val databaseReference = database.reference.child("players")
    private val gameReference = database.reference.child("games")

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
    }

    private fun updatePlayers() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val playerSnapshots = snapshot.children.toList()
                if (playerSnapshots.size >= 15) {
                    // Shuffle and select 15 random players
                    val selectedPlayers = playerSnapshots.shuffled().take(15)

                    // Update the UI with the selected players
                    for (i in selectedPlayers.indices) {
                        val playerSnapshot = selectedPlayers[i]
                        val playerName = playerSnapshot.key ?: "Unknown Name"
                        val playerImage = playerSnapshot.child("image").getValue(String::class.java) ?: ""

                        val j = i + 1

                        // Update ImageView and TextView for each player
                        val imageViewId = resources.getIdentifier("image_$j", "id", packageName)
                        val nameViewId = resources.getIdentifier("name_$j", "id", packageName)

                        val imageView: ImageView? = findViewById(imageViewId)
                        val nameView: TextView? = findViewById(nameViewId)

                        nameView?.text = playerName
                        if (playerImage.isNotEmpty()) {
                            imageView?.let {
                                Glide.with(this@GuessWhoStart)
                                    .load(playerImage)
                                    .into(it)
                            }
                        }
                    }
                } else {
                    Log.e("GuessWhoStart", "Not enough players found in the database.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GuessWhoStart", "Database error: ${error.message}")
            }
        })
    }

    private fun setUI() {
        guessWhoModel?.apply {
            guessWhoBinding.startGameButton.visibility = View.VISIBLE

            guessWhoBinding.gameStatusText.text =
                when (gameStatus) {
                    GameStatus.CREATED -> {
                        guessWhoBinding.startGameButton.visibility = View.INVISIBLE
                        "Game ID: " + gameId
                    }
                    GameStatus.JOINED -> {
                        "Click on start game"
                    }
                    GameStatus.INPROGRESS -> {
                        guessWhoBinding.startGameButton.visibility = View.INVISIBLE
                        when (GuessWhoData.myID) {
                            currentPlayer -> "Your turn"
                            else -> currentPlayer + " turn"
                        }
                    }
                    GameStatus.FINISHED -> {
                        if (winner.isNotEmpty()) {
                            when (GuessWhoData.myID) {
                                winner -> "You won!"
                                else -> winner + " won!"
                            }
                        } else "DRAW"
                    }
                }
        }
    }

    private fun startGame() {
        guessWhoModel?.apply {
            val updatedModel = TicTacToeModel(
                gameId = gameId,
                filledPos = filledPos,
                winner = winner,
                gameStatus = GameStatus.INPROGRESS,
                currentPlayer = currentPlayer
            )

            // Update game data in the Firebase database
            gameReference.child(gameId).setValue(updatedModel).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("GuessWhoStart", "Game started successfully.")
                    updatePlayers() // Ensure both players see the same set of players
                } else {
                    Log.e("GuessWhoStart", "Failed to start game: ${task.exception?.message}")
                }
            }
        }
    }

    override fun onClick(v: View?) {
        guessWhoModel?.apply {
            if (gameStatus != GameStatus.INPROGRESS) {
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
}

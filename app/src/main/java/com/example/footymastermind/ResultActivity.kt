package com.example.footymastermind

import com.bumptech.glide.Glide
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.footymastermind.databinding.ActivityResultBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.random.Random

class ResultActivity : AppCompatActivity() {

    lateinit var resultBinding: ActivityResultBinding

    private val database = FirebaseDatabase.getInstance("https://footymastermindapp-default-rtdb.europe-west1.firebasedatabase.app/")
    private val databaseReference = database.reference.child("players")

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    private var userCorrect = ""
    private var userWrong = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultBinding = ActivityResultBinding.inflate(layoutInflater)
        val view = resultBinding.root
        setContentView(view)

        fetchPlayerData()

        resultBinding.buttonExit.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fragment_to_load", R.id.nav_home)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchPlayerData() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Collect all player keys
                val playerKeys = snapshot.children.map { it.key }.filterNotNull()

                // Select a random player
                if (playerKeys.isNotEmpty()) {
                    val randomPlayerKey = playerKeys[Random.nextInt(playerKeys.size)]
                    val playerSnapshot = snapshot.child(randomPlayerKey)

                    // Update UI with player's data
                    updateUIWithPlayerData(playerSnapshot)
                } else {
                    Log.e("ResultActivity", "No players found in the database.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ResultActivity", "Database error: ${error.message}")
            }
        })
    }

    private fun updateUIWithPlayerData(playerSnapshot: DataSnapshot) {
        // Extract player data
        val playerName = playerSnapshot.key ?: "Unknown Player"
        val playerCountry = playerSnapshot.child("country").getValue(String::class.java) ?: "Unknown Country"
        val playerCurrentClub = playerSnapshot.child("current club").getValue(String::class.java) ?: "Unknown Club"
        val playerImage = playerSnapshot.child("image").getValue(String::class.java) ?: ""
        val playerOverall = playerSnapshot.child("overall").getValue(Int::class.java) ?: 0
        val playerPosition = playerSnapshot.child("position").getValue(String::class.java) ?: "Unknown Position"

        // Update UI elements
        resultBinding.editTextName.text = playerName
        resultBinding.editTextClub.text = playerCurrentClub
        resultBinding.editTextOverall.text = playerOverall.toString()
        resultBinding.editTextNationality.text = playerCountry
        resultBinding.editTextPosition.text = playerPosition

        // Load image into ImageView (you might need to use an image loading library like Glide or Picasso)
        if (playerImage.isNotEmpty()) {
            // For example, using Glide:
            Glide.with(this)
                .load(playerImage)
                .into(resultBinding.imageView)
        }
    }
}

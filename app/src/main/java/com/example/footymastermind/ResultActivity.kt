package com.example.footymastermind

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.footymastermind.databinding.ActivityResultBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlin.random.Random

class ResultActivity : AppCompatActivity() {

    lateinit var resultBinding: ActivityResultBinding

    private val database = FirebaseDatabase.getInstance("https://footymastermindapp-default-rtdb.europe-west1.firebasedatabase.app/")
    private val databaseReference = database.reference.child("players")

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultBinding = ActivityResultBinding.inflate(layoutInflater)
        val view = resultBinding.root
        setContentView(view)

        fetchPlayerData()
        ensureUserExists()

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
                val playerKeys = snapshot.children.map { it.key }.filterNotNull()

                if (playerKeys.isNotEmpty()) {
                    val randomPlayerKey = playerKeys[Random.nextInt(playerKeys.size)]
                    val playerSnapshot = snapshot.child(randomPlayerKey)
                    updateUIWithPlayerData(playerSnapshot)
                    updateUserOwnedPlayers(randomPlayerKey) // Add the won player to the user's owned players
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
        val playerName = playerSnapshot.key ?: "Unknown Player"
        val playerCountry = playerSnapshot.child("country").getValue(String::class.java) ?: "Unknown Country"
        val playerCurrentClub = playerSnapshot.child("current club").getValue(String::class.java) ?: "Unknown Club"
        val playerImage = playerSnapshot.child("image").getValue(String::class.java) ?: ""
        val playerOverall = playerSnapshot.child("overall").getValue(Int::class.java) ?: 0
        val playerPosition = playerSnapshot.child("position").getValue(String::class.java) ?: "Unknown Position"

        resultBinding.editTextName.text = playerName
        resultBinding.editTextClub.text = playerCurrentClub
        resultBinding.editTextOverall.text = playerOverall.toString()
        resultBinding.editTextNationality.text = playerCountry
        resultBinding.editTextPosition.text = playerPosition

        if (playerImage.isNotEmpty()) {
            Glide.with(this)
                .load(playerImage)
                .into(resultBinding.imageView)
        }
    }

    private fun ensureUserExists() {
        user?.let {
            val userUID = it.uid
            val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userUID)

            userReference.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (!task.result.exists()) {
                        userReference.setValue(mapOf(
                            "email" to it.email,
                            "owned_players" to emptyList<String>()
                        )).addOnSuccessListener {
                            Log.d("ResultActivity", "User node created successfully.")
                        }.addOnFailureListener { e ->
                            Log.e("ResultActivity", "Failed to create user node: ${e.message}")
                        }
                    }
                } else {
                    Log.e("ResultActivity", "Failed to check user existence: ${task.exception?.message}")
                }
            }
        }
    }

    private fun updateUserOwnedPlayers(playerKey: String) {
        user?.let {
            val userUID = it.uid
            val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userUID)

            userReference.child("owned_players").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(userSnapshot: DataSnapshot) {
                    val ownedPlayers = userSnapshot.getValue<List<String>>()?.toMutableList() ?: mutableListOf()

                    if (!ownedPlayers.contains(playerKey)) {
                        ownedPlayers.add(playerKey)

                        userReference.child("owned_players").setValue(ownedPlayers)
                            .addOnSuccessListener {
                                Log.d("ResultActivity", "User owned_players updated successfully.")
                            }
                            .addOnFailureListener { e ->
                                Log.e("ResultActivity", "Failed to update user owned_players: ${e.message}")
                            }
                    } else {
                        Log.d("ResultActivity", "Player $playerKey is already owned.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ResultActivity", "Failed to read user data: ${error.message}")
                }
            })
        }
    }
}

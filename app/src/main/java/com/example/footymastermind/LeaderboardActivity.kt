package com.example.footymastermind

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.footymastermind.databinding.ActivityLeaderboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var leaderboardBinding: ActivityLeaderboardBinding
    private val database = FirebaseDatabase.getInstance("https://footymastermindapp-default-rtdb.europe-west1.firebasedatabase.app/")
    private val databaseReference = database.reference.child("scores")
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        leaderboardBinding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(leaderboardBinding.root)

        fetchAndDisplayLeaderboard()

        val exitButton: Button = findViewById(R.id.exit_button)
        exitButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)

            // Start MainActivity
            startActivity(intent)

            // Optionally, finish the current activity
            finish()
        }
    }

    private fun fetchAndDisplayLeaderboard() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                leaderboardBinding.leaderboardTable.removeAllViews()
                val scrollView: ScrollView = findViewById(R.id.scroll_view)

                // Header Row
                val headerRow = TableRow(this@LeaderboardActivity)
                val userIdHeader = TextView(this@LeaderboardActivity)
                userIdHeader.text = "User ID"
                userIdHeader.setPadding(8, 8, 8, 8)
                headerRow.addView(userIdHeader)

                val scoreHeader = TextView(this@LeaderboardActivity)
                scoreHeader.text = "Score"
                scoreHeader.setPadding(8, 8, 8, 8)
                headerRow.addView(scoreHeader)
                leaderboardBinding.leaderboardTable.addView(headerRow)

                var currentUserRow: TableRow? = null

                // Populate rows with data
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    val score = userSnapshot.child("correct").getValue(Int::class.java) ?: 0

                    val row = TableRow(this@LeaderboardActivity)
                    val userIdTextView = TextView(this@LeaderboardActivity)
                    userIdTextView.text = userId
                    userIdTextView.setPadding(8, 8, 8, 8)
                    row.addView(userIdTextView)

                    val scoreTextView = TextView(this@LeaderboardActivity)
                    scoreTextView.text = score.toString()
                    scoreTextView.setPadding(8, 8, 8, 8)
                    row.addView(scoreTextView)

                    if (userId == user?.uid) {
                        row.setBackgroundColor(Color.GREEN)
                        currentUserRow = row
                    }

                    leaderboardBinding.leaderboardTable.addView(row)
                }

                // Scroll to the current user's row if it exists
                currentUserRow?.let {
                    scrollView.post {
                        scrollView.scrollTo(0, it.top - scrollView.height / 2 + it.height / 2)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })
    }
}

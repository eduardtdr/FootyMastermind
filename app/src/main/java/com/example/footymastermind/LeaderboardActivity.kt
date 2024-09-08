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
    private val database =
        FirebaseDatabase.getInstance("https://footymastermindapp-default-rtdb.europe-west1.firebasedatabase.app/")
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

            startActivity(intent)

            finish()
        }
    }

    private fun fetchAndDisplayLeaderboard() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                leaderboardBinding.leaderboardTable.removeAllViews()
                val scrollView: ScrollView = findViewById(R.id.scroll_view)

                val headerRow = TableRow(this@LeaderboardActivity)

                val userIdHeader = TextView(this@LeaderboardActivity)
                userIdHeader.text = "User ID"
                userIdHeader.setPadding(16, 16, 16, 16)
                headerRow.addView(
                    userIdHeader,
                    TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                )

                val scoreHeader = TextView(this@LeaderboardActivity)
                scoreHeader.text = "Score"
                scoreHeader.setPadding(16, 16, 16, 16)
                headerRow.addView(
                    scoreHeader,
                    TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                )

                leaderboardBinding.leaderboardTable.addView(headerRow)

                var currentUserRow: TableRow? = null

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.child("name").getValue(String::class.java) ?: continue
                    val score = userSnapshot.child("correct").getValue(Int::class.java) ?: 0

                    val playerName = userId.substringBefore("@")

                    val row = TableRow(this@LeaderboardActivity)

                    val userIdTextView = TextView(this@LeaderboardActivity)
                    userIdTextView.text = playerName
                    userIdTextView.setPadding(16, 16, 16, 16) // Increased padding
                    row.addView(
                        userIdTextView,
                        TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                    )

                    val scoreTextView = TextView(this@LeaderboardActivity)
                    scoreTextView.text = score.toString()
                    scoreTextView.setPadding(16, 16, 16, 16) // Increased padding
                    row.addView(
                        scoreTextView,
                        TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                    )

                    if (userId == (user?.email?.substringBefore("@") ?: "")) {
                        row.setBackgroundColor(Color.GREEN)
                        currentUserRow = row
                    }

                    leaderboardBinding.leaderboardTable.addView(row)
                }

                currentUserRow?.let {
                    scrollView.post {
                        scrollView.scrollTo(0, it.top - scrollView.height / 2 + it.height / 2)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}

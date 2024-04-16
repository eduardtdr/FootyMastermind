// exit sa duca la home page

package com.example.footymastermind

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.footymastermind.databinding.ActivityResultBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ResultActivity : AppCompatActivity() {

    lateinit var  resultBinding : ActivityResultBinding

    val database = FirebaseDatabase.getInstance("https://footymastermindapp-default-rtdb.europe-west1.firebasedatabase.app/")

    val databaseReference = database.reference.child("scores")

    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    var userCorrect = ""
    var userWrong = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultBinding = ActivityResultBinding.inflate(layoutInflater)
        val view = resultBinding.root
        setContentView(view)

        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                user?.let {
                    val userUID = it.uid
                    userCorrect = snapshot.child(userUID).child("correct").value.toString()
                    userWrong = snapshot.child(userUID).child("wrong").value.toString()

                    resultBinding.textViewScoreCorrect.text = userCorrect
                    resultBinding.textViewScoreWrong.text = userWrong
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        resultBinding.buttonPlayAgain.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fragment_to_load", R.id.nav_trivia)
            startActivity(intent)
            finish()
        }

        resultBinding.buttonExit.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fragment_to_load", R.id.nav_home)
            startActivity(intent)
            finish()
        }



    }


}
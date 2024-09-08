package com.example.footymastermind

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.footymastermind.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    lateinit var signupBinding: ActivitySignupBinding

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signupBinding = ActivitySignupBinding.inflate(layoutInflater)
        val view = signupBinding.root
        setContentView(view)

        signupBinding.buttonSignup.setOnClickListener {

            val email = signupBinding.editTextSignupEmail.text.toString()
            val password = signupBinding.editTextSignupPassword.text.toString()

            if (isPasswordSecure(password)) {
                signupWithFirebase(email, password)
            } else {
                Toast.makeText(
                    applicationContext,
                    "Password must be at least 8 characters long and include upper and lower case letters, numbers, and special characters.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun isPasswordSecure(password: String): Boolean {
        val passwordPattern =
            Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\S+$).{8,}$")
        return passwordPattern.matches(password)
    }

    fun signupWithFirebase(email: String, password: String) {
        signupBinding.progressBarSignup.visibility = View.VISIBLE
        signupBinding.buttonSignup.isClickable = false

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->

            if (task.isSuccessful) {

                Toast.makeText(
                    applicationContext,
                    "Your account has been created",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                signupBinding.progressBarSignup.visibility = View.INVISIBLE
                signupBinding.buttonSignup.isClickable = true

            } else {

                Toast.makeText(
                    applicationContext,
                    task.exception?.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
                signupBinding.progressBarSignup.visibility = View.INVISIBLE
                signupBinding.buttonSignup.isClickable = true
            }
        }
    }
}

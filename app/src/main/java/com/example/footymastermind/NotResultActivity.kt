package com.example.footymastermind

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.footymastermind.databinding.ActivityNotResultBinding

class NotResultActivity : AppCompatActivity() {
    lateinit var notResultBinding: ActivityNotResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the correct binding for activity_not_result.xml
        notResultBinding = ActivityNotResultBinding.inflate(layoutInflater)
        val view = notResultBinding.root
        setContentView(view)

        // Handle Exit button click
        notResultBinding.buttonExit.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fragment_to_load", R.id.nav_home)
            startActivity(intent)
            finish()
        }
    }
}

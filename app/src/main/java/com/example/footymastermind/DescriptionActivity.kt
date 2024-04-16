package com.example.footymastermind

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class DescriptionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        val name = intent.getStringExtra("NAME")
        val category = intent.getStringExtra("CATEGORY")
        val description = intent.getStringExtra("DESCRIPTION")
        val image = intent.getIntExtra("IMAGE", 0)

        val nameTextView = findViewById<TextView>(R.id.gameTitle)
        val categoryTextView = findViewById<TextView>(R.id.gameCategory)
        val descriptionTextView = findViewById<TextView>(R.id.gameDescription)
        val game_image = findViewById<ImageView>(R.id.gameImage)

        nameTextView.setText(name)
        categoryTextView.setText(category)
        descriptionTextView.setText(description)
        game_image.setImageResource(image)

    }
}
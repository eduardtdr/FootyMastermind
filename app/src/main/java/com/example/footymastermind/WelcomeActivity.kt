package com.example.footymastermind

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputBinding
import com.example.footymastermind.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    lateinit var splashBinding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashBinding = ActivityWelcomeBinding.inflate(layoutInflater)
        val view = splashBinding.root
        setContentView(view)

        val alphaAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.splash_anim)
        splashBinding.imageViewTop.startAnimation(alphaAnimation)

        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed(object : Runnable{
            override fun run() {
                val intent = Intent(this@WelcomeActivity,LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 5000)
    }
}
package com.example.footymastermind

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.footymastermind.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    lateinit var mainBinding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        user?.reload()?.addOnCompleteListener { reloadTask ->
            if (reloadTask.isSuccessful) {
                val displayName = user.displayName
                if (displayName != null) {
                    val headerView = navigationView.getHeaderView(0)
                    val usernameTextView = headerView.findViewById<TextView>(R.id.usernameText)
                    usernameTextView.text = displayName
                } else {
                    val email = user.email
                    val username = email?.substringBefore('@')
                    val headerView = navigationView.getHeaderView(0)
                    val usernameTextView = headerView.findViewById<TextView>(R.id.usernameText)
                    usernameTextView.text = username
                }
            } else {
                Log.e(TAG, "Failed to reload user data", reloadTask.exception)
            }
        }



        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_trivia)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_home -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            R.id.nav_tenaball -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TenaballFragment()).commit()
            R.id.nav_dream_team -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DreamTeamFragment()).commit()
            R.id.nav_tic_tac_toe -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TicTacToeFragment()).commit()
            R.id.nav_guess -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, GuessWhoFragment()).commit()
            R.id.nav_trivia -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TriviaFragment()).commit()
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
package com.example.chiikawa_n_friends_diary_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.core.content.edit

class MainMenu : AppCompatActivity() {

    lateinit var btnProfile: Button
    lateinit var btnAbout: Button
    lateinit var btnNotes: Button
    lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (Firebase.auth.currentUser == null) {
            val intent = Intent(this, Startup::class.java) // Or your SignupLogin activity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return // Stop further execution of onCreate
        }

        btnProfile = findViewById<Button>(R.id.btnProfile)
        btnProfile.setOnClickListener{
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }


        btnNotes = findViewById<Button>(R.id.btnNotes)
        btnNotes.setOnClickListener{
            val intent = Intent(this, Notes::class.java)
            startActivity(intent)
        }

        btnAbout = findViewById<Button>(R.id.btnAbout)
        btnAbout.setOnClickListener{
            val intent = Intent(this, About::class.java)
            startActivity(intent)
        }

        btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            Firebase.auth.signOut()

            //clear current user
            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
            sharedPref.edit {
                remove("CURRENT_USER") // Remove the specific key
                // Alternatively: putString("CURRENT_USER", "guest") if you truly need a default "guest" state
                apply() // Apply asynchronously
            }

            val intent = Intent(this, Startup::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }
}
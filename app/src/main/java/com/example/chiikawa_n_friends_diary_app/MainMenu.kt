package com.example.chiikawa_n_friends_diary_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainMenu : AppCompatActivity() {

    lateinit var btnProfile: Button
    lateinit var btnAbout: Button
    lateinit var btnNotes: Button
    lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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

        btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener{
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

    }
}
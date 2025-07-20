package com.example.chiikawa_n_friends_diary_app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainMenu : AppCompatActivity() {

    private lateinit var btnProfile: Button
    private lateinit var btnAbout: Button
    private lateinit var btnNotes: Button
    private lateinit var btnLogout: Button
    private lateinit var tvName: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvTime: TextView

    private val dateFormatter = SimpleDateFormat("MM-dd-yy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Initialize views
        tvDate = findViewById(R.id.tvDate)
        tvTime = findViewById(R.id.tvTime)
        tvName = findViewById(R.id.tvName)
        btnProfile = findViewById(R.id.btnProfile)
        btnAbout = findViewById(R.id.btnAbout)
        btnNotes = findViewById(R.id.btnNotes)
        btnLogout = findViewById(R.id.btnLogout)

        // Display user name from SharedPreferences
        val sp = getSharedPreferences("UserSession", MODE_PRIVATE)
        val fname = sp.getString("FIRST_NAME", "") ?: ""
        val lname = sp.getString("LAST_NAME", "") ?: ""
        tvName.text = "Hello, $fname $lname!"

        // Set up date/time update task
        updateRunnable = object : Runnable {
            override fun run() {
                updateDateTime()
                val nowSec = Date().seconds
                val delay = ((60 - nowSec) * 1000L)
                handler.postDelayed(this, delay)
            }
        }

        // Auth check
        if (Firebase.auth.currentUser == null) {
            val i = Intent(this, Startup::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(i); finish()
            return
        }

        // Navigation buttons
        btnProfile.setOnClickListener { startActivity(Intent(this, Profile::class.java)) }
        btnNotes.setOnClickListener { startActivity(Intent(this, Notes::class.java)) }
        btnAbout.setOnClickListener { startActivity(Intent(this, About::class.java)) }
        btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            getSharedPreferences("UserSession", MODE_PRIVATE).edit().remove("CURRENT_USER").apply()
            Intent(this, Startup::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(this); finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateRunnable.run() // Start updating immediately
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    private fun updateDateTime() {
        val now = Date()
        tvDate.text = dateFormatter.format(now)
        tvTime.text = timeFormatter.format(now)
    }
}

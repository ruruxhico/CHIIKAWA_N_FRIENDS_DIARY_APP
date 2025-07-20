package com.example.chiikawa_n_friends_diary_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Write : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnSave: Button
    private lateinit var etmWrite: EditText
    private lateinit var tvWrite: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_write)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        etmWrite = findViewById(R.id.etmWrite)
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)
        tvWrite = findViewById(R.id.tvEntry) // Update this ID if your layout uses a different one

        // Load stored text if any
        val year = intent.getIntExtra("EXTRA_YEAR", 0)
        val month = intent.getIntExtra("EXTRA_MONTH", 0)
        val day = intent.getIntExtra("EXTRA_DAY", 0)
        val userID = getSharedPreferences("UserSession", MODE_PRIVATE)
            .getString("CURRENT_USER", "guest")
        val key = "${userID}_${year}-${month + 1}-$day"
        val sharedPref = getSharedPreferences("DiaryEntries", MODE_PRIVATE)
        val savedEntry = sharedPref.getString(key, "") ?: ""
        tvWrite.text = savedEntry

        btnBack.setOnClickListener {
            // simply return to previous activity
            finish()
        }

        btnSave.setOnClickListener {
            val newText = etmWrite.text.toString().trim()
            if (newText.isNotBlank()) {
                val existing = sharedPref.getString(key, "") ?: ""
                val updated = if (existing.isEmpty()) {
                    newText
                } else {
                    "$existing\n$newText"
                }

                sharedPref.edit {
                    putString(key, updated)
                    apply()
                }

                // Update UI without finishing
                tvWrite.text = updated
                etmWrite.text.clear()
                Toast.makeText(this, "Entry appended!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please write something!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

package com.example.chiikawa_n_friends_diary_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.edit

class Write : AppCompatActivity() {

    lateinit var btnBack: Button
    lateinit var btnSave: Button
    lateinit var etmWrite: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_write)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //init
        etmWrite = findViewById(R.id.etmWrite)

        //get date
        val year = intent.getIntExtra("EXTRA_YEAR", 0)
        val month = intent.getIntExtra("EXTRA_MONTH", 0)
        val day = intent.getIntExtra("EXTRA_DAY", 0)

        //current user
        val userID = getSharedPreferences("UserSession", MODE_PRIVATE)
            .getString("CURRENT_USER", "guest")

        //make key for a diary entry
        val key = "${userID}_${year}-${month + 1}-$day"

        val sharedPref = getSharedPreferences("DiaryEntries", MODE_PRIVATE)
        val savedEntry = sharedPref.getString(key, "")
        etmWrite.setText(savedEntry)

        //back button
        btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener{
            val intent = Intent(this, Notes::class.java)
            startActivity(intent)
            finish()
        }

        //save entry
        btnSave = findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener {
            val entry = etmWrite.text.toString()

            if (entry.isNotBlank()) {
                sharedPref.edit {
                    putString(key, entry)
                    apply()
                }

                Toast.makeText(this, "Entry saved!", Toast.LENGTH_SHORT).show()

                // Go back to Notes to update UI
                val intent = Intent(this, Notes::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please write something!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
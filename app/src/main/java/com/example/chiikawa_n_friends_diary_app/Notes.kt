package com.example.chiikawa_n_friends_diary_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Notes : AppCompatActivity() {

    lateinit var btnBack: Button
    lateinit var btnFirst: Button
    lateinit var btnSecond: Button
    lateinit var btnThird: Button
    lateinit var btnFourth: Button
    lateinit var btnFifth: Button
    lateinit var btnSixth: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener{
            val intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
        }

        btnFirst = findViewById<Button>(R.id.btnFirst)
        btnFirst.setOnClickListener{
            val intent = Intent(this, Write::class.java)
            startActivity(intent)
        }

        btnSecond = findViewById<Button>(R.id.btnSecond)
        btnSecond.setOnClickListener{
            val intent = Intent(this, Write::class.java)
            startActivity(intent)
        }

        btnThird = findViewById<Button>(R.id.btnThird)
        btnThird.setOnClickListener{
            val intent = Intent(this, Write::class.java)
            startActivity(intent)
        }

        btnFourth = findViewById<Button>(R.id.btnFourth)
        btnFourth.setOnClickListener{
            val intent = Intent(this, Write::class.java)
            startActivity(intent)
        }

        btnFifth = findViewById<Button>(R.id.btnFifth)
        btnFifth.setOnClickListener{
            val intent = Intent(this, Write::class.java)
            startActivity(intent)
        }

        btnSixth = findViewById<Button>(R.id.btnSixth)
        btnSixth.setOnClickListener{
            val intent = Intent(this, Write::class.java)
            startActivity(intent)
        }



    }
}
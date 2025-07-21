package com.example.chiikawa_n_friends_diary_app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.CalendarView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Notes : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var cvCalendar: CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notes)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ivBack = findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            startActivity(Intent(this, MainMenu::class.java))
        }

        cvCalendar = findViewById(R.id.cvCalendar)
        cvCalendar.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val intent = Intent(this, Write::class.java).apply {
                putExtra("EXTRA_YEAR", year)
                putExtra("EXTRA_MONTH", month)
                putExtra("EXTRA_DAY", dayOfMonth)
            }
            startActivity(intent)
        }
    }
}
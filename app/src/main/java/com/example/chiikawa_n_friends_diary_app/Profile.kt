package com.example.chiikawa_n_friends_diary_app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Profile : AppCompatActivity() {


    private lateinit var ivBack: ImageView

    private lateinit var tvYourFname: TextView
    private lateinit var tvYourMName: TextView
    private lateinit var tvYourLName: TextView
    private lateinit var tvYourEmail: TextView
    private lateinit var tvYourBDay: TextView
    private lateinit var tvYourBMonth: TextView
    private lateinit var tvYourBYear: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        tvYourFname = findViewById(R.id.tvYourFname)
        tvYourMName = findViewById(R.id.tvYourMName)
        tvYourLName = findViewById(R.id.tvYourLName)
        tvYourEmail = findViewById(R.id.tvYourEmail)
        tvYourBDay = findViewById(R.id.tvYourBDay)
        tvYourBMonth = findViewById(R.id.tvYourBMonth)
        tvYourBYear = findViewById(R.id.tvYourBYear)
        ivBack = findViewById(R.id.ivBack)

        // Retrieve from SharedPreferences
        val sp: SharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        tvYourFname.text  = sp.getString("FIRST_NAME", "")
        tvYourMName.text  = sp.getString("MIDDLE_NAME", "")
        tvYourLName.text  = sp.getString("LAST_NAME", "")
        tvYourEmail.text  = sp.getString("EMAIL", "")
        tvYourBDay.text   = sp.getString("BIRTH_DAY", "")
        tvYourBMonth.text = sp.getString("BIRTH_MONTH", "")
        tvYourBYear.text  = sp.getString("BIRTH_YEAR", "")

        ivBack.setOnClickListener {
            startActivity(Intent(this, MainMenu::class.java))
            finish()
        }
    }
}

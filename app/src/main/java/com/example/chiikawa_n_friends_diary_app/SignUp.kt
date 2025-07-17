package com.example.chiikawa_n_friends_diary_app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val imgChiikawa: ImageView = findViewById(R.id.imgChiikawa)

        Glide.with(this)
            .load(R.drawable.adorablecutiechiikawa)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
            .into(imgChiikawa)
    }
}
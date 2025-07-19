package com.example.chiikawa_n_friends_diary_app

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUp : AppCompatActivity() {

    lateinit var btnSignUp: Button
    lateinit var btnBack: Button
    private lateinit var etYourEmail: EditText
    private lateinit var etYourPassword: EditText
    private lateinit var etYourConfirmPassword: EditText
    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //init firebase auth
        auth = Firebase.auth

        //init fields
        etYourEmail = findViewById(R.id.etYourEmail)
        etYourPassword = findViewById(R.id.etYourPassword)
        etYourConfirmPassword = findViewById(R.id.etYourConfirmPassword)

        //show and hide pass
        val ivToggle = findViewById<ImageView>(R.id.ivToggleYourPassword)

        ivToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etYourPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivToggle.setImageResource(R.drawable.ic_eye_open)
            } else {
                etYourPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivToggle.setImageResource(R.drawable.ic_eye_closed)
            }
            etYourPassword.typeface = resources.getFont(R.font.chewy)
            etYourPassword.setSelection(etYourPassword.text.length)
        }

        //confirm pass toggle
        val ivConfirmToggle = findViewById<ImageView>(R.id.ivToggleYourConfirmPassword)

        ivConfirmToggle.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            if (isConfirmPasswordVisible) {
                etYourConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivConfirmToggle.setImageResource(R.drawable.ic_eye_open)
            } else {
                etYourConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivConfirmToggle.setImageResource(R.drawable.ic_eye_closed)
            }
            etYourConfirmPassword.typeface = resources.getFont(R.font.chewy)
            etYourConfirmPassword.setSelection(etYourConfirmPassword.text.length)
        }


        btnSignUp = findViewById<Button>(R.id.btnSignUp)
        btnSignUp.setOnClickListener{
            val email = etYourEmail.text.toString()
            val password = etYourPassword.text.toString()
            val confirmPassword = etYourConfirmPassword.text.toString()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword){
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Sign Up Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainMenu::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }


        btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener{
            val intent = Intent(this, SignupLogin::class.java)
            startActivity(intent)
        }

    }
}
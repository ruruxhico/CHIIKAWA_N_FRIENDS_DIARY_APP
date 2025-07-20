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
import androidx.core.content.edit
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

    lateinit var etYourFName: EditText
    lateinit var etYourMName: EditText
    lateinit var etYourLName: EditText
    lateinit var etYourBYear: EditText
    lateinit var etYourBMonth: EditText
    lateinit var editTextText14: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        auth = Firebase.auth

        etYourEmail = findViewById(R.id.etYourEmail)
        etYourPassword = findViewById(R.id.etYourPassword)
        etYourConfirmPassword = findViewById(R.id.etYourConfirmPassword)
        etYourFName = findViewById(R.id.etYourFName)
        etYourMName = findViewById(R.id.etYourMName)
        etYourLName = findViewById(R.id.etYourLName)
        etYourBYear = findViewById(R.id.etYourBYear)
        etYourBMonth = findViewById(R.id.etYourBMonth)
        editTextText14 = findViewById(R.id.editTextText14)

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

        btnSignUp = findViewById(R.id.btnSignUp)
        btnSignUp.setOnClickListener {
            val email = etYourEmail.text.toString().trim()
            val password = etYourPassword.text.toString()
            val confirmPassword = etYourConfirmPassword.text.toString()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        firebaseUser?.let {
                            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                            sharedPref.edit {
                                putString("CURRENT_USER", it.uid)
                                putString("FIRST_NAME", etYourFName.text.toString())
                                putString("MIDDLE_NAME", etYourMName.text.toString())
                                putString("LAST_NAME", etYourLName.text.toString())
                                putString("EMAIL", etYourEmail.text.toString())
                                putString("BIRTH_DAY", editTextText14.text.toString())
                                putString("BIRTH_MONTH", etYourBMonth.text.toString())
                                putString("BIRTH_YEAR", etYourBYear.text.toString())
                                apply()
                            }
                            Toast.makeText(this, "Sign Up Successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainMenu::class.java))
                            finish()
                        } ?: run {
                            Toast.makeText(this, "Sign Up successful, but user ID not found.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            startActivity(Intent(this, SignupLogin::class.java))
            finish()
        }
    }
}

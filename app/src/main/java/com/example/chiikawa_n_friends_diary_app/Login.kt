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
import androidx.core.content.edit
import com.google.firebase.firestore.FirebaseFirestore


class Login : AppCompatActivity() {

    lateinit var btnLogin: Button
    lateinit var btnBack: Button
    private lateinit var etLoginEmail: EditText
    private lateinit var etLoginPassword: EditText
    private lateinit var auth: FirebaseAuth
    private var isLoginPasswordVisible = false

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
    }
        //initialise firebase auth
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        etLoginPassword = findViewById(R.id.etLoginPassword)
        etLoginEmail = findViewById(R.id.etLoginEmail)

        //show and hide pass
        val ivToggle = findViewById<ImageView>(R.id.ivTogglePassword)

        ivToggle.setOnClickListener {
            isLoginPasswordVisible = !isLoginPasswordVisible
            if (isLoginPasswordVisible) {
                etLoginPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivToggle.setImageResource(R.drawable.ic_eye_open)
            } else {
                etLoginPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivToggle.setImageResource(R.drawable.ic_eye_closed)
            }
            etLoginPassword.typeface = resources.getFont(R.font.chewy)
            etLoginPassword.setSelection(etLoginPassword.text.length)
        }

        //login
        btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            val email = etLoginEmail.text.toString()
            val password = etLoginPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        firebaseUser?.let{ user ->
                            val userId = user.uid

                            // fetch user from firestore/db
                            db.collection("users").document(userId).get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                                        sharedPref.edit {
                                            putString("CURRENT_USER", userId)
                                            putString("FIRST_NAME", document.getString("firstName"))
                                            putString("MIDDLE_NAME", document.getString("middleName"))
                                            putString("LAST_NAME", document.getString("lastName"))
                                            putString("EMAIL", document.getString("email"))
                                            putString("BIRTH_DAY", document.getString("birthDay"))
                                            putString("BIRTH_MONTH", document.getString("birthMonth"))
                                            putString("BIRTH_YEAR", document.getString("birthYear"))
                                            apply()
                                        }
                                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, MainMenu::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Login successful, but profile data not found in Firestore. Please sign up again.", Toast.LENGTH_LONG).show()
                                        auth.signOut()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Login successful, but failed to load profile data: ${e.message}", Toast.LENGTH_LONG).show()
                                    startActivity(Intent(this, MainMenu::class.java))
                                    finish()
                                }
                        } ?: run {
                            Toast.makeText(this, "Login successful, but user ID not found.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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
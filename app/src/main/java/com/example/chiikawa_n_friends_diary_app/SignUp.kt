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
import com.google.firebase.firestore.FirebaseFirestore

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
    lateinit var etYourBDay: EditText

    private lateinit var db: FirebaseFirestore

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
        db = FirebaseFirestore.getInstance()

        etYourEmail = findViewById(R.id.etYourEmail)
        etYourPassword = findViewById(R.id.etYourPassword)
        etYourConfirmPassword = findViewById(R.id.etYourConfirmPassword)
        etYourFName = findViewById(R.id.etYourFName)
        etYourMName = findViewById(R.id.etYourMName)
        etYourLName = findViewById(R.id.etYourLName)
        etYourBYear = findViewById(R.id.etYourBYear)
        etYourBMonth = findViewById(R.id.etYourBMonth)
        etYourBDay = findViewById(R.id.editTextText14)

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

            //fetch input details
            val fName = etYourFName.text.toString().trim()
            val mName = etYourMName.text.toString().trim()
            val lName = etYourLName.text.toString().trim()
            val bDay = etYourBDay.text.toString().trim()
            val bMonth = etYourBMonth.text.toString().trim()
            val bYear = etYourBYear.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                fName.isEmpty() || mName.isEmpty() || lName.isEmpty() || bDay.isEmpty() || bMonth.isEmpty() || bYear.isEmpty()) {
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
                        firebaseUser?.let { user ->
                            val userId = user.uid
                            val userProfile = hashMapOf(
                                "firstName" to fName,
                                "middleName" to mName,
                                "lastName" to lName,
                                "email" to email,
                                "birthDay" to bDay,
                                "birthMonth" to bMonth,
                                "birthYear" to bYear
                            )
                            db.collection("users").document(userId)
                                .set(userProfile)
                                .addOnSuccessListener {
                                    // shared prefs section
                                    val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                                    sharedPref.edit {
                                        putString("CURRENT_USER", userId)
                                        putString("FIRST_NAME", fName)
                                        putString("MIDDLE_NAME", mName)
                                        putString("LAST_NAME", lName)
                                        putString("EMAIL", email)
                                        putString("BIRTH_DAY", bDay)
                                        putString("BIRTH_MONTH", bMonth)
                                        putString("BIRTH_YEAR", bYear)
                                        apply()
                                    }
                                    Toast.makeText(this, "Sign Up Successful!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainMenu::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to save profile: ${e.message}", Toast.LENGTH_LONG).show()
                                    auth.signOut()
                                }
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

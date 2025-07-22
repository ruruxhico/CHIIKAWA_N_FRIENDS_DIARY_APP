package com.example.chiikawa_n_friends_diary_app

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditProfile : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var etYourFname: EditText
    private lateinit var etYourMName: EditText
    private lateinit var etYourLName: EditText
    private lateinit var tvYourEmail: TextView
    private lateinit var etYourBDay: EditText
    private lateinit var etYourBMonth: EditText
    private lateinit var etYourBYear: EditText
    private lateinit var btnSaveChanges: Button
    private lateinit var btnCancel: Button

    //firebase var
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        //init auths
        auth = Firebase.auth
        db = Firebase.firestore
        sp = getSharedPreferences("UserSession", MODE_PRIVATE)

        //var objects
        ivBack = findViewById(R.id.ivBack)
        etYourFname = findViewById(R.id.etYourFname)
        etYourMName = findViewById(R.id.etYourMName)
        etYourLName = findViewById(R.id.etYourLName)
        tvYourEmail = findViewById(R.id.tvYourEmail)
        etYourBDay = findViewById(R.id.etYourBDay)
        etYourBMonth = findViewById(R.id.etYourBMonth)
        etYourBYear = findViewById(R.id.etYourBYear)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        btnCancel = findViewById(R.id.btnCancel)

        loadCurrentProfileData()

        btnCancel.setOnClickListener {
            finish()
        }

        ivBack.setOnClickListener {
            finish()
        }

        btnSaveChanges.setOnClickListener {
            saveProfileDetails()
        }
    }

    private fun loadCurrentProfileData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        etYourFname.setText(document.getString("firstName"))
                        etYourMName.setText(document.getString("middleName"))
                        etYourLName.setText(document.getString("lastName"))
                        tvYourEmail.text = document.getString("email") ?: currentUser.email
                        etYourBDay.setText(document.getString("birthDay"))
                        etYourBMonth.setText(document.getString("birthMonth"))
                        etYourBYear.setText(document.getString("birthYear"))
                    } else {
                        // load from shared preds if not in firebase
                        etYourFname.setText(sp.getString("FIRST_NAME", ""))
                        etYourMName.setText(sp.getString("MIDDLE_NAME", ""))
                        etYourLName.setText(sp.getString("LAST_NAME", ""))
                        tvYourEmail.text = document.getString("EMAIL") ?: currentUser.email
                        etYourBDay.setText(sp.getString("BIRTH_DAY", ""))
                        etYourBMonth.setText(sp.getString("BIRTH_MONTH", ""))
                        etYourBYear.setText(sp.getString("BIRTH_YEAR", ""))
                        Toast.makeText(this, "No cloud profile, editing local data.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    etYourFname.setText(sp.getString("FIRST_NAME", ""))
                    etYourMName.setText(sp.getString("MIDDLE_NAME", ""))
                    etYourLName.setText(sp.getString("LAST_NAME", ""))
                    tvYourEmail.text = sp.getString("EMAIL", "") ?: currentUser.email
                    etYourBDay.setText(sp.getString("BIRTH_DAY", ""))
                    etYourBMonth.setText(sp.getString("BIRTH_MONTH", ""))
                    etYourBYear.setText(sp.getString("BIRTH_YEAR", ""))
                    Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveProfileDetails() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in to save profile.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val firstName = etYourFname.text.toString().trim()
        val middleName = etYourMName.text.toString().trim()
        val lastName = etYourLName.text.toString().trim()
        val birthDay = etYourBDay.text.toString().trim()
        val birthMonth = etYourBMonth.text.toString().trim()
        val birthYear = etYourBYear.text.toString().trim()

        //checking fields if empty
        if (firstName.isEmpty() || lastName.isEmpty() ||
            birthDay.isEmpty() || birthMonth.isEmpty() || birthYear.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val profileData = hashMapOf(
            "firstName" to firstName,
            "middleName" to middleName,
            "lastName" to lastName,
            "birthDay" to birthDay,
            "birthMonth" to birthMonth,
            "birthYear" to birthYear
        )

        db.collection("users").document(userId)
            .set(profileData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                val editor: SharedPreferences.Editor = sp.edit()
                editor.putString("FIRST_NAME", firstName)
                editor.putString("MIDDLE_NAME", middleName)
                editor.putString("LAST_NAME", lastName)
                editor.putString("BIRTH_DAY", birthDay)
                editor.putString("BIRTH_MONTH", birthMonth)
                editor.putString("BIRTH_YEAR", birthYear)
                editor.apply()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
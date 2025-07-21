package com.example.chiikawa_n_friends_diary_app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.android.gms.tasks.Tasks

class Profile : AppCompatActivity() {


    private lateinit var ivBack: ImageView

    private lateinit var tvYourFname: TextView
    private lateinit var tvYourMName: TextView
    private lateinit var tvYourLName: TextView
    private lateinit var tvYourEmail: TextView
    private lateinit var tvYourBDay: TextView
    private lateinit var tvYourBMonth: TextView
    private lateinit var tvYourBYear: TextView
    private lateinit var btnEditDetails: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnDeleteAccount: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sp: SharedPreferences

    //for log msgs
    private val message = "ProfileDelete"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        //firebase objs
        auth = Firebase.auth
        db = Firebase.firestore
        sp = getSharedPreferences("UserSession", MODE_PRIVATE)

        tvYourFname = findViewById(R.id.tvYourFname)
        tvYourMName = findViewById(R.id.tvYourMName)
        tvYourLName = findViewById(R.id.tvYourLName)
        tvYourEmail = findViewById(R.id.tvYourEmail)
        tvYourBDay = findViewById(R.id.tvYourBDay)
        tvYourBMonth = findViewById(R.id.tvYourBMonth)
        tvYourBYear = findViewById(R.id.tvYourBYear)
        ivBack = findViewById(R.id.ivBack)

        btnEditDetails = findViewById(R.id.btnEditDetails)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)

        ivBack.setOnClickListener {
            startActivity(Intent(this, MainMenu::class.java))
            finish()
        }

        loadUserProfile()

        btnEditDetails.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

        btnChangePassword.setOnClickListener {
            val intent = Intent(this, ChangePass::class.java)
            startActivity(intent)
        }

        btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userID = currentUser.uid
            db.collection("users").document(userID)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        tvYourFname.text = document.getString("firstName")
                        tvYourMName.text = document.getString("middleName")
                        tvYourLName.text = document.getString("lastName")
                        tvYourEmail.text = document.getString("email") ?: currentUser.email
                        tvYourBDay.text = document.getString("birthDay")
                        tvYourBMonth.text = document.getString("birthMonth")
                        tvYourBYear.text = document.getString("birthYear")

                        // sycn shared prefs and firestore/firebase data
                        val editor: SharedPreferences.Editor = sp.edit()
                        editor.putString("FIRST_NAME", document.getString("firstName"))
                        editor.putString("MIDDLE_NAME", document.getString("middleName"))
                        editor.putString("LAST_NAME", document.getString("lastName"))
                        editor.putString("EMAIL", document.getString("email") ?: currentUser.email)
                        editor.putString("BIRTH_DAY", document.getString("birthDay"))
                        editor.putString("BIRTH_MONTH", document.getString("birthMonth"))
                        editor.putString("BIRTH_YEAR", document.getString("birthYear"))
                        editor.apply()
                    } else {
                        loadFromSharedPreferences()
                        Toast.makeText(this, "Profile not found in cloud, showing local data.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    loadFromSharedPreferences()
                    Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            clearProfileDisplay()
            Toast.makeText(this, "No user logged in. Please log in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFromSharedPreferences() {
        tvYourFname.text = sp.getString("FIRST_NAME", "")
        tvYourMName.text = sp.getString("MIDDLE_NAME", "")
        tvYourLName.text = sp.getString("LAST_NAME", "")
        tvYourEmail.text = sp.getString("EMAIL", auth.currentUser?.email ?: "")
        tvYourBDay.text = sp.getString("BIRTH_DAY", "")
        tvYourBMonth.text = sp.getString("BIRTH_MONTH", "")
        tvYourBYear.text = sp.getString("BIRTH_YEAR", "")
    }

    private fun clearProfileDisplay() {
        tvYourFname.text = ""
        tvYourMName.text = ""
        tvYourLName.text = ""
        tvYourEmail.text = ""
        tvYourBDay.text = ""
        tvYourBMonth.text = ""
        tvYourBYear.text = ""
    }

    // for deleting accs
    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { dialog, _ ->
                dialog.dismiss()
                promptPassDeletion()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun promptPassDeletion() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUser.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }) {
            val passwordEditText = EditText(this).apply {
                hint = "Enter your current password"
                inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD or android.text.InputType.TYPE_CLASS_TEXT
            }

            AlertDialog.Builder(this)
                .setTitle("Confirm Password")
                .setMessage("Please enter your current password to proceed.")
                .setView(passwordEditText)
                .setPositiveButton("Confirm") { dialog, _ ->
                    val password = passwordEditText.text.toString().trim()
                    if (password.isNotEmpty()) {
                        dialog.dismiss()
                        reauthenticateAndDeleteUserAndData(currentUser, password)
                    } else {
                        Toast.makeText(this, "Password cannot be empty.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            reauthenticateAndDeleteUserAndData(currentUser, null)
        }
    }

    private fun reauthenticateAndDeleteUserAndData(user: com.google.firebase.auth.FirebaseUser, password: String?) {
        val reauthOperation = if (password != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, password)
            user.reauthenticate(credential)
        } else {
            Tasks.forResult(null)
        }

        reauthOperation.addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                user.delete()
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            val userId = user.uid
                            val userDocRef = db.collection("users").document(userId)

                            userDocRef.delete()
                                .addOnSuccessListener {
                                    db.collection("users").document(userId)
                                        .collection("diaryEntries")
                                        .get()
                                        .addOnSuccessListener { querySnapshot ->
                                            val batch = db.batch()
                                            for (document in querySnapshot.documents) {
                                                batch.delete(document.reference)
                                            }
                                            if (!querySnapshot.isEmpty) {
                                                batch.commit()
                                                    .addOnSuccessListener {
                                                        Toast.makeText(this, "Account successfully deleted!", Toast.LENGTH_LONG).show()
                                                        clearAndRedirectToLogin()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e(message, "Failed to delete diary entries for user $userId: ${e.message}", e)
                                                        Toast.makeText(this, "Account successfully deleted!", Toast.LENGTH_LONG).show()
                                                        clearAndRedirectToLogin()
                                                    }
                                            } else {
                                                Toast.makeText(this, "Account successfully deleted!", Toast.LENGTH_LONG).show()
                                                clearAndRedirectToLogin()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(message, "Failed to query diary entries for user $userId: ${e.message}", e)
                                            Toast.makeText(this, "Account successfully deleted!", Toast.LENGTH_LONG).show()
                                            clearAndRedirectToLogin()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(message, "Failed to delete primary user document $userId from Firestore: ${e.message}", e)
                                    Toast.makeText(this, "Account successfully deleted!", Toast.LENGTH_LONG).show()
                                    clearAndRedirectToLogin()
                                }
                        } else {
                            Toast.makeText(this, "Failed to delete account: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                            Log.e(message, "Firebase Auth account deletion failed for user ${user.uid}: ${authTask.exception?.message}", authTask.exception)
                        }
                    }
            } else {
                // if auth failed
                Toast.makeText(this, "Authentication failed: Incorrect password or not recent login. Please try again.", Toast.LENGTH_LONG).show()
                Log.e(message, "Reauthentication failed for user ${user.uid}: ${reauthTask.exception?.message}", reauthTask.exception)
            }
        }
    }


    private fun clearAndRedirectToLogin() {
        sp.edit().clear().apply() //clean sessions
        val intent = Intent(this, Startup::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
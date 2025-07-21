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
            // Option 1: Start a new activity for editing details
            val intent = Intent(this, EditProfile::class.java) // Create EditProfileActivity
            startActivity(intent)

            // Option 2 (Alternative): Show an AlertDialog with EditTexts for editing
            // This would be more complex to implement directly here for all fields
        }

        btnChangePassword.setOnClickListener {
            // Option 1: Start a new activity for changing password
            val intent = Intent(this, ChangePass::class.java) // Create ChangePasswordActivity
            startActivity(intent)

            // Option 2 (Alternative): Show an AlertDialog asking for old/new password
            // This is also a common approach.
        }

        btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload data from Firestore whenever the activity resumes (e.g., after editing)
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
                        tvYourEmail.text = document.getString("email") ?: currentUser.email // Prioritize Firestore, fallback to Auth email
                        tvYourBDay.text = document.getString("birthDay")
                        tvYourBMonth.text = document.getString("birthMonth")
                        tvYourBYear.text = document.getString("birthYear")

                        // Update SharedPreferences with the latest Firestore data
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
                        // Document doesn't exist in Firestore, use data from SharedPreferences
                        loadFromSharedPreferences()
                        Toast.makeText(this, "Profile not found in cloud, showing local data.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Error fetching from Firestore, use data from SharedPreferences
                    loadFromSharedPreferences()
                    Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            // No user logged in, clear UI or show login prompt
            clearProfileDisplay()
            Toast.makeText(this, "No user logged in. Please log in.", Toast.LENGTH_SHORT).show()
            // Optionally redirect to login screen:
            // startActivity(Intent(this, Startup::class.java))
            // finish()
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

    // --- Delete Account Feature ---

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
            // For other providers (Google, Facebook), assume reauthentication isn't needed or handled automatically
            reauthenticateAndDeleteUserAndData(currentUser, null) // Pass null for password
        }
    }

    private fun reauthenticateAndDeleteUserAndData(user: com.google.firebase.auth.FirebaseUser, password: String?) {
        val reauthOperation = if (password != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, password)
            user.reauthenticate(credential)
        } else {
            // If no password (e.g., social login) or email is null, skip explicit reauthentication
            // This relies on Firebase token freshness or provider handling.
            Tasks.forResult(null) // Return a completed task if reauth not strictly needed
        }

        reauthOperation.addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                // Reauthentication successful or not required. Proceed with deletion.

                // Start chained deletion: Auth account -> Firestore user doc -> Firestore subcollection
                user.delete() // Delete Auth account
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            // Auth account successfully deleted.
                            // Now attempt to delete Firestore data silently (from user's perspective).
                            val userId = user.uid
                            val userDocRef = db.collection("users").document(userId)

                            userDocRef.delete() // Attempt to delete primary user document
                                .addOnSuccessListener {
                                    // Primary user document deleted. Now attempt subcollection.
                                    db.collection("users").document(userId)
                                        .collection("diaryEntries")
                                        .get()
                                        .addOnSuccessListener { querySnapshot ->
                                            val batch = db.batch()
                                            for (document in querySnapshot.documents) {
                                                batch.delete(document.reference)
                                            }
                                            if (!querySnapshot.isEmpty) { // Only commit if there are entries
                                                batch.commit()
                                                    .addOnSuccessListener {
                                                        // All deletions (Auth, primary doc, diary entries) succeeded.
                                                        Toast.makeText(this, "Account successfully deleted!", Toast.LENGTH_LONG).show()
                                                        clearAndRedirectToLogin()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        // Auth and primary doc deleted, but diary entries failed.
                                                        // User still gets success message as per requirement, but we log the error.
                                                        Log.e(message, "Failed to delete diary entries for user $userId: ${e.message}", e)
                                                        Toast.makeText(this, "Account successfully deleted!", Toast.LENGTH_LONG).show()
                                                        clearAndRedirectToLogin()
                                                    }
                                            } else {
                                                // Auth and primary doc deleted, no diary entries to delete.
                                                Toast.makeText(this, "Account successfully deleted!", Toast.LENGTH_LONG).show()
                                                clearAndRedirectToLogin()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            // Auth and primary doc deleted, but query for diary entries failed.
                                            Log.e(message, "Failed to query diary entries for user $userId: ${e.message}", e)
                                            Toast.makeText(this, "Account successfully deleted!", Toast.LENGTH_LONG).show()
                                            clearAndRedirectToLogin()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    // Auth account deleted, but primary user document failed to delete.
                                    Log.e(message, "Failed to delete primary user document $userId from Firestore: ${e.message}", e)
                                    Toast.makeText(this, "Account successfully deleted!", Toast.LENGTH_LONG).show()
                                    clearAndRedirectToLogin()
                                }
                        } else {
                            // Firebase Auth account deletion failed. This is the primary failure point.
                            Toast.makeText(this, "Failed to delete account: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                            Log.e(message, "Firebase Auth account deletion failed for user ${user.uid}: ${authTask.exception?.message}", authTask.exception)
                        }
                    }
            } else {
                // Reauthentication itself failed.
                Toast.makeText(this, "Authentication failed: Incorrect password or not recent login. Please try again.", Toast.LENGTH_LONG).show()
                Log.e(message, "Reauthentication failed for user ${user.uid}: ${reauthTask.exception?.message}", reauthTask.exception)
            }
        }
    }


    private fun clearAndRedirectToLogin() {
        sp.edit().clear().apply() // Clear all user session data
        val intent = Intent(this, Startup::class.java) // Assuming Startup is your initial login/signup screen
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear back stack
        startActivity(intent)
        finish() // Finish the current activity
    }
}
package com.example.chiikawa_n_friends_diary_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth

class ChangePass: AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmNewPassword: EditText
    private lateinit var btnUpdatePassword: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_pass)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        //init auth
        auth = Firebase.auth

        //init objects
        ivBack = findViewById(R.id.ivBack)
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword)
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword)

        //navigations
        ivBack.setOnClickListener {
            finish()
        }

        btnUpdatePassword.setOnClickListener {
            changeUserPassword()
        }
    }

    // changing passwords
    private fun changeUserPassword() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentPassword = etCurrentPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmNewPassword = etConfirmNewPassword.text.toString().trim()

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmNewPassword) {
            Toast.makeText(this, "New passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(this, "New password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
            return
        }

        // input pass again
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // actual updating password
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updatePasswordTask ->
                            if (updatePasswordTask.isSuccessful) {
                                Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "Failed to update password: ${updatePasswordTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Authentication failed: Current password is incorrect.", Toast.LENGTH_LONG).show()
                }
            }
    }
}
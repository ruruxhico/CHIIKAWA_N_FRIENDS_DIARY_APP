package com.example.chiikawa_n_friends_diary_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Write : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnSave: Button
    private lateinit var etmWrite: EditText
    private lateinit var tvWrite: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0

    private lateinit var entryDocumentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_write)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        //firebase and firestore
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        etmWrite = findViewById(R.id.etmWrite)
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)
        tvWrite = findViewById(R.id.tvEntry) // Update this ID if your layout uses a different one

        selectedYear = intent.getIntExtra("EXTRA_YEAR", 0)
        selectedMonth = intent.getIntExtra("EXTRA_MONTH", 0)
        selectedDay = intent.getIntExtra("EXTRA_DAY", 0)

        // --- Crucial: Check if a user is logged in before proceeding ---
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to write diary entries.", Toast.LENGTH_LONG).show()
            // Redirect to your Login or Startup activity if no user is logged in
            val intent = Intent(this, Login::class.java) // Assuming 'Login' is your login activity
            startActivity(intent)
            finish() // Finish this activity to prevent going back to it
            return // Stop further execution of onCreate
        }

        // Construct the unique document ID for the diary entry (e.g., "2025-07-20")
        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, selectedDay)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        entryDocumentId = dateFormat.format(calendar.time)

        // --- Fetch the existing diary entry for this user and date from Firestore ---
        fetchDiaryEntry(currentUser.uid, entryDocumentId)

        // Back button logic
        btnBack.setOnClickListener {
            // simply return to previous activity (Notes in your case)
            // You might want to start Notes explicitly if you want to ensure a refresh
            val intent = Intent(this, Notes::class.java)
            startActivity(intent)
            finish()
        }

        // Save button logic (now handles appending to Firestore)
        btnSave.setOnClickListener {
            val newTextToAppend = etmWrite.text.toString().trim()

            if (newTextToAppend.isNotBlank()) {
                // Get the current text displayed in tvWrite (which should be the existing entry)
                val currentFullEntry = tvWrite.text.toString()

                // Append the new text
                val updatedFullEntry = if (currentFullEntry.isEmpty()) {
                    newTextToAppend
                } else {
                    "$currentFullEntry\n$newTextToAppend"
                }

                // Save the combined text to Firestore
                saveDiaryEntry(currentUser.uid, entryDocumentId, updatedFullEntry)

            } else {
                Toast.makeText(this, "Please write something to append!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Fetches the diary entry for the given user and date from Firestore.
     * Updates tvWrite with the fetched content.
     */
    private fun fetchDiaryEntry(userId: String, dateId: String) {
        val docRef = db.collection("users").document(userId)
            .collection("diaryEntries").document(dateId)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val content = document.getString("content")
                    tvWrite.text = content ?: "" // Display content, default to empty string if null
                } else {
                    tvWrite.text = "" // No entry for this date, keep tvWrite empty
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching entry: ${exception.message}", Toast.LENGTH_LONG).show()
                tvWrite.text = "" // Clear on error
            }
    }

    /**
     * Saves (or updates) the full diary entry for the given user and date to Firestore.
     * Clears etmWrite after successful save.
     */
    private fun saveDiaryEntry(userId: String, dateId: String, fullContent: String) {
        val docRef = db.collection("users").document(userId)
            .collection("diaryEntries").document(dateId)

        val entryData = hashMapOf(
            "date" to dateId, // Store date as a field too for easier querying/sorting if needed
            "timestamp" to System.currentTimeMillis(), // Optional: Store timestamp for sorting
            "content" to fullContent // This now stores the entire updated content
        )

        docRef.set(entryData) // 'set' will create or overwrite the document
            .addOnSuccessListener {
                Toast.makeText(this, "Entry appended and saved to cloud!", Toast.LENGTH_SHORT).show()
                etmWrite.text.clear() // Clear the input field after appending
                // You might want to navigate back or just leave them on the page
                // For a diary, often you save and just stay on the same screen.
                // If you want to go back to Notes:
                val intent = Intent(this, Notes::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving entry: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}

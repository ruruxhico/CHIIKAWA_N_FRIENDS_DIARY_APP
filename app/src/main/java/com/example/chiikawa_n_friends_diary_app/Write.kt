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
    private lateinit var btnDelete: Button
    private lateinit var btnSave: Button
    private lateinit var etmWrite: EditText
    private lateinit var tvEntry: TextView

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

        // Firebase init
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        etmWrite = findViewById(R.id.etmWrite)
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
        tvEntry = findViewById(R.id.tvEntry)

        selectedYear = intent.getIntExtra("EXTRA_YEAR", 0)
        selectedMonth = intent.getIntExtra("EXTRA_MONTH", 0)
        selectedDay = intent.getIntExtra("EXTRA_DAY", 0)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to write diary entries.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, selectedDay)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        entryDocumentId = dateFormat.format(calendar.time)

        fetchDiaryEntry(currentUser.uid, entryDocumentId)

        btnBack.setOnClickListener {
            val intent = Intent(this, Notes::class.java)
            startActivity(intent)
            finish()
        }

        btnDelete.setOnClickListener {
            val docRef = db.collection("users").document(currentUser.uid)
                .collection("diaryEntries").document(entryDocumentId)

            docRef.delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Diary entry deleted!", Toast.LENGTH_SHORT).show()
                    tvEntry.text = ""
                    etmWrite.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error deleting entry: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        btnSave.setOnClickListener {
            val newTextToAppend = etmWrite.text.toString().trim()

            if (newTextToAppend.isNotBlank()) {
                val currentFullEntry = tvEntry.text.toString()
                val updatedFullEntry = if (currentFullEntry.isEmpty()) {
                    newTextToAppend
                } else {
                    "$currentFullEntry\n$newTextToAppend"
                }

                saveDiaryEntry(currentUser.uid, entryDocumentId, updatedFullEntry)
            } else {
                Toast.makeText(this, "Please write something to append!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchDiaryEntry(userId: String, dateId: String) {
        val docRef = db.collection("users").document(userId)
            .collection("diaryEntries").document(dateId)

        docRef.get()
            .addOnSuccessListener { document ->
                tvEntry.text = if (document.exists()) document.getString("content").orEmpty() else ""
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching entry: ${exception.message}", Toast.LENGTH_LONG).show()
                tvEntry.text = ""
            }
    }

    private fun saveDiaryEntry(userId: String, dateId: String, fullContent: String) {
        val docRef = db.collection("users").document(userId)
            .collection("diaryEntries").document(dateId)

        val entryData = hashMapOf(
            "date" to dateId,
            "timestamp" to System.currentTimeMillis(),
            "content" to fullContent
        )

        docRef.set(entryData)
            .addOnSuccessListener {
                Toast.makeText(this, "Entry appended and saved to cloud!", Toast.LENGTH_SHORT).show()
                etmWrite.text.clear()
                tvEntry.text = fullContent
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving entry: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}

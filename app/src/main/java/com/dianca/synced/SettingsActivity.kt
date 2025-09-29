package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var spinnerLanguage: Spinner
    private lateinit var switchMessages: Switch
    private lateinit var switchMatches: Switch
    private lateinit var switchFriendActivity: Switch
    private lateinit var btnViewRules: Button
    private lateinit var btnBlockReport: Button
    private lateinit var btnLogout: Button
    private lateinit var btnDeleteAccount: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val blockedUsers = mutableSetOf<String>()  // keep track in session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Bind views
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        switchMessages = findViewById(R.id.switchMessages)
        switchMatches = findViewById(R.id.switchMatches)
        switchFriendActivity = findViewById(R.id.switchFriendActivity)
        btnViewRules = findViewById(R.id.btnViewRules)
        btnBlockReport = findViewById(R.id.btnBlockReport)
        btnLogout = findViewById(R.id.btnLogout)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupLanguageSpinner()
        setupNotifications()
        setupCommunityActions()
        setupLogout()
        setupDeleteAccount()
    }

    // -----------------------
    // Language
    // -----------------------
    private fun setupLanguageSpinner() {
        val languages = listOf("English", "Zulu", "French")
        spinnerLanguage.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, languages
        )

        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selected = languages[position]
                val locale = when (selected) {
                    "Zulu" -> Locale("zu")
                    "French" -> Locale("fr")
                    else -> Locale("en")
                }
                Locale.setDefault(locale)
                val config = resources.configuration
                config.setLocale(locale)
                resources.updateConfiguration(config, resources.displayMetrics)
                Toast.makeText(this@SettingsActivity, "Language set to $selected", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // -----------------------
    // Notifications
    // -----------------------
    private fun setupNotifications() {
        val uid = auth.currentUser?.uid ?: return
        // Load saved preferences for this session
        val prefs = getSharedPreferences("notifPrefs", MODE_PRIVATE)
        switchMessages.isChecked = prefs.getBoolean("messages", true)
        switchMatches.isChecked = prefs.getBoolean("matches", true)
        switchFriendActivity.isChecked = prefs.getBoolean("friendActivity", true)

        val listener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            val editor = prefs.edit()
            when (buttonView.id) {
                R.id.switchMessages -> editor.putBoolean("messages", isChecked)
                R.id.switchMatches -> editor.putBoolean("matches", isChecked)
                R.id.switchFriendActivity -> editor.putBoolean("friendActivity", isChecked)
            }
            editor.apply()
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        }

        switchMessages.setOnCheckedChangeListener(listener)
        switchMatches.setOnCheckedChangeListener(listener)
        switchFriendActivity.setOnCheckedChangeListener(listener)
    }

    private fun setupCommunityActions() {
        btnViewRules.setOnClickListener {
            startActivity(Intent(this, RulesActivity::class.java))
        }

        btnBlockReport.setOnClickListener {
            // Fetch current synced friends
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("users").document(uid)
                .collection("synced")
                .get()
                .addOnSuccessListener { snapshot ->
                    val friends = snapshot.documents.mapNotNull { doc ->
                        doc.getString("name")?.let { name -> name to doc.id }
                    }.toTypedArray()

                    if (friends.isEmpty()) {
                        Toast.makeText(this, "No friends to block/report", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    AlertDialog.Builder(this)
                        .setTitle("Select user to Block / Report")
                        .setItems(friends.map { it.first }.toTypedArray()) { _, which ->
                            val friendId = friends[which].second
                            showBlockReportDialog(friendId)
                        }.show()
                }
        }
    }

    private fun showBlockReportDialog(friendId: String) {
        AlertDialog.Builder(this)
            .setTitle("Block or Report User")
            .setMessage("Do you want to block, report, or both?")
            .setPositiveButton("Block") { _, _ -> blockUser(friendId) }
            .setNeutralButton("Report") { _, _ -> reportUser(friendId) }
            .setNegativeButton("Both") { _, _ ->
                reportUser(friendId)
                blockUser(friendId)
            }
            .show()
    }

    private fun blockUser(userId: String) {
        val currentUid = auth.currentUser?.uid ?: return
        blockedUsers.add(userId)

        db.collection("users").document(currentUid)
            .collection("synced").document(userId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "User blocked and removed from friends", Toast.LENGTH_SHORT).show()
            }
    }

    private fun reportUser(userId: String) {
        val report = hashMapOf(
            "reportedBy" to auth.currentUser?.uid,
            "reportedUser" to userId,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("reports").add(report).addOnSuccessListener {
            Toast.makeText(this, "User reported", Toast.LENGTH_SHORT).show()
        }
    }

    // -----------------------
    // Logout / Delete
    // -----------------------
    private fun setupLogout() {
        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupDeleteAccount() {
        btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure? This cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    val user = auth.currentUser
                    val uid = user?.uid
                    if (uid != null) {
                        db.collection("users").document(uid).delete()
                        user.delete().addOnCompleteListener {
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}

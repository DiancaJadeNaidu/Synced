package com.dianca.synced

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
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
    private lateinit var dbRoom: AppDatabase
    private var isFirstLanguageSelection = true

    private val blockedUsers = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Bind views
        bottomNav = findViewById(R.id.bottomNav)
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
        dbRoom = AppDatabase.getInstance(this)

        setupBottomNav()
        setupLanguageSpinner()
        setupNotifications()
        setupCommunityActions()
        setupLogout()
        setupDeleteAccount()
        syncBlockedUsers()
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, TopMatchesActivity::class.java))
                R.id.nav_geo -> startActivity(Intent(this, GeoLocationActivity::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_settings -> {} // already here
                R.id.nav_help -> startActivity(Intent(this, HelpActivity::class.java))
            }
            finish()
            true
        }
    }

    private fun setupLanguageSpinner() {
        val languages = resources.getStringArray(R.array.languages)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (isFirstLanguageSelection) {
                    isFirstLanguageSelection = false
                    return
                }

                val langCode = when (position) {
                    0 -> "en"
                    1 -> "af"
                    2 -> "zu"
                    else -> "en"
                }
                updateLanguage(langCode)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateLanguage(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = resources.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
            config.setLayoutDirection(locale)
        } else {
            config.locale = locale
        }

        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }

    private fun setupNotifications() {
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
            Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
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
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("users").document(uid).collection("synced").get()
                .addOnSuccessListener { snapshot ->
                    val friends = snapshot.documents.mapNotNull { doc ->
                        doc.getString("name")?.let { name -> name to doc.id }
                    }

                    if (friends.isEmpty()) {
                        Toast.makeText(this, R.string.no_friends_to_block, Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    AlertDialog.Builder(this)
                        .setTitle(R.string.select_user_block_report)
                        .setItems(friends.map { it.first }.toTypedArray()) { _, which ->
                            showBlockReportDialog(friends[which].second)
                        }
                        .show()
                }
        }
    }

    private fun showBlockReportDialog(friendId: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.block_or_report)
            .setMessage(R.string.block_or_report_msg)
            .setPositiveButton(R.string.block) { _, _ -> blockUser(friendId) }
            .setNeutralButton(R.string.report) { _, _ -> reportUser(friendId) }
            .setNegativeButton(R.string.both) { _, _ ->
                reportUser(friendId)
                blockUser(friendId)
            }
            .show()
    }

    private fun blockUser(userId: String) {
        val currentUid = auth.currentUser?.uid ?: return
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { dbRoom.blockedUserDao().insert(BlockedUser(userId)) }
            blockedUsers.add(userId)
            Toast.makeText(this@SettingsActivity, R.string.user_blocked_locally, Toast.LENGTH_SHORT).show()

            db.collection("users").document(currentUid).collection("synced").document(userId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this@SettingsActivity, R.string.user_synced_server, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this@SettingsActivity, R.string.sync_later, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun reportUser(userId: String) {
        val report = hashMapOf(
            "reportedBy" to auth.currentUser?.uid,
            "reportedUser" to userId,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("reports").add(report)
        Toast.makeText(this, R.string.user_reported, Toast.LENGTH_SHORT).show()
    }

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
                .setTitle(R.string.delete_account)
                .setMessage(R.string.delete_account_confirm)
                .setPositiveButton(R.string.delete) { _, _ ->
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
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun syncBlockedUsers() {
        lifecycleScope.launch(Dispatchers.IO) {
            val blockedList = dbRoom.blockedUserDao().getAll()
            val currentUid = auth.currentUser?.uid ?: return@launch
            blockedList.forEach { user ->
                db.collection("users").document(currentUid).collection("synced")
                    .document(user.userId).delete()
            }
        }
    }
}

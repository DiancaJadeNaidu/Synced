package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SendMessageRequestActivity : AppCompatActivity() {

    private lateinit var imgMatchAvatar: ImageView
    private lateinit var txtMatchName: TextView
    private lateinit var etMessage: EditText
    private lateinit var btnSendRequest: Button

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var toUserId: String? = null
    private var toUserName: String? = null
    private var toUserImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_message_request)

        // Toolbar setup
        val toolbar: Toolbar = findViewById(R.id.toolbarSendMessage)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, TopMatchesActivity::class.java))
                R.id.nav_messages -> startActivity(Intent(this, SyncRequestsActivity::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_help -> startActivity(Intent(this, HelpActivity::class.java))
            }
            true
        }

        // Bind views
        imgMatchAvatar = findViewById(R.id.imgMatchAvatar)
        txtMatchName = findViewById(R.id.txtMatchName)
        etMessage = findViewById(R.id.etMessage)
        btnSendRequest = findViewById(R.id.btnSendRequest)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Get recipient data from intent
        toUserId = intent.getStringExtra("uid")
        toUserName = intent.getStringExtra("name")
        toUserImage = intent.getStringExtra("imageUrl")

        txtMatchName.text = toUserName ?: "Unknown User"

        Glide.with(this)
            .load(toUserImage)
            .placeholder(R.drawable.default_avatar_foreground)
            .into(imgMatchAvatar)

        btnSendRequest.setOnClickListener { sendRequest() }
    }

    private fun sendRequest() {
        val currentUser = auth.currentUser ?: return
        val message = etMessage.text.toString().trim()
        val recipientId = toUserId ?: return

        if (message.isEmpty()) {
            Toast.makeText(this, "Please write a message", Toast.LENGTH_SHORT).show()
            return
        }

        val requestData = hashMapOf(
            "fromId" to currentUser.uid,
            "fromName" to (auth.currentUser?.displayName ?: "Someone"),
            "message" to message,
            "timestamp" to Timestamp.now()
        )

        db.collection("users").document(recipientId)
            .collection("messageRequests")
            .add(requestData)
            .addOnSuccessListener {
                Toast.makeText(this, "Request sent ✅", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send request ❌", Toast.LENGTH_SHORT).show()
            }
    }
}

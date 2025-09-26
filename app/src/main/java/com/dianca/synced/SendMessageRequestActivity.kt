package com.dianca.synced

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
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

        imgMatchAvatar = findViewById(R.id.imgMatchAvatar)
        txtMatchName = findViewById(R.id.txtMatchName)
        etMessage = findViewById(R.id.etMessage)
        btnSendRequest = findViewById(R.id.btnSendRequest)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Get intent data from ViewProfileActivity
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

        val request = hashMapOf(
            "fromUserId" to currentUser.uid,
            "toUserId" to toUserId,
            "message" to message,
            "status" to "pending",
            "timestamp" to Timestamp.now()
        )

        db.collection("messageRequests").add(request)
            .addOnSuccessListener {
                Toast.makeText(this, "Request sent ✅", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send request ❌", Toast.LENGTH_SHORT).show()
            }
    }
}
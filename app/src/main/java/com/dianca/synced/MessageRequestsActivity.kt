package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessageRequestsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: MessageRequestAdapter
    private val requestsList = mutableListOf<MessageRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_requests)

        recyclerView = findViewById(R.id.requestsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        adapter = MessageRequestAdapter(
            requestsList,
            onAccept = { request -> acceptRequest(request) },
            onDecline = { request -> declineRequest(request) }
        )

        recyclerView.adapter = adapter

        loadRequests()
    }

    private fun loadRequests() {
        val currentUser = auth.currentUser ?: return

        db.collection("messageRequests")
            .whereEqualTo("toUserId", currentUser.uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                requestsList.clear()
                for (doc in snapshot.documents) {
                    val fromUserId = doc.getString("fromUserId") ?: continue
                    requestsList.add(MessageRequest(doc.id, fromUserId))
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun acceptRequest(request: MessageRequest) {
        val currentUser = auth.currentUser ?: return

        // 1. Create chat collection
        val chatData = hashMapOf(
            "participants" to listOf(currentUser.uid, request.fromUserId)
        )

        db.collection("chats").add(chatData).addOnSuccessListener { chatDoc ->
            // 2. Send automatic "Hi" message
            val message = hashMapOf(
                "senderId" to currentUser.uid,
                "message" to "Hi",
                "timestamp" to com.google.firebase.Timestamp.now()
            )
            db.collection("chats").document(chatDoc.id)
                .collection("messages")
                .add(message)

            // 3. Delete the request
            db.collection("messageRequests").document(request.id).delete()

            Toast.makeText(this, "Request accepted ✅", Toast.LENGTH_SHORT).show()
        }
    }

    private fun declineRequest(request: MessageRequest) {
        db.collection("messageRequests").document(request.id).delete()
        Toast.makeText(this, "Request declined ❌", Toast.LENGTH_SHORT).show()
    }
}

// Renamed class for clarity
data class MessageRequest(val id: String, val fromUserId: String)

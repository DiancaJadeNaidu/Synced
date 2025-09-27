package com.dianca.synced

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SyncRequestsActivity : AppCompatActivity() {

    private lateinit var rvRequests: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String
    private lateinit var adapter: SyncRequestsAdapter
    private val requestsList = mutableListOf<SyncRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)

        rvRequests = findViewById(R.id.rvRequests)
        rvRequests.layoutManager = LinearLayoutManager(this)

        db = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        adapter = SyncRequestsAdapter(
            requestsList,
            onAccept = { r -> acceptRequest(r) },
            onDecline = { r -> declineRequest(r) }
        )

        rvRequests.adapter = adapter
        loadRequests()
    }

    private fun loadRequests() {
        db.collection("requests")
            .whereEqualTo("receiverId", uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                requestsList.clear()
                for (doc in snapshot.documents) {
                    val request = doc.toObject<SyncRequest>() ?: continue
                    request.id = doc.id

                    db.collection("users").document(request.senderId).get()
                        .addOnSuccessListener { userDoc ->
                            request.senderName = userDoc.getString("name") ?: "Unknown"
                            request.senderImage = userDoc.getString("profileImage") ?: ""
                            request.senderAge = calculateAge(userDoc.getString("birthday") ?: "")

                            requestsList.add(request)
                            adapter.notifyDataSetChanged()
                        }
                }
            }
    }

    private fun calculateAge(birthday: String): Int {
        return try {
            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            val birthDate = sdf.parse(birthday)
            val today = Calendar.getInstance()
            val dob = Calendar.getInstance()
            dob.time = birthDate ?: return 0
            var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--
            age
        } catch (e: Exception) {
            0
        }
    }


    private fun acceptRequest(request: SyncRequest) {
        db.collection("requests").document(request.id)
            .update("status", "accepted")
            .addOnSuccessListener {
                // Add to synced list
                val syncedData = hashMapOf(
                    "friendId" to request.senderId,
                    "name" to request.senderName,
                    "profileImage" to request.senderImage
                )
                db.collection("users").document(uid)
                    .collection("synced")
                    .document(request.senderId)
                    .set(syncedData)

                Toast.makeText(this, "Synced ✅", Toast.LENGTH_SHORT).show()
                adapter.removeRequest(request)
            }
    }

    private fun declineRequest(request: SyncRequest) {
        db.collection("requests").document(request.id)
            .update("status", "declined")
            .addOnSuccessListener {
                Toast.makeText(this, "Request declined ❌", Toast.LENGTH_SHORT).show()
                adapter.removeRequest(request)
            }
    }
}

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

class RequestsActivity : AppCompatActivity() {

    private lateinit var rvRequests: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)

        rvRequests = findViewById(R.id.rvRequests)
        rvRequests.layoutManager = LinearLayoutManager(this)

        db = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        loadRequests()
    }

    private fun loadRequests() {
        db.collection("requests")
            .whereEqualTo("receiverId", uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading requests", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val requests = mutableListOf<Request>()
                    for (doc in snapshot.documents) {
                        val request = doc.toObject<Request>() ?: continue
                        request.id = doc.id // mutate properties, not reassign var

                        // fetch sender’s details
                        db.collection("users").document(request.senderId).get()
                            .addOnSuccessListener { userDoc ->
                                request.senderName = userDoc.getString("name") ?: "Unknown"
                                request.senderImage = userDoc.getString("profileImage") ?: ""
                                request.senderAge = calculateAge(userDoc.getString("birthday") ?: "")

                                requests.add(request)
                                rvRequests.adapter = RequestsAdapter(
                                    requests,
                                    onAccept = { r -> acceptRequest(r) },
                                    onDecline = { r -> declineRequest(r) }
                                )
                            }
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

    private fun acceptRequest(request: Request) {
        db.collection("requests").document(request.id)
            .update("status", "accepted")
            .addOnSuccessListener {
                Toast.makeText(this, "Request accepted ✅", Toast.LENGTH_SHORT).show()
                openSocialChat(request.senderId)
                (rvRequests.adapter as? RequestsAdapter)?.removeRequest(request)
            }
    }

    private fun declineRequest(request: Request) {
        db.collection("requests").document(request.id)
            .update("status", "declined")
            .addOnSuccessListener {
                Toast.makeText(this, "Request declined ❌", Toast.LENGTH_SHORT).show()
                (rvRequests.adapter as? RequestsAdapter)?.removeRequest(request)
            }
    }

    private fun openSocialChat(senderId: String) {
        db.collection("users").document(senderId).get().addOnSuccessListener { doc ->
            val whatsapp = doc.getString("whatsapp")?.trim()
            val instagram = doc.getString("instagram")?.trim()

            val options = mutableListOf<String>()
            if (!whatsapp.isNullOrEmpty()) options.add("WhatsApp")
            if (!instagram.isNullOrEmpty()) options.add("Instagram")

            if (options.isEmpty()) {
                Toast.makeText(this, "No contact info available", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            AlertDialog.Builder(this)
                .setTitle("Open chat with ${doc.getString("name") ?: "user"}")
                .setItems(options.toTypedArray()) { _, which ->
                    when (options[which]) {
                        "WhatsApp" -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$whatsapp")))
                        "Instagram" -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/$instagram")))
                    }
                }
                .show()
        }
    }
}

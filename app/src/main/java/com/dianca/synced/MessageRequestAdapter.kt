package com.dianca.synced

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MessageRequestAdapter(
    private val requests: List<MessageRequest>,
    private val onAccept: (MessageRequest) -> Unit,
    private val onDecline: (MessageRequest) -> Unit
) : RecyclerView.Adapter<MessageRequestAdapter.RequestViewHolder>() {

    inner class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvRequestName)
        val btnAccept: Button = view.findViewById(R.id.btnAccept)
        val btnDecline: Button = view.findViewById(R.id.btnDecline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]

        // Load sender's name from Firestore
        FirebaseFirestore.getInstance().collection("users")
            .document(request.fromUserId)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Unknown"
                holder.tvName.text = name
            }
            .addOnFailureListener {
                holder.tvName.text = "Unknown"
            }

        holder.btnAccept.setOnClickListener { onAccept(request) }
        holder.btnDecline.setOnClickListener { onDecline(request) }
    }

    override fun getItemCount(): Int = requests.size
}

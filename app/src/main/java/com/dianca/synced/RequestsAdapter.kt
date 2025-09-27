package com.dianca.synced

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RequestsAdapter(
    private val requests: MutableList<Request>,
    private val onAccept: (Request) -> Unit,
    private val onDecline: (Request) -> Unit
) : RecyclerView.Adapter<RequestsAdapter.RequestViewHolder>() {

    inner class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvRequestName)
        val tvAge: TextView = view.findViewById(R.id.tvRequestAge)
        val ivProfilePic: ImageView = view.findViewById(R.id.ivProfilePic)
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
        holder.tvName.text = request.senderName
        holder.tvAge.text = "${request.senderAge} yrs"

        if (request.senderImage.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(request.senderImage)
                .circleCrop()
                .into(holder.ivProfilePic)
        } else {
            holder.ivProfilePic.setImageResource(R.drawable.ic_avatar1)
        }

        holder.btnAccept.setOnClickListener { onAccept(request) }
        holder.btnDecline.setOnClickListener { onDecline(request) }
    }

    override fun getItemCount(): Int = requests.size

    fun removeRequest(request: Request) {
        val index = requests.indexOfFirst { it.id == request.id }
        if (index != -1) {
            requests.removeAt(index)
            notifyItemRemoved(index)
        }
    }

}

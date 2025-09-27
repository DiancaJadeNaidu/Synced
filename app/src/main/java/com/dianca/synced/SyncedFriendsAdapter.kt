package com.dianca.synced

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SyncedFriendsAdapter(
    private val friends: MutableList<SyncedFriend>,
    private val onFriendClick: (SyncedFriend) -> Unit
) : RecyclerView.Adapter<SyncedFriendsAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvFriendName)
        val ivProfile: ImageView = view.findViewById(R.id.ivFriendProfile)
        val btnOpen: Button = view.findViewById(R.id.btnOpenFriend)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_synced_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.tvName.text = friend.name
        Glide.with(holder.itemView.context)
            .load(friend.profileImage)
            .circleCrop()
            .placeholder(R.drawable.ic_avatar1)
            .into(holder.ivProfile)

        holder.btnOpen.setOnClickListener { onFriendClick(friend) }
    }

    override fun getItemCount(): Int = friends.size
}

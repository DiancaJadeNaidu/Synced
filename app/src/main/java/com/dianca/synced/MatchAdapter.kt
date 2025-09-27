package com.dianca.synced

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dianca.synced.models.MatchModel

class MatchAdapter(
    private val matches: List<MatchModel>,
    private val onClick: (MatchModel) -> Unit
) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    inner class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatarImage: ImageView = view.findViewById(R.id.zodiacIcon) // still your ImageView
        val txtName: TextView = view.findViewById(R.id.nameText)
        val txtBio: TextView = view.findViewById(R.id.bioText)
        val matchPercent: TextView = view.findViewById(R.id.matchPercent)
        val viewProfileButton: Button = view.findViewById(R.id.viewProfileButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match_card, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]

        holder.txtName.text = "${match.name}, ${match.age}"
        holder.txtBio.text = match.bio
        holder.matchPercent.text = "${match.percentage}%\nmatch"




        // Load avatar drawable safely using string name from Firebase
        val context = holder.itemView.context
        val drawableResId = context.resources.getIdentifier(
            match.avatarName,  // e.g., "avatar1", "avatar2"
            "drawable",
            context.packageName
        )
        holder.avatarImage.setImageResource(
            if (drawableResId != 0) drawableResId else R.drawable.default_avatar_foreground
        )

        holder.viewProfileButton.setOnClickListener { onClick(match) }
        holder.itemView.setOnClickListener { onClick(match) }
    }

    override fun getItemCount(): Int = matches.size
}

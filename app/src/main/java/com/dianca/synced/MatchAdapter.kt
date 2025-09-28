package com.dianca.synced

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dianca.synced.models.MatchModel

class MatchAdapter(
    private val matches: List<MatchModel>,
    private val currentUser: MatchModel,
    private val onClick: (MatchModel) -> Unit
) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    // Zodiac compatibility map
    private val zodiacCompatibility = mapOf(
        "Aries" to listOf("Leo", "Sagittarius", "Gemini", "Aquarius"),
        "Taurus" to listOf("Virgo", "Capricorn", "Cancer", "Pisces"),
        "Gemini" to listOf("Libra", "Aquarius", "Aries", "Leo"),
        "Cancer" to listOf("Scorpio", "Pisces", "Taurus", "Virgo"),
        "Leo" to listOf("Aries", "Sagittarius", "Gemini", "Libra"),
        "Virgo" to listOf("Taurus", "Capricorn", "Cancer", "Scorpio"),
        "Libra" to listOf("Gemini", "Aquarius", "Leo", "Sagittarius"),
        "Scorpio" to listOf("Cancer", "Pisces", "Virgo", "Capricorn"),
        "Sagittarius" to listOf("Aries", "Leo", "Libra", "Aquarius"),
        "Capricorn" to listOf("Taurus", "Virgo", "Scorpio", "Pisces"),
        "Aquarius" to listOf("Gemini", "Libra", "Aries", "Sagittarius"),
        "Pisces" to listOf("Cancer", "Scorpio", "Taurus", "Capricorn")
    )

    inner class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatarImage: ImageView = view.findViewById(R.id.zodiacIcon)
        val txtName: TextView = view.findViewById(R.id.nameText)
        val txtBio: TextView = view.findViewById(R.id.bioText)
        val matchPercent: TextView = view.findViewById(R.id.matchPercent)
        val viewProfileButton: Button = view.findViewById(R.id.viewProfileButton)
        val traitsChip: TextView = view.findViewById(R.id.traitsChip) // Now combined button
        val zodiacChip: TextView = view.findViewById(R.id.zodiacChip)
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

        // Avatar
        val context = holder.itemView.context
        val drawableResId = context.resources.getIdentifier(
            match.avatarName,
            "drawable",
            context.packageName
        )
        holder.avatarImage.setImageResource(
            if (drawableResId != 0) drawableResId else R.drawable.default_avatar_foreground
        )

        // Profile click
        holder.viewProfileButton.setOnClickListener { onClick(match) }
        holder.itemView.setOnClickListener { onClick(match) }

        // ------------------ Combined Traits Button ------------------
        holder.traitsChip.setOnClickListener {
            val traits = calculateTraits(currentUser, match)
            showListDialog(holder.itemView, "Traits & Differences", traits)
        }

        // ------------------ Zodiac Compatibility Button ------------------
        holder.zodiacChip.setOnClickListener {
            val (percent, desc) = getZodiacCompatibility(currentUser, match)
            showListDialog(holder.itemView, "Zodiac Compatibility", listOf("${percent}% compatible", desc))
        }
    }

    override fun getItemCount(): Int = matches.size

    // ------------------ Helper Functions ------------------
    private fun calculateTraits(currentUser: MatchModel, match: MatchModel): List<String> {
        val traits = mutableListOf<String>()

        val sharedHobbies = match.hobbies.intersect(currentUser.hobbies.toSet())
        val diffHobbies = match.hobbies.subtract(currentUser.hobbies.toSet())
        if (sharedHobbies.isNotEmpty()) traits.add("Shared Hobbies: ${sharedHobbies.joinToString(", ")}")
        if (diffHobbies.isNotEmpty()) traits.add("Different Hobbies: ${diffHobbies.joinToString(", ")}")

        if (match.food == currentUser.food && match.food.isNotEmpty()) traits.add("Both love ${match.food}")
        else if (match.food.isNotEmpty()) traits.add("${match.name} loves ${match.food}")

        if (match.movieGenre == currentUser.movieGenre && match.movieGenre.isNotEmpty()) traits.add("Both enjoy ${match.movieGenre} movies")
        else if (match.movieGenre.isNotEmpty()) traits.add("${match.name} enjoys ${match.movieGenre} movies")

        if (match.favoriteColor == currentUser.favoriteColor && match.favoriteColor.isNotEmpty()) traits.add("Both like ${match.favoriteColor}")
        else if (match.favoriteColor.isNotEmpty()) traits.add("${match.name}'s favorite color is ${match.favoriteColor}")

        return traits
    }
    private fun getZodiacCompatibility(currentUser: MatchModel, match: MatchModel): Pair<Int, String> {
        val compatibleSigns = zodiacCompatibility[currentUser.zodiacSign] ?: emptyList()
        val description: String
        val advice: String

        val percentage = when {
            compatibleSigns.take(2).contains(match.zodiacSign) -> {
                description = "${match.zodiacSign} is highly compatible with ${currentUser.zodiacSign} 💖"
                advice = when (currentUser.zodiacSign) {
                    "Aries" -> "${match.name} will match your energy and boldness perfectly! Expect spontaneous adventures, laughter, and a fiery connection that keeps life exciting. ⚡🔥"
                    "Taurus" -> "${match.name} brings stability and comfort, making cozy nights and shared goals a joy. You’ll savor life's pleasures together like a perfect duet of hearts. 🌿🍷"
                    "Gemini" -> "${match.name} will keep up with your curiosity and love for variety! Endless conversations, jokes, and spontaneous ideas will keep you both on your toes. 🌀💬"
                    "Cancer" -> "${match.name} understands your emotional depth. Expect nurturing, empathy, and long heartfelt talks. Together, you can create a safe, warm space. 🌊💞"
                    "Leo" -> "${match.name} adores your confidence and joins your fun-loving energy. Expect compliments, playful rivalry, and shared moments in the spotlight. 🦁🌟"
                    "Virgo" -> "${match.name} aligns with your attention to detail and practical mindset. Teamwork flows naturally, and everyday life becomes beautifully organized. 📝🌼"
                    "Libra" -> "${match.name} balances your social nature and love for harmony. Expect romantic gestures, artful dates, and endless charm. 🎨💫"
                    "Scorpio" -> "${match.name} matches your intensity and passion. Expect deep conversations, magnetic attraction, and emotional fireworks! 🔥🦂"
                    "Sagittarius" -> "${match.name} shares your love for adventure! Spontaneous trips, laughter, and thrilling experiences await. 🌍🎒"
                    "Capricorn" -> "${match.name} grounds your ambitions and helps you reach new heights. Together, you’ll build something solid, lasting, and meaningful. 🏔️💼"
                    "Aquarius" -> "${match.name} sparks your originality and imagination! Expect quirky adventures, unique ideas, and endless inspiration. 🌌🤝"
                    "Pisces" -> "${match.name} resonates with your intuition and emotional depth. Expect dreamy moments, creative synergy, and tender connection. 🌊🎨"
                    else -> ""
                }
                95
            }
            compatibleSigns.drop(2).take(2).contains(match.zodiacSign) -> {
                description = "${match.zodiacSign} is moderately compatible with ${currentUser.zodiacSign} 🙂"
                advice = "${match.name} shares some qualities that complement yours. You’ll have fun exploring differences, learning from each other, and creating memorable experiences together. Some sparks may require patience, but potential is definitely there! ✨"
                75
            }
            else -> {
                description = "${match.zodiacSign} has low compatibility with ${currentUser.zodiacSign} ⚡"
                advice = "While ${match.name} might challenge you in unique ways, embracing those differences could lead to unexpected growth, fun, and unforgettable stories. Opposites can attract—get ready for surprises! 🎢🤔"
                50
            }
        }
        return Pair(percentage, "$description\n$advice")
    }


    private fun showListDialog(view: View, title: String, items: List<String>) {
        if (items.isEmpty()) {
            Toast.makeText(view.context, "No $title", Toast.LENGTH_SHORT).show()
            return
        }
        val builder = android.app.AlertDialog.Builder(view.context)
        builder.setTitle(title)
        builder.setItems(items.toTypedArray(), null)
        builder.setPositiveButton("Close", null)
        builder.show()
    }
}

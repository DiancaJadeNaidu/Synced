package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dianca.synced.models.MatchModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class TopMatchesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MatchAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var allMatches = mutableListOf<MatchModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_matches)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recyclerMatches)
        progressBar = findViewById(R.id.progressBar)

        adapter = MatchAdapter(allMatches) { selectedMatch ->
            val intent = Intent(this, ViewProfileActivity::class.java)
            intent.putExtra("uid", selectedMatch.uid)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadMatches()
    }

    private fun loadMatches() {
        progressBar.visibility = View.VISIBLE
        val currentUser = auth.currentUser ?: return

        db.collection("users").get().addOnSuccessListener { result ->
            allMatches.clear()

            for (doc in result) {
                val uid = doc.id
                if (uid == currentUser.uid) continue

                val name = doc.getString("name")?.takeIf { it.isNotBlank() } ?: "Anonymous"
                val birthday = doc.getString("birthday") ?: ""
                val country = doc.getString("country") ?: ""
                val food = doc.getString("food") ?: ""
                val movieGenre = doc.getString("movieGenre") ?: ""
                val favoriteColor = doc.getString("favoriteColor") ?: ""
                val avatarId = doc.getLong("avatarId")?.toInt() ?: R.drawable.default_avatar_foreground
                val hobbies = doc.get("hobbies") as? List<String> ?: emptyList()

                val age = calculateAge(birthday)
                val bio = "Loves $food, enjoys $movieGenre movies, favorite color $favoriteColor"

                val match = MatchModel(
                    uid = uid,
                    name = name,
                    age = age,
                    bio = bio,
                    gender = doc.getString("gender") ?: "N/A",
                    location = country,
                    avatarName = "ic_avatar1"
                )

                allMatches.add(match)
            }

            adapter.notifyDataSetChanged()
            progressBar.visibility = View.GONE
        }.addOnFailureListener {
            Toast.makeText(this, "Error loading matches", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
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
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (e: Exception) {
            0
        }
    }
}

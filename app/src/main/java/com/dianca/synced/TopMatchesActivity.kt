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
import kotlin.math.roundToInt

class TopMatchesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MatchAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var genderSpinner: Spinner
    private lateinit var continentSpinner: Spinner
    private lateinit var countrySpinner: Spinner

    private var allMatches = mutableListOf<MatchModel>()
    private var filteredMatches = mutableListOf<MatchModel>()

    // ----------------- CONTINENT + COUNTRY -----------------
    private val continentCountries = mapOf(
        "Africa" to listOf("South Africa", "Nigeria", "Egypt", "Kenya", "Morocco"),
        "Asia" to listOf("China", "India", "Japan", "South Korea", "Thailand"),
        "Europe" to listOf("UK", "France", "Germany", "Italy", "Spain"),
        "North America" to listOf("USA", "Canada", "Mexico"),
        "South America" to listOf("Brazil", "Argentina", "Chile"),
        "Oceania" to listOf("Australia", "New Zealand", "Fiji")
    )

    private var selectedGender: String = "Both"
    private var selectedContinent: String? = null
    private var selectedCountry: String? = null

    // ----------------- CURRENT USER DATA -----------------
    private var currentUserHobbies: List<String> = emptyList()
    private var currentUserFood: String = ""
    private var currentUserMovieGenre: String = ""
    private var currentUserFavoriteColor: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_matches)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recyclerMatches)
        progressBar = findViewById(R.id.progressBar)
        genderSpinner = findViewById(R.id.spinnerGender)
        continentSpinner = findViewById(R.id.spinnerContinent)
        countrySpinner = findViewById(R.id.spinnerCountry)

        adapter = MatchAdapter(filteredMatches) { selectedMatch ->
            val intent = Intent(this, ViewProfileActivity::class.java)
            intent.putExtra("uid", selectedMatch.uid)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setupFilters()
        loadCurrentUserAndMatches()
    }

    // ----------------- SETUP FILTERS -----------------
    private fun setupFilters() {
        // Gender spinner
        val genders = listOf("Both", "Male", "Female")
        genderSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedGender = genders[position]
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Continent spinner
        val continents = listOf("All") + continentCountries.keys
        continentSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, continents).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        continentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedContinent = if (position == 0) null else continents[position]
                setupCountrySpinner()
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupCountrySpinner() {
        val countries = if (selectedContinent != null) {
            listOf("All") + (continentCountries[selectedContinent] ?: emptyList())
        } else listOf("All")

        countrySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countries).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        countrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCountry = if (position == 0) null else countries[position]
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // ----------------- LOAD USER + MATCHES -----------------
    private fun loadCurrentUserAndMatches() {
        progressBar.visibility = View.VISIBLE
        val currentUser = auth.currentUser ?: return

        db.collection("users").document(currentUser.uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                currentUserHobbies = doc.get("hobbies") as? List<String> ?: emptyList()
                currentUserFood = doc.getString("food") ?: ""
                currentUserMovieGenre = doc.getString("movieGenre") ?: ""
                currentUserFavoriteColor = doc.getString("favoriteColor") ?: ""
            }
            loadMatches()
        }
    }

    private fun loadMatches() {
        val currentUser = auth.currentUser ?: return
        db.collection("users").get().addOnSuccessListener { result ->
            allMatches.clear()

            for (doc in result) {
                val uid = doc.id
                if (uid == currentUser.uid) continue

                val name = doc.getString("name") ?: "Anonymous"
                val birthday = doc.getString("birthday") ?: ""
                val country = doc.getString("country") ?: ""
                val food = doc.getString("food") ?: ""
                val movieGenre = doc.getString("movieGenre") ?: ""
                val favoriteColor = doc.getString("favoriteColor") ?: ""
                val hobbies = doc.get("hobbies") as? List<String> ?: emptyList()

                val rawGender = doc.getString("gender") ?: "N/A"
                val gender = rawGender.replace("♀", "").replace("♂", "").trim()

                val age = calculateAge(birthday)
                val bio = "Loves $food, enjoys $movieGenre movies, favorite color $favoriteColor"

                val percentage = calculateCompatibility(hobbies, food, movieGenre, favoriteColor)

                val match = MatchModel(
                    uid = uid,
                    name = name,
                    age = age,
                    bio = bio,
                    gender = gender,
                    location = country,
                    avatarName = "ic_avatar1",
                    percentage = percentage
                )
                allMatches.add(match)
            }

            applyFilters()
            progressBar.visibility = View.GONE
        }.addOnFailureListener {
            Toast.makeText(this, "Error loading matches", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }
    }

    // ----------------- FILTER + SORT -----------------
    private fun applyFilters() {
        filteredMatches.clear()

        for (match in allMatches) {
            // Gender filter
            if (selectedGender != "Both" && !match.gender.equals(selectedGender, ignoreCase = true)) {
                continue
            }

            // Continent filter
            if (selectedContinent != null) {
                val countries = continentCountries[selectedContinent]
                if (countries != null && !countries.contains(match.location)) {
                    continue
                }
            }

            // Country filter
            if (selectedCountry != null && match.location != selectedCountry) {
                continue
            }

            filteredMatches.add(match)
        }

        // Sort by percentage
        filteredMatches.sortByDescending { it.percentage }
        adapter.notifyDataSetChanged()
    }

    // ----------------- HELPERS -----------------
    private fun calculateCompatibility(hobbies: List<String>, food: String, movieGenre: String, favoriteColor: String): Int {
        var score = 0
        var total = 4 // hobbies, food, movie, color

        // Hobbies overlap
        val sharedHobbies = hobbies.intersect(currentUserHobbies.toSet()).size
        if (currentUserHobbies.isNotEmpty()) {
            score += sharedHobbies
            total += currentUserHobbies.size
        }

        if (food == currentUserFood && food.isNotEmpty()) score++
        if (movieGenre == currentUserMovieGenre && movieGenre.isNotEmpty()) score++
        if (favoriteColor == currentUserFavoriteColor && favoriteColor.isNotEmpty()) score++

        return if (total > 0) ((score.toDouble() / total) * 100).roundToInt() else 0
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

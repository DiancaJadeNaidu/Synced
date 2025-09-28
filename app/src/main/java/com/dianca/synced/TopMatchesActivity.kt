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
    private lateinit var currentUser: MatchModel

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_matches)

        // Buttons
        val btnViewRequests: Button = findViewById(R.id.btnViewRequests)
        val btnViewFriends: Button = findViewById(R.id.btnViewFriends)

        btnViewRequests.setOnClickListener {
            startActivity(Intent(this, SyncRequestsActivity::class.java))
        }

        btnViewFriends.setOnClickListener {
            startActivity(Intent(this, SyncedFriendsActivity::class.java))
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recyclerMatches)
        progressBar = findViewById(R.id.progressBar)
        genderSpinner = findViewById(R.id.spinnerGender)
        continentSpinner = findViewById(R.id.spinnerContinent)
        countrySpinner = findViewById(R.id.spinnerCountry)

        recyclerView.layoutManager = LinearLayoutManager(this)

        setupFilters()
        loadCurrentUserAndMatches()
    }

    private fun setupFilters() {
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

    private fun loadCurrentUserAndMatches() {
        progressBar.visibility = View.VISIBLE
        val firebaseUser = auth.currentUser ?: return

        db.collection("users").document(firebaseUser.uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val intentValue = doc.getString("intent") ?: "Friendship"
                currentUser = MatchModel(
                    uid = firebaseUser.uid,
                    name = doc.getString("name") ?: "Me",
                    age = calculateAge(doc.getString("birthday") ?: ""),
                    bio = "",
                    gender = doc.getString("gender") ?: "",
                    location = doc.getString("country") ?: "",
                    avatarName = "ic_avatar1",
                    percentage = 0,
                    hobbies = doc.get("hobbies") as? List<String> ?: emptyList(),
                    food = doc.getString("food") ?: "",
                    movieGenre = doc.getString("movieGenre") ?: "",
                    favoriteColor = doc.getString("favoriteColor") ?: "",
                    zodiacSign = doc.getString("zodiacSign") ?: "",
                    intent = intentValue
                )
            }
            loadMatches()
        }
    }

    private fun loadMatches() {
        val firebaseUser = auth.currentUser ?: return

        db.collection("users").get().addOnSuccessListener { result ->
            allMatches.clear()
            for (doc in result) {
                val uid = doc.id
                if (uid == firebaseUser.uid) continue

                val matchIntent = doc.getString("intent") ?: "Friendship"
                if (matchIntent != currentUser.intent) continue // <-- intent filter

                val name = doc.getString("name") ?: "Anonymous"
                val birthday = doc.getString("birthday") ?: ""
                val country = doc.getString("country") ?: ""
                val food = doc.getString("food") ?: ""
                val movieGenre = doc.getString("movieGenre") ?: ""
                val favoriteColor = doc.getString("favoriteColor") ?: ""
                val hobbies = doc.get("hobbies") as? List<String> ?: emptyList()
                val zodiacSign = doc.getString("zodiacSign") ?: ""

                val rawGender = doc.getString("gender") ?: "N/A"
                val gender = rawGender.replace("♀", "").replace("♂", "").trim()

                val age = calculateAge(birthday)
                val bio = "Loves $food, enjoys $movieGenre movies, favorite color $favoriteColor"

                val percentage = calculateCompatibility(hobbies, food, movieGenre, favoriteColor, zodiacSign, age)

                val match = MatchModel(
                    uid = uid,
                    name = name,
                    age = age,
                    bio = bio,
                    gender = gender,
                    location = country,
                    avatarName = "ic_avatar1",
                    percentage = percentage,
                    hobbies = hobbies,
                    food = food,
                    movieGenre = movieGenre,
                    favoriteColor = favoriteColor,
                    zodiacSign = zodiacSign,
                    intent = matchIntent
                )
                allMatches.add(match)
            }

            filteredMatches.clear()
            filteredMatches.addAll(allMatches.sortedByDescending { it.percentage })

            adapter = MatchAdapter(filteredMatches, currentUser) { selectedMatch ->
                val intent = Intent(this, ViewProfileActivity::class.java)
                intent.putExtra("uid", selectedMatch.uid)
                startActivity(intent)
            }
            recyclerView.adapter = adapter

            applyFilters()
            progressBar.visibility = View.GONE
        }.addOnFailureListener {
            Toast.makeText(this, "Error loading matches", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }
    }

    private fun applyFilters() {
        if (!::adapter.isInitialized) return

        filteredMatches.clear()
        for (match in allMatches) {
            if (selectedGender != "Both" && !match.gender.equals(selectedGender, true)) continue
            if (selectedContinent != null) {
                val countries = continentCountries[selectedContinent]
                if (countries != null && !countries.contains(match.location)) continue
            }
            if (selectedCountry != null && match.location != selectedCountry) continue
            filteredMatches.add(match)
        }

        filteredMatches.sortByDescending { it.percentage }
        adapter.notifyDataSetChanged()
    }

    private fun calculateCompatibility(
        hobbies: List<String>,
        food: String,
        movieGenre: String,
        favoriteColor: String,
        zodiacSign: String,
        age: Int
    ): Int {
        var totalScore = 0.0
        var totalWeight = 0.0

        // Hobbies: 15%
        val hobbyWeight = 15.0
        val sharedHobbies = hobbies.intersect(currentUser.hobbies.toSet()).size
        val hobbyScore = if (currentUser.hobbies.isNotEmpty())
            (sharedHobbies.toDouble() / currentUser.hobbies.size) * hobbyWeight else 0.0
        totalScore += hobbyScore
        totalWeight += hobbyWeight

        // Food: 5%
        val foodWeight = 5.0
        if (food.isNotEmpty() && food == currentUser.food) totalScore += foodWeight
        totalWeight += foodWeight

        // Movie genre: 5%
        val movieWeight = 5.0
        if (movieGenre.isNotEmpty() && movieGenre == currentUser.movieGenre) totalScore += movieWeight
        totalWeight += movieWeight

        // Favorite color: 5%
        val colorWeight = 5.0
        if (favoriteColor.isNotEmpty() && favoriteColor == currentUser.favoriteColor) totalScore += colorWeight
        totalWeight += colorWeight

        // Zodiac: 35%
        val zodiacWeight = 35.0
        totalScore += getZodiacCompatibility(zodiacSign, currentUser.zodiacSign) * zodiacWeight
        totalWeight += zodiacWeight

        // Age similarity: 15%
        val ageWeight = 15.0
        val ageDiff = kotlin.math.abs(age - currentUser.age)
        val ageScore = when {
            ageDiff == 0 -> ageWeight.toDouble()
            ageDiff <= 2 -> ageWeight * 0.8
            ageDiff <= 5 -> ageWeight * 0.5
            ageDiff <= 10 -> ageWeight * 0.2
            else -> 0.0
        }
        totalScore += ageScore
        totalWeight += ageWeight

        return ((totalScore / totalWeight) * 100).roundToInt()
    }

    private fun getZodiacCompatibility(sign1: String, sign2: String): Double {
        val compatiblePairs = mapOf(
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
        return when {
            sign1 == sign2 -> 1.0
            compatiblePairs[sign1]?.contains(sign2) == true -> 0.8
            else -> 0.3
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
}

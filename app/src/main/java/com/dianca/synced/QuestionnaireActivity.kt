package com.dianca.synced

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.*

class QuestionnaireActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var birthdayInput: EditText
    private lateinit var zodiacSpinner: Spinner
    private lateinit var siblingSpinner: Spinner
    private lateinit var continentSpinner: Spinner
    private lateinit var countrySpinner: Spinner
    private lateinit var movieSpinner: Spinner
    private lateinit var religionSpinner: Spinner
    private lateinit var foodSpinner: Spinner
    private lateinit var outgoingSeekBar: SeekBar
    private lateinit var petRadioGroup: RadioGroup

    private lateinit var fitnessCheck: CheckBox
    private lateinit var musicCheck: CheckBox
    private lateinit var artCheck: CheckBox
    private lateinit var cookingCheck: CheckBox
    private lateinit var travelCheck: CheckBox
    private lateinit var gamingCheck: CheckBox
    private lateinit var readingCheck: CheckBox
    private lateinit var sportsCheck: CheckBox
    private lateinit var dancingCheck: CheckBox
    private lateinit var photographyCheck: CheckBox
    private lateinit var otherCheck: CheckBox

    private lateinit var completeProfileButton: Button
    private lateinit var redButton: Button
    private lateinit var greenButton: Button
    private lateinit var orangeButton: Button
    private lateinit var blueButton: Button

    private var selectedColor: String? = null

    // Continent → Countries map
    private val continentCountries = mapOf(
        "Africa" to listOf("South Africa", "Nigeria", "Egypt", "Kenya", "Morocco"),
        "Asia" to listOf("China", "India", "Japan", "South Korea", "Thailand"),
        "Europe" to listOf("UK", "France", "Germany", "Italy", "Spain"),
        "North America" to listOf("USA", "Canada", "Mexico"),
        "South America" to listOf("Brazil", "Argentina", "Chile"),
        "Oceania" to listOf("Australia", "New Zealand", "Fiji")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionnaire)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Bind views
        birthdayInput = findViewById(R.id.birthdayInput)
        zodiacSpinner = findViewById(R.id.zodiacSpinner)
        siblingSpinner = findViewById(R.id.siblingSpinner)
        continentSpinner = findViewById(R.id.continentSpinner)
        countrySpinner = findViewById(R.id.countrySpinner)
        movieSpinner = findViewById(R.id.movieSpinner)
        religionSpinner = findViewById(R.id.religionSpinner)
        foodSpinner = findViewById(R.id.foodSpinner)
        outgoingSeekBar = findViewById(R.id.outgoingSeekBar)
        petRadioGroup = findViewById(R.id.petRadioGroup)

        fitnessCheck = findViewById(R.id.fitnessCheck)
        musicCheck = findViewById(R.id.musicCheck)
        artCheck = findViewById(R.id.artCheck)
        cookingCheck = findViewById(R.id.cookingCheck)
        travelCheck = findViewById(R.id.travelCheck)
        gamingCheck = findViewById(R.id.gamingCheck)
        readingCheck = findViewById(R.id.readingCheck)
        sportsCheck = findViewById(R.id.sportsCheck)
        dancingCheck = findViewById(R.id.dancingCheck)
        photographyCheck = findViewById(R.id.photographyCheck)
        otherCheck = findViewById(R.id.otherCheck)

        completeProfileButton = findViewById(R.id.completeProfileButton)
        redButton = findViewById(R.id.redButton)
        greenButton = findViewById(R.id.greenButton)
        orangeButton = findViewById(R.id.orangeButton)
        blueButton = findViewById(R.id.blueButton)

        // Birthday picker
        birthdayInput.setOnClickListener {
            val c = Calendar.getInstance()
            val dpd = DatePickerDialog(this, { _, y, m, d ->
                birthdayInput.setText(String.format("%02d/%02d/%04d", m + 1, d, y))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            dpd.show()
        }

        // Populate spinners
        zodiacSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item,
            listOf("Aries","Taurus","Gemini","Cancer","Leo","Virgo","Libra","Scorpio","Sagittarius","Capricorn","Aquarius","Pisces")
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        siblingSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item,
            listOf("0","1","2","3","4+")
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        continentSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item,
            continentCountries.keys.toList()
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Continent → country dynamic
        continentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long
            ) {
                val continent = continentSpinner.selectedItem?.toString() ?: return
                val countries = continentCountries[continent] ?: emptyList()
                countrySpinner.adapter = ArrayAdapter(
                    this@QuestionnaireActivity,
                    android.R.layout.simple_spinner_item,
                    countries
                ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        movieSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item,
            listOf("Action","Comedy","Drama","Horror","Romance","Sci-Fi")
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        religionSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item,
            listOf("Christianity","Islam","Hinduism","Buddhism","None","Other")
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        foodSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item,
            listOf("Pizza","Burgers","Sushi","Pasta","Salad","Other")
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Color buttons
        listOf(redButton, greenButton, orangeButton, blueButton).forEach { btn ->
            btn.setOnClickListener {
                selectedColor = btn.contentDescription.toString()
                Toast.makeText(this, "Selected color: $selectedColor", Toast.LENGTH_SHORT).show()
            }
        }

        // Load saved data
        loadProfile()

        // Save button
        completeProfileButton.setOnClickListener { saveProfile() }
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return
        val user = auth.currentUser

        val selectedPetId = petRadioGroup.checkedRadioButtonId
        val selectedPet = if (selectedPetId != -1) findViewById<RadioButton>(selectedPetId).text.toString() else null

        val hobbies = mutableListOf<String>()
        if (fitnessCheck.isChecked) hobbies.add("Fitness")
        if (musicCheck.isChecked) hobbies.add("Music")
        if (artCheck.isChecked) hobbies.add("Art")
        if (cookingCheck.isChecked) hobbies.add("Cooking")
        if (travelCheck.isChecked) hobbies.add("Travel")
        if (gamingCheck.isChecked) hobbies.add("Gaming")
        if (readingCheck.isChecked) hobbies.add("Reading")
        if (sportsCheck.isChecked) hobbies.add("Sports")
        if (dancingCheck.isChecked) hobbies.add("Dancing")
        if (photographyCheck.isChecked) hobbies.add("Photography")
        if (otherCheck.isChecked) hobbies.add("Other")

        val userData = hashMapOf(
            "name" to (user?.displayName ?: ""),
            "email" to (user?.email ?: ""),
            "birthday" to birthdayInput.text.toString(),
            "zodiacSign" to zodiacSpinner.selectedItem.toString(),
            "siblings" to siblingSpinner.selectedItem.toString(),
            "continent" to continentSpinner.selectedItem.toString(),
            "country" to countrySpinner.selectedItem.toString(),
            "movieGenre" to movieSpinner.selectedItem.toString(),
            "religion" to religionSpinner.selectedItem.toString(),
            "food" to foodSpinner.selectedItem.toString(),
            "outgoingLevel" to outgoingSeekBar.progress,
            "petPreference" to selectedPet,
            "hobbies" to hobbies,
            "favoriteColor" to selectedColor
        )

        db.collection("users").document(uid)
            .set(userData, SetOptions.merge()) // MERGE to preserve existing fields
            .addOnSuccessListener {
                Toast.makeText(this, "Profile completed!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // nameTextView.text = doc.getString("name")
                    // emailTextView.text = doc.getString("email")
                    birthdayInput.setText(doc.getString("birthday"))
                    zodiacSpinner.setSelection(
                        (zodiacSpinner.adapter as ArrayAdapter<String>).getPosition(doc.getString("zodiacSign") ?: "Aries")
                    )
                    siblingSpinner.setSelection(
                        (siblingSpinner.adapter as ArrayAdapter<String>).getPosition(doc.getString("siblings") ?: "0")
                    )

                    val savedContinent = doc.getString("continent")
                    if (!savedContinent.isNullOrEmpty()) {
                        val continentPos = (continentSpinner.adapter as ArrayAdapter<String>).getPosition(savedContinent)
                        if (continentPos >= 0) continentSpinner.setSelection(continentPos)
                    }

                    // Populate countries based on continent
                    val selectedContinent = continentSpinner.selectedItem?.toString()
                    val countries = continentCountries[selectedContinent] ?: emptyList()
                    countrySpinner.adapter = ArrayAdapter(
                        this@QuestionnaireActivity,
                        android.R.layout.simple_spinner_item,
                        countries
                    ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                    val savedCountry = doc.getString("country")
                    if (!savedCountry.isNullOrEmpty() && countries.contains(savedCountry)) {
                        countrySpinner.setSelection(countries.indexOf(savedCountry))
                    }

                    movieSpinner.setSelection(
                        (movieSpinner.adapter as ArrayAdapter<String>).getPosition(doc.getString("movieGenre") ?: "Action")
                    )
                    religionSpinner.setSelection(
                        (religionSpinner.adapter as ArrayAdapter<String>).getPosition(doc.getString("religion") ?: "None")
                    )
                    foodSpinner.setSelection(
                        (foodSpinner.adapter as ArrayAdapter<String>).getPosition(doc.getString("food") ?: "Pizza")
                    )
                    outgoingSeekBar.progress = doc.getLong("outgoingLevel")?.toInt() ?: 0

                    selectedColor = doc.getString("favoriteColor")
                }
            }
    }
}

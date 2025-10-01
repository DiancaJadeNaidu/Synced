package com.dianca.synced

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class QuestionnaireActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var genderRadioGroup: RadioGroup
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

    private var editMode: Boolean = false

    private var selectedColor: String? = null
    private var selectedAvatarId: Int = R.drawable.default_avatar_foreground

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
        editMode = intent.getBooleanExtra("EXTRA_EDIT_MODE", false)

        bindViews()
        setupSpinners()
        setupBirthdayPicker()
        setupColorButtons()
        loadProfile()

        completeProfileButton.setOnClickListener {
            if (isProfileComplete()) saveProfile()
        }
    }

    private fun bindViews() {
        genderRadioGroup = findViewById(R.id.genderRadioGroup)
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
    }

    private fun setupSpinners() {
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

        continentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
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
    }

    private fun setupBirthdayPicker() {
        birthdayInput.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                birthdayInput.setText(String.format("%02d/%02d/%04d", m + 1, d, y))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupColorButtons() {
        listOf(redButton, greenButton, orangeButton, blueButton).forEach { btn ->
            btn.setOnClickListener {
                selectedColor = btn.contentDescription.toString()
                selectedAvatarId = when(btn.id){
                    R.id.redButton -> R.drawable.rounded_button_red
                    R.id.greenButton -> R.drawable.rounded_button_green
                    R.id.orangeButton -> R.drawable.chip_negative_bg
                    R.id.blueButton -> R.drawable.rounded_button_blue
                    else -> R.drawable.default_avatar_foreground
                }
                Toast.makeText(this, "Selected color: $selectedColor", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun collectHobbies(): List<String> {
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
        return hobbies
    }

    private fun selectedGenderIsEmpty(): Boolean {
        return genderRadioGroup.checkedRadioButtonId == -1
    }

    private fun calculateAge(birthdayStr: String): Int {
        return try {
            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            val birthDate = sdf.parse(birthdayStr) ?: return 0
            val today = Calendar.getInstance()
            val birthCal = Calendar.getInstance().apply { time = birthDate }
            var age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) age--
            age
        } catch (e: Exception) { 0 }
    }

    private fun isProfileComplete(): Boolean {
        val birthdayStr = birthdayInput.text.toString().trim()
        val age = calculateAge(birthdayStr)

        if (selectedGenderIsEmpty()) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
            return false
        }
        if (birthdayStr.isEmpty()) {
            Toast.makeText(this, "Please enter your birthday", Toast.LENGTH_SHORT).show()
            return false
        }
        if (age < 18) {
            Toast.makeText(this, "You must be at least 18 years old to complete your profile", Toast.LENGTH_LONG).show()
            return false
        }
        if (zodiacSpinner.selectedItem == null ||
            siblingSpinner.selectedItem == null ||
            continentSpinner.selectedItem == null ||
            countrySpinner.selectedItem == null ||
            movieSpinner.selectedItem == null ||
            religionSpinner.selectedItem == null ||
            foodSpinner.selectedItem == null ||
            outgoingSeekBar.progress == 0 ||
            petRadioGroup.checkedRadioButtonId == -1
        ) {
            Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show()
            return false
        }
        if (collectHobbies().isEmpty()) {
            Toast.makeText(this, "Please select at least one hobby", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedColor == null) {
            Toast.makeText(this, "Please select a favorite color", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return
        val user = auth.currentUser

        val selectedGenderId = genderRadioGroup.checkedRadioButtonId
        val selectedGender = if (selectedGenderId != -1) findViewById<RadioButton>(selectedGenderId).text.toString() else null

        val selectedPetId = petRadioGroup.checkedRadioButtonId
        val selectedPet = if (selectedPetId != -1) findViewById<RadioButton>(selectedPetId).text.toString() else null

        val birthdayStr = birthdayInput.text.toString()
        val age = calculateAge(birthdayStr)

        val userData = hashMapOf(
            "email" to (user?.email ?: ""),
            "birthday" to birthdayStr,
            "age" to age,
            "zodiacSign" to zodiacSpinner.selectedItem.toString(),
            "siblings" to siblingSpinner.selectedItem.toString(),
            "continent" to continentSpinner.selectedItem.toString(),
            "country" to countrySpinner.selectedItem.toString(),
            "movieGenre" to movieSpinner.selectedItem.toString(),
            "religion" to religionSpinner.selectedItem.toString(),
            "food" to foodSpinner.selectedItem.toString(),
            "outgoingLevel" to outgoingSeekBar.progress,
            "petPreference" to selectedPet,
            "hobbies" to collectHobbies(),
            "favoriteColor" to selectedColor,
            "gender" to selectedGender
        )

        db.collection("users").document(uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()
                val nextActivity = if (editMode) ProfileActivity::class.java else ChoosingIntentActivity::class.java
                startActivity(Intent(this, nextActivity))
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
                    selectedAvatarId = doc.getLong("avatarId")?.toInt() ?: R.drawable.default_avatar_foreground
                }
            }
    }
}

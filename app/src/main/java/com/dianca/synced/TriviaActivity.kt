package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TriviaActivity : AppCompatActivity() {

    private lateinit var txtQuestion: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnSubmit: Button
    private lateinit var btnNext: Button
    private lateinit var txtFeedback: TextView
    private lateinit var txtYourScore: TextView
    private lateinit var txtFriendScore: TextView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    private var currentQuestionIndex = 0
    private var score = 0
    private var friendScore = 0

    private lateinit var userId: String
    private lateinit var friendId: String

    private val questions = listOf(
        TriviaQuestion("What is the capital of South Africa?", listOf("Cape Town", "Pretoria", "Johannesburg", "Durban"), 1),
        TriviaQuestion("Which planet is known as the Red Planet?", listOf("Earth", "Venus", "Mars", "Jupiter"), 2),
        TriviaQuestion("Who wrote 'Romeo and Juliet'?", listOf("Shakespeare", "Homer", "Dickens", "Tolstoy"), 0),
        TriviaQuestion("What is the largest ocean?", listOf("Atlantic", "Indian", "Pacific", "Arctic"), 2)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trivia)


        // Initialize views
        txtQuestion = findViewById(R.id.txtQuestion)
        radioGroup = findViewById(R.id.radioGroup)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnNext = findViewById(R.id.btnNext)
        txtFeedback = findViewById(R.id.txtFeedback)
        txtYourScore = findViewById(R.id.txtYourScore)
        txtFriendScore = findViewById(R.id.txtFriendScore)
        bottomNav = findViewById(R.id.bottomNav)

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        friendId = intent.getStringExtra("friendId") ?: "friend123"

        loadQuestion()
        fetchScores()

        btnSubmit.setOnClickListener { submitAnswer() }
        btnNext.setOnClickListener { nextQuestion() }

        // Bottom navigation listener
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, MainActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); true }
                R.id.nav_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
                R.id.nav_help -> { startActivity(Intent(this, HelpActivity::class.java)); true }
                else -> false
            }
        }
    }

    private fun loadQuestion() {
        val q = questions[currentQuestionIndex]
        txtQuestion.text = q.text
        radioGroup.removeAllViews()
        txtFeedback.text = ""
        btnNext.visibility = Button.GONE

        for (option in q.options) {
            val rb = RadioButton(this)
            rb.text = option
            radioGroup.addView(rb)
        }
    }

    private fun submitAnswer() {
        val selectedId = radioGroup.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, "Select an answer", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedIndex = radioGroup.indexOfChild(findViewById(selectedId))
        val correct = questions[currentQuestionIndex].correctAnswer

        if (selectedIndex == correct) {
            score += 10
            txtFeedback.text = "✅ Correct! +10 points"
        } else {
            txtFeedback.text = "❌ Wrong! Correct answer: ${questions[currentQuestionIndex].options[correct]}"
        }

        txtYourScore.text = "Your Score: $score"
        saveScore(score)

        btnSubmit.visibility = Button.GONE
        btnNext.visibility = Button.VISIBLE
    }

    private fun nextQuestion() {
        currentQuestionIndex++
        if (currentQuestionIndex < questions.size) {
            loadQuestion()
            btnSubmit.visibility = Button.VISIBLE
            btnNext.visibility = Button.GONE
        } else {
            txtQuestion.text = "🎉 Quiz complete! Final score: $score"
            radioGroup.removeAllViews()
            btnSubmit.visibility = Button.GONE
            btnNext.visibility = Button.GONE
        }
        fetchScores()
    }

    private fun saveScore(points: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            ScoreManager.saveScore(userId, friendId, "Trivia", points)
        }
    }

    private fun fetchScores() {
        ScoreManager.fetchFriendScores(friendId) { scores ->
            friendScore = scores.filter { it.userId == friendId }.sumOf { it.points }
            runOnUiThread {
                txtFriendScore.text = "Friend Score: $friendScore"
                txtYourScore.text = "Your Score: $score"
            }
        }
    }
}

data class TriviaQuestion(
    val text: String,
    val options: List<String>,
    val correctAnswer: Int
)

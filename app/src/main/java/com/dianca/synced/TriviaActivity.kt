package com.dianca.synced

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.dianca.synced.R
import com.dianca.synced.ScoreManager
import com.google.firebase.auth.FirebaseAuth

class TriviaActivity : AppCompatActivity() {

    private lateinit var txtQuestion: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnSubmit: Button
    private lateinit var txtScore: TextView

    private var currentQuestionIndex = 0
    private var score = 0

    private val questions = listOf(
        TriviaQuestion(
            "What is the capital of South Africa?",
            listOf("Cape Town", "Pretoria", "Johannesburg", "Durban"),
            1
        ),
        TriviaQuestion(
            "Which planet is known as the Red Planet?",
            listOf("Earth", "Venus", "Mars", "Jupiter"),
            2
        ),
        TriviaQuestion(
            "Who wrote 'Romeo and Juliet'?",
            listOf("Shakespeare", "Homer", "Dickens", "Tolstoy"),
            0
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trivia)

        txtQuestion = findViewById(R.id.txtQuestion)
        radioGroup = findViewById(R.id.radioGroup)
        btnSubmit = findViewById(R.id.btnSubmit)
        txtScore = findViewById(R.id.txtScore)

        loadQuestion()

        btnSubmit.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(this, "Select an answer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedIndex = radioGroup.indexOfChild(findViewById(selectedId))
            if (selectedIndex == questions[currentQuestionIndex].correctAnswer) {
                score += 10
            }

            currentQuestionIndex++

            if (currentQuestionIndex < questions.size) {
                loadQuestion()
            } else {
                endQuiz()
            }
        }
    }

    private fun loadQuestion() {
        val question = questions[currentQuestionIndex]
        txtQuestion.text = question.text
        radioGroup.removeAllViews()

        for (option in question.options) {
            val radioButton = RadioButton(this)
            radioButton.text = option
            radioGroup.addView(radioButton)
        }

        txtScore.text = "Score: $score"
    }

    private fun endQuiz() {
        txtQuestion.text = "🎉 Quiz complete! Final score: $score"
        radioGroup.removeAllViews()
        btnSubmit.isEnabled = false
        saveScore(score)
    }

    private fun saveScore(points: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        ScoreManager.saveScore(userId, "friendId123", "Trivia", points)
    }
}

data class TriviaQuestion(
    val text: String,
    val options: List<String>,
    val correctAnswer: Int
)

package com.dianca.synced

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding

class HelpActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var quickHelp1: TextView
    private lateinit var quickHelp2: TextView
    private lateinit var quickHelp3: TextView
    private lateinit var quickHelp4: TextView
    private lateinit var quickHelpContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        inputEditText = findViewById(R.id.inputEditText)
        sendButton = findViewById(R.id.sendButton)
        quickHelp1 = findViewById(R.id.quickHelp1)
        quickHelp2 = findViewById(R.id.quickHelp2)
        quickHelp3 = findViewById(R.id.quickHelp3)
        quickHelp4 = findViewById(R.id.quickHelp4)
        quickHelpContainer = findViewById(R.id.quickHelpContainer)

        // Quick help taps → simulate typing that question
        quickHelp1.setOnClickListener { showAnswer("intent") }
        quickHelp2.setOnClickListener { showAnswer("matching") }
        quickHelp3.setOnClickListener { showAnswer("report") }
        quickHelp4.setOnClickListener { showAnswer("privacy") }

        // Send button → analyze user input
        sendButton.setOnClickListener {
            val query = inputEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                showAnswer(query)
                inputEditText.text.clear()
            }
        }
    }

    private fun showAnswer(query: String) {
        val lowerQuery = query.lowercase()
        val answer = when {
            "intent" in lowerQuery -> "To change your intent, go to your profile settings and update the intent field."
            "matching" in lowerQuery -> "Matching works by comparing your answers and preferences with others nearby."
            "report" in lowerQuery -> "To report someone, go to their profile → tap the menu → select 'Report User'."
            "privacy" in lowerQuery -> "Keep your data safe: never share personal details. You can control visibility in your privacy settings."
            "game" in lowerQuery -> "You can play games with friends under the 'Play Games' section — Trivia, Memory, Number Guess, and Tic Tac Toe."
            else -> "I couldn’t find a specific answer for that. Please check settings or contact support."
        }

        // Add answer to quickHelpContainer dynamically
        val responseView = TextView(this).apply {
            text = "🤖 $answer"
            textSize = 16f
            setPadding(16)
        }
        quickHelpContainer.addView(responseView)
    }
}

package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class RulesActivity : AppCompatActivity() {

    private lateinit var checkAgree: CheckBox
    private lateinit var btnContinue: Button
    private var fromSettings: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rules)

        checkAgree = findViewById(R.id.checkAgree)
        btnContinue = findViewById(R.id.btnContinue)

        // Detect flow
        fromSettings = intent.getBooleanExtra("fromSettings", false)

        // Initial checkbox color
        checkAgree.buttonTintList =
            ContextCompat.getColorStateList(this, android.R.color.darker_gray)

        checkAgree.setOnCheckedChangeListener { _, isChecked ->
            val color = if (isChecked) R.color.purple_700 else android.R.color.darker_gray
            checkAgree.buttonTintList = ContextCompat.getColorStateList(this, color)
        }

        btnContinue.setOnClickListener {
            if (!checkAgree.isChecked) {
                // Shake animation
                val shake = TranslateAnimation(0f, 25f, 0f, 0f)
                shake.duration = 300
                shake.repeatMode = Animation.REVERSE
                shake.repeatCount = 3
                checkAgree.startAnimation(shake)

                // Highlight checkbox in red temporarily
                checkAgree.buttonTintList =
                    ContextCompat.getColorStateList(this, android.R.color.holo_red_dark)

                // Temporarily tick checkbox
                checkAgree.isChecked = true
                checkAgree.postDelayed({
                    checkAgree.isChecked = false
                    checkAgree.buttonTintList =
                        ContextCompat.getColorStateList(this, android.R.color.darker_gray)
                }, 500)

                Toast.makeText(
                    this,
                    "You must accept the terms to continue",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (fromSettings) {
                    // Go back to Settings
                    finish()
                } else {
                    // Normal app flow → go to Login
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }
}

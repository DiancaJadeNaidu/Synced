package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.dianca.synced.R  // correct R import

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_home) // correct XML file name

        val btnSync: Button = findViewById(R.id.btnSync)
        btnSync.setOnClickListener {
            val intent = Intent(this, RulesActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnAllow: Button = findViewById(R.id.btnAllow)
        val btnNotNow: Button = findViewById(R.id.btnNotNow)

        btnAllow.setOnClickListener { goToRules() }
        btnNotNow.setOnClickListener { goToRules() }
    }

    private fun goToRules() {
        val intent = Intent(this, RulesActivity::class.java)
        startActivity(intent)
        finish()
    }
}

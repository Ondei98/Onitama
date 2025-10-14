package com.project.onitama

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var buttonPlayHuman: Button
    private lateinit var buttonPlayComputer: Button
    private lateinit var buttonSettings: Button

    companion object { var isVsComputer = false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonPlayHuman = findViewById(R.id.buttonPlayHuman)
        buttonPlayComputer = findViewById(R.id.buttonPlayComputer)
        buttonSettings = findViewById(R.id.buttonSettings)

        buttonPlayHuman.setOnClickListener {
            isVsComputer = false
            val intent = Intent(this, GameBoard::class.java)
            startActivity(intent)
        }

        buttonPlayComputer.setOnClickListener {
            isVsComputer = true
            val intent = Intent(this, GameBoard::class.java)
            startActivity(intent)
        }

        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
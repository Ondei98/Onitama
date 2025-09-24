package com.project.onitama

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var playButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playButton = findViewById(R.id.buttonPlay)

        playButton.setOnClickListener {
            val intent = Intent(this, GameBoard::class.java)
            startActivity(intent)
        }
    }
}
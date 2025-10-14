package com.project.onitama

import android.os.Bundle
import android.widget.Button
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class SettingsActivity : AppCompatActivity() {

    private lateinit var difficultyPicker: NumberPicker
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        difficultyPicker = findViewById(R.id.difficulty_picker)
        saveButton = findViewById(R.id.save_button)

        // Setup NumberPicker with custom ranks
        val ranks = resources.getStringArray(R.array.ninja_ranks)
        difficultyPicker.minValue = 0
        difficultyPicker.maxValue = ranks.size - 1
        difficultyPicker.displayedValues = ranks
        difficultyPicker.wrapSelectorWheel = false

        // Load saved preference
        val sharedPreferences = getSharedPreferences("OnitamaSettings", MODE_PRIVATE)
        val savedDifficulty = sharedPreferences.getInt("AIDepth", 2)
        difficultyPicker.value = savedDifficulty - 1 // Convert depth to index

        saveButton.setOnClickListener {
            val selectedDifficulty = difficultyPicker.value + 1
            sharedPreferences.edit {
                putInt("AIDepth", selectedDifficulty)
            }
            finish()
        }
    }

}

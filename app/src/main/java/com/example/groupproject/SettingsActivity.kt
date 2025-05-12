package com.example.groupproject

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val spinner: Spinner = findViewById(R.id.seasonSpinner)
        val seekBar: SeekBar = findViewById(R.id.timerSeekBar)
        val timerText: TextView = findViewById(R.id.timerValueText)
        val applyButton: Button = findViewById(R.id.applyButton)


        // prior settings

        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val selectedSeason = prefs.getString("season", "2024-2025") ?: "2024-2025"
        val timer = prefs.getInt("timerSeconds", 8)
        val playBackgroundMusic = prefs.getBoolean("backgroundSound", true)
        val playStartupSound = prefs.getBoolean("startUpSound", true)

        // Season options
        val seasons = listOf("2021", "2022", "2023", "2024", "2025")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, seasons)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // last season used, value is the index from list of seasons
        val index = when (selectedSeason) {
            "2020-2021" -> 0
            "2021-2022" -> 1
            "2022-2023" -> 2
            "2023-2024" -> 3
            else -> 4
        }

        spinner.setSelection(index)

        // Default timer value
        var selectedTimerSeconds = timer
        timerText.text = "Timer: $selectedTimerSeconds seconds"

        // SeekBar config: 0 → 3 sec, 12 → 15 sec
        seekBar.max = 12
        seekBar.progress = timer - 3 // 5 + 3 = 8 sec default

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedTimerSeconds = progress + 3
                timerText.text = "Timer: $selectedTimerSeconds seconds"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val soundSwitch: Switch = findViewById(R.id.startupSoundSwitch)
        var startupSoundEnabled = playStartupSound // default ON

        // checks based on prior settings
        soundSwitch.isChecked = startupSoundEnabled

        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            startupSoundEnabled = isChecked
        }

        val bgMusicSwitch: Switch = findViewById(R.id.backgroundMusicSwitch)
        var bgMusicEnabled = playBackgroundMusic

        // checks based on prior settings
        bgMusicSwitch.isChecked = bgMusicEnabled

        bgMusicSwitch.setOnCheckedChangeListener { _, isChecked ->
            bgMusicEnabled = isChecked
        }

        applyButton.setOnClickListener {
            val selectedSeason = spinner.selectedItem.toString()

            // ADDED: Gets stored app settings
            val pref = getSharedPreferences("app_settings", MODE_PRIVATE)

            val season = when (selectedSeason) {
                "2021" -> "2020-2021"
                "2022" -> "2021-2022"
                "2023" -> "2022-2023"
                "2024" -> "2023-2024"
                "2025" -> "2024-2025"
                else -> "2024-2025"
            }

            // ADDED: Updates any settings changed
            pref.edit().putString("season", season).apply()
            pref.edit().putInt("timerSeconds", selectedTimerSeconds).apply()
            pref.edit().putBoolean("startUpSound", startupSoundEnabled).apply()
            pref.edit().putBoolean("backgroundSound", bgMusicEnabled).apply()

            Toast.makeText(this, "Selected season: $season", Toast.LENGTH_SHORT).show()

            finish()
        }


    }
}

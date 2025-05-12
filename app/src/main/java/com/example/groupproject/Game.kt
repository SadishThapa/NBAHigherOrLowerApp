package com.example.groupproject

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import kotlin.random.Random

class Game {
    private lateinit var season : String

    private lateinit var playerStats : JSONArray

    // JSON objects which have the data for the current top and bottom players displayed
    private lateinit var topPlayer : JSONObject
    private lateinit var bottomPlayer : JSONObject

    private lateinit var bar : ProgressBar

    private var score = 0

    private lateinit var username: String  // leaderboard
    private lateinit var userState: String


    // Seconds allowed before user runs out of time. No time limit of zero.
    private var seconds = 0

    // Which NBA season to get data from (default is 2024-2025)
    private var statsResourceId = R.raw.stats2025

    // Which stat (points, rebounds, or assists) is being quizzed
    private var currentStat = "Points"
    // Key to access stat in JSON object
    private var statKey = "ppg"


    // If blank, the game rotates the quizzed stat. Otherwise, the questions are locked at this stat
    private var lock : String? = ""

    constructor(resources: Resources, newBar : ProgressBar, newSeconds : Int, newSeason : String, newLock : String?) {
        bar = newBar
        seconds = newSeconds
        season = newSeason
        lock = newLock

        if (lock == "Points") {
            currentStat = "Points"
            statKey = "ppg"
        }
        else if (lock == "Rebounds") {
            currentStat = "Rebounds"
            statKey = "ppg"
        }

        else {
            currentStat = "Assists"
            statKey = "apg"
        }

        newStat()

        if (season == "2020-2021") {
            statsResourceId = R.raw.stats2021
        }
        else if (season == "2021-2022") {
            statsResourceId = R.raw.stats2022
        }
        else if (season == "2022-2023") {
            statsResourceId = R.raw.stats2023
        }
        else if (season == "2023-2024") {
            statsResourceId = R.raw.stats2024
        }

        // Read out stats JSON file into string, then parse into JSONArray
        var iStream: InputStream = resources.openRawResource(statsResourceId)
        var JsonString : String = iStream.bufferedReader().use { it.readText() }

        //Log.w("Game", JsonString)

        playerStats = JSONArray(JsonString)

        topPlayer = playerStats.getJSONObject(Random.nextInt(playerStats.length()))
        bottomPlayer = playerStats.getJSONObject(Random.nextInt(playerStats.length()))

        // Make sure we don't have the same players top and bottom
        while (topPlayer.getDouble(statKey) == bottomPlayer.getDouble(statKey)) {
            bottomPlayer = playerStats.getJSONObject(Random.nextInt(playerStats.length()))
        }

        Log.w("Game", topPlayer.getString("name"))
        Log.w("Game", bottomPlayer.getString("name"))

        Log.w("Game", season)

        username = MainActivity.username
        userState = MainActivity.userState
    }

    // Check if an answer was correct based on which button was clicked
    fun checkCorrectness(which : String) : Boolean {
        var topPlayerStat : Double = topPlayer.getDouble(statKey)
        var bottomPlayerStat : Double = bottomPlayer.getDouble(statKey)

        if (which == "higher") {
            return bottomPlayerStat > topPlayerStat
        }

        else {
            return bottomPlayerStat < topPlayerStat
        }
    }

    // Randomly select a new stat to quiz unless the stat is locked
    fun newStat() {
        if (lock == null) {
            var nextStat : Int = Random.nextInt(3)

            if (nextStat == 0) {
                currentStat = "Points"
                statKey = "ppg"
            }
            else if (nextStat == 1) {
                currentStat = "Rebounds"
                statKey = "rpg"
            }

            else {
                currentStat = "Assists"
                statKey = "apg"
            }
        }
    }

    fun incrementScore() {
        score = score + 1
    }

    fun nextRound() {
        newStat()

        topPlayer = bottomPlayer
        bottomPlayer = playerStats.getJSONObject(Random.nextInt(playerStats.length()))

        // Make sure we don't have the same players top and bottom
        while (topPlayer.getDouble(statKey) == bottomPlayer.getDouble(statKey)) {
            bottomPlayer = playerStats.getJSONObject(Random.nextInt(playerStats.length()))
        }
    }

    fun getCurrentStat() : String {
        return currentStat
    }

    fun getScore() : Int {
        return score
    }

    fun getSeason() : String {
        return season
    }

    fun getSeconds() : Int {
        return seconds
    }

    fun getStatKey() : String {
        return statKey
    }


    fun getTopPlayer() : JSONObject {
        return topPlayer
    }

    fun getBottomPlayer() : JSONObject {
        return bottomPlayer
    }

    fun getUsername(): String {
        return username
    }

    fun getUserState(): String {
        return userState
    }
}
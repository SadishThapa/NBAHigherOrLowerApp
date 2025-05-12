package com.example.groupproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var player1TV: TextView
    private lateinit var player2TV: TextView
    private lateinit var player3TV: TextView
    private lateinit var player4TV: TextView
    private lateinit var player5TV: TextView
    private lateinit var score1TV: TextView
    private lateinit var score2TV: TextView
    private lateinit var score3TV: TextView
    private lateinit var score4TV: TextView
    private lateinit var score5TV: TextView
    private lateinit var state1TV: TextView
    private lateinit var state2TV: TextView
    private lateinit var state3TV: TextView
    private lateinit var state4TV: TextView
    private lateinit var state5TV: TextView

    private lateinit var homeButton : Button
    private lateinit var play : Button
    private lateinit var settings : Button

    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        player1TV = findViewById(R.id.leaderboard_player_1)
        player2TV = findViewById(R.id.leaderboard_player_2)
        player3TV = findViewById(R.id.leaderboard_player_3)
        player4TV = findViewById(R.id.leaderboard_player_4)
        player5TV = findViewById(R.id.leaderboard_player_5)
        score1TV = findViewById(R.id.leaderboard_score_1)
        score2TV = findViewById(R.id.leaderboard_score_2)
        score3TV = findViewById(R.id.leaderboard_score_3)
        score4TV = findViewById(R.id.leaderboard_score_4)
        score5TV = findViewById(R.id.leaderboard_score_5)
        state1TV = findViewById(R.id.leaderboard_state_1)
        state2TV = findViewById(R.id.leaderboard_state_2)
        state3TV = findViewById(R.id.leaderboard_state_3)
        state4TV = findViewById(R.id.leaderboard_state_4)
        state5TV = findViewById(R.id.leaderboard_state_5)

        homeButton = findViewById(R.id.homeButton)
        play = findViewById(R.id.leaderBoardPlay)
        settings = findViewById(R.id.settings)
        // get the top 5 highest scorers
        var firebase: FirebaseDatabase = FirebaseDatabase.getInstance()
        var reference: DatabaseReference = firebase.getReference("users")
        val query: Query = reference.orderByChild("highscore").limitToLast(5)
        query.addListenerForSingleValueEvent(DataListener())


        // banner ad
        adView = AdView(this)
        var adSize: AdSize = AdSize(AdSize.FULL_WIDTH, AdSize.AUTO_HEIGHT)
        adView.setAdSize(adSize)
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
        var builder: AdRequest.Builder = AdRequest.Builder()
        var request: AdRequest = builder.build()

        // put adView in linear layout
        var adLayout: LinearLayout = findViewById(R.id.ad_view)
        adLayout.addView(adView)
        adView.loadAd(request)

        // ADDED: 3 buttons so users can either go back to home, play again, or settings screen
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        play.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
            finish()
        }

        settings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    inner class DataListener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {

            // list of (username, highscore, state)
            var topUsersList = mutableListOf<Triple<String, Int, String>>()

            for (user in snapshot.children) {
                val username: String = user.key!!
                val score: Int = user.child("highscore").value.toString().toInt()
                val state: String = user.child("state").value.toString()

                topUsersList.add(Triple(username, score, state))
            }

            // sort users by highest high score
            topUsersList = topUsersList.sortedByDescending { it.second }.toMutableList()

            if (topUsersList.size >= 1) {
                player1TV.text = "1. " + topUsersList[0].first
                score1TV.text = topUsersList[0].second.toString()
                state1TV.text = topUsersList[0].third
            }
            if (topUsersList.size > 1) {
                player2TV.text = "2. " + topUsersList[1].first
                score2TV.text = topUsersList[1].second.toString()
                state2TV.text = topUsersList[1].third
            }
            if (topUsersList.size > 2) {
                player3TV.text = "3. " + topUsersList[2].first
                score3TV.text = topUsersList[2].second.toString()
                state3TV.text = topUsersList[2].third
            }
            if (topUsersList.size > 3) {
                player4TV.text = "4. " + topUsersList[3].first
                score4TV.text = topUsersList[3].second.toString()
                state4TV.text = topUsersList[3].third
            }
            if (topUsersList.size > 4) {
                player5TV.text = "5. " + topUsersList[4].first
                score5TV.text = topUsersList[4].second.toString()
                state5TV.text = topUsersList[4].third
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("LeaderboardActivity", "Inside onCancelled")
        }
    }

}
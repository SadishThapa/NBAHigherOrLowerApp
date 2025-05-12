package com.example.groupproject

import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import org.json.JSONObject
import android.media.MediaPlayer
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class GameActivity : AppCompatActivity() {

    private lateinit var game : Game
    private lateinit var progressBar : ProgressBar
    private lateinit var higherButton : Button
    private lateinit var lowerButton : Button
    private lateinit var firstImage : ImageView
    private lateinit var secondImage : ImageView

    private lateinit var currentScore : TextView
    private lateinit var firstPlayerText : TextView
    private lateinit var statNumber : TextView
    private lateinit var firstPlayerStat : TextView
    private lateinit var secondPlayerText : TextView
    private lateinit var secondPlayerStat : TextView

    private lateinit var leaderBoardIntent : Intent
    private lateinit var mainActivityIntent : Intent

    private lateinit var topPlayer : JSONObject
    private lateinit var bottomPlayer : JSONObject

    // Animation for progress bar
    private lateinit var anim : ObjectAnimator


    private var backgroundMusic: MediaPlayer? = null
    private var playBackgroundMusic = true // Will later be set via Settings
    private var timerInProgress : Boolean = true
    private var gameInProgress : Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // ADDED: Local persistent storage
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        playBackgroundMusic = prefs.getBoolean("backgroundSound", true)

        if (playBackgroundMusic) {
            backgroundMusic = MediaPlayer.create(this, R.raw.bg_music)
            backgroundMusic?.isLooping = true
            Log.w("GameActivity", "Starting background music")
            backgroundMusic?.start()
        }

        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        higherButton = findViewById(R.id.higher)
        lowerButton = findViewById(R.id.lower)
        firstImage = findViewById(R.id.player_image_1)
        secondImage = findViewById(R.id.player_image_2)

        currentScore = findViewById(R.id.current_score)
        firstPlayerText = findViewById(R.id.first_player)
        statNumber = findViewById(R.id.stat_number)
        firstPlayerStat = findViewById(R.id.first_player_stat)
        secondPlayerText = findViewById(R.id.second_player)
        secondPlayerStat = findViewById(R.id.second_player_stat)


        Glide.with(this)
            .load("https://cdn.nba.com/headshots/nba/latest/1040x760/.png")
            .into(firstImage)

        Glide.with(this)
            .load("https://cdn.nba.com/headshots/nba/latest/1040x760/.png")
            .into(secondImage)


        higherButton.setOnClickListener { buttonHandler("higher") }
        lowerButton.setOnClickListener { buttonHandler("lower") }

        // ADDED: Local persistent storage
        val selectedSeason = prefs.getString("season", "2024-2025") ?: "2024-2025"
        val startingSeconds = prefs.getInt("timerSeconds", 8)
        val startingLock: String? = null
        game = Game(resources, progressBar, startingSeconds, selectedSeason, startingLock)

        progressBar.max = 10000

        if (game.getSeconds() > 0) {
            anim = ObjectAnimator.ofInt(progressBar, "progress", progressBar.max)
            anim.setDuration((game.getSeconds() * 1000).toLong())
            // Event driven function that ends game when user runs out of time
            anim.doOnEnd({
                if(timerInProgress) {
                    endGame("timeout")
                }
            })
        }
        startRound()
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        playBackgroundMusic = prefs.getBoolean("backgroundSound", true)

        if (playBackgroundMusic) {
            backgroundMusic = MediaPlayer.create(this, R.raw.bg_music)
            backgroundMusic?.isLooping = true
            backgroundMusic?.start()
        }
    }

    fun startRound() {
        game.nextRound()

        topPlayer = game.getTopPlayer()
        bottomPlayer = game.getBottomPlayer()

        Glide.with(this)
            .load("https://cdn.nba.com/headshots/nba/latest/1040x760/" +
                    "${topPlayer.getInt("id")}.png")
            .into(firstImage)

        Glide.with(this)
            .load("https://cdn.nba.com/headshots/nba/latest/1040x760/" +
                    "${bottomPlayer.getInt("id")}.png")
            .into(secondImage)

        var score : Int = game.getScore()
        var currentStat : String = game.getCurrentStat()
        var statKey : String = game.getStatKey()
        var season : String = game.getSeason()

        var topPlayerName = topPlayer.getString("name")
        var topPlayerStat = topPlayer.getDouble(game.getStatKey())
        var bottomPlayerName = bottomPlayer.getString("name")

        currentScore.text = "Current score: ${score}"
        firstPlayerText.text = "${topPlayerName} averaged"
        statNumber.text = "${topPlayerStat}"
        firstPlayerStat.text = "${currentStat} per game in the ${season} season"
        secondPlayerText.text = "${bottomPlayerName} had a"
        secondPlayerStat.text = "${currentStat} per game average that season"

        if (game.getSeconds() > 0) {
            anim.start()
            timerInProgress = true
            progressBar.visibility = View.VISIBLE
        }
    }

    // Handle button click based on if "higher" or "lower" was chosen
    fun buttonHandler(which : String) {
        if (gameInProgress) {
            timerInProgress = false
            progressBar.visibility = View.INVISIBLE

            var correct : Boolean = game.checkCorrectness(which)

            if (correct) {
                correctAnswer(which)
            }

            else {
                wrongAnswer(which)
            }
        }
    }


    fun correctAnswer(which : String) {
        val toast = Toast.makeText(this, "Correct! He averaged " +
                "${bottomPlayer.getDouble(game.getStatKey())}", Toast.LENGTH_LONG)
        toast.show()

        var b : Button

        if (which == "higher") {
            b = higherButton
        }

        else {
            b = lowerButton
        }

        Thread.sleep(3500)

        if (which == "higher") {
            b.text = "Higher ▲"
            b.setBackgroundColor(Color.BLACK)
        }

        else {
            b.text = "Lower ▼"
            b.setTextColor(Color.BLACK)
            b.setBackgroundColor(Color.WHITE)
        }

        game.incrementScore()

        startRound()

    }

    fun wrongAnswer(which : String) {
        var b : Button

        if (which == "higher") {
            b = higherButton
        }

        else {
            b = lowerButton
        }

        b.text = "✖"
        b.setTextColor(Color.WHITE)
        b.setBackgroundColor(Color.RED)

        endGame("wrong answer")
    }

    fun endGame(cause : String) {
        gameInProgress = false
        var titleStart : String
        var title : String
        if (cause == "timeout") {
            titleStart = "Time out!"
        }
        else {
            titleStart = "Incorrect Answer!"
        }

        bottomPlayer = game.getBottomPlayer()

        title = titleStart + " ${bottomPlayer.getString("name")} averaged " +
                "${bottomPlayer.getDouble(game.getStatKey())}" +
                " ${game.getCurrentStat().lowercase()} per game"

        var alertMessage = ""
        var alert : AlertDialog.Builder = AlertDialog.Builder( this )
        alert.setTitle(title)
        alert.setMessage(alertMessage)

        mainActivityIntent = Intent(this, MainActivity::class.java)
        leaderBoardIntent = Intent(this, LeaderboardActivity::class.java)
        var playAgain : PlayDialog = PlayDialog()
        alert.setPositiveButton( "PLAY AGAIN", playAgain )
        alert.setNegativeButton( "LEADERBOARD", playAgain )
        val alertDialog = alert.show()

        addUserToLeaderboard(game.getUsername(), game.getScore(), game.getUserState(), alertDialog)
    }

    // add user to leaderboard, if user already exists and new score is higher then update it
    // sets the alert message
    fun addUserToLeaderboard(username: String, score: Int, state: String, dialog: AlertDialog) {
        var firebase: FirebaseDatabase = FirebaseDatabase.getInstance()
        var reference: DatabaseReference = firebase.getReference("users").child(username)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // snapshot.child("highscore")
                // snapshot.child("state")

                var message: String = "Username: ${game.getUsername()}\n" +
                        "Score: ${game.getScore()}\n"

                // check if this is a new player
                if (snapshot.child("highscore").value != null) {
                    var currLeaderboardScore = snapshot.child("highscore").value.toString().toInt()
                    reference.child("state").setValue(state)

                    if (currLeaderboardScore < game.getScore()) {
                        // new score beats old high score
                        reference.child("highscore").setValue(score)
                        message += "New high score!\nHigh score for ${game.getUsername()} is now: ${score}"

                    } else {
                        // old score is still highest
                        message += "High score for ${game.getUsername()} is still: ${currLeaderboardScore}"
                    }

                } else {
                    // this is user's first game, so we update high score
                    reference.child("highscore").setValue(score)
                    message += "This is your first game! Your high score is: ${score}"

                    // also update state
                    reference.child("state").setValue(state)
                }

                dialog.setMessage(message)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("LeaderboardActivity", "Inside onCancelled")
            }
        })
    }

    inner class PlayDialog : DialogInterface.OnClickListener {

        override fun onClick(p0: DialogInterface?, p1: Int) {
            if( p1 == - 1 ) { // PLAY AGAIN button
                Log.w("GameActivity", "Starting MainActivity")
                // startActivity(mainActivityIntent)
                // restarts game
                recreate()

            } else if( p1 == -2 ) { // LEADERBOARD Button
                Log.w("GameActivity", "Starting LeaderboardActivity")
                startActivity(leaderBoardIntent)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        backgroundMusic?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundMusic?.stop()
        backgroundMusic?.release()
        backgroundMusic = null
    }
}
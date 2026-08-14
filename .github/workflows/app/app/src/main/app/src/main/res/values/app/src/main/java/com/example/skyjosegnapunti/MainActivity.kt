package com.example.skyjosegnapunti

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.graphics.Typeface
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

data class Player(
    val name: String,
    val scores: MutableList<Int> = mutableListOf()
) {
    val total: Int
        get() = scores.sum()
}

class MainActivity : AppCompatActivity() {

    private val players = mutableListOf<Player>()
    private val inputs = mutableListOf<EditText>()

    private lateinit var board: LinearLayout
    private lateinit var title: TextView

    private var round = 0

    private val prefs by lazy {
        getSharedPreferences("skyjo", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSetup()
    }

    private fun showSetup() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 40, 32, 24)
        }

        val logo = TextView(this).apply {
            text = "SKYJO"
            textSize = 38f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        root.addView(
            logo,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val subtitle = TextView(this).apply {
            text = "SEGNAPUNTI"
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }

        root.addView(subtitle)

        val playersInput = EditText(this).apply {
            hint = "Numero giocatori (2-8)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        root.addView(playersInput)

        val start = Button(this).apply {
            text = "INIZIA PARTITA"
        }

        root.addView(
            start,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 24
            }
        )

        start.setOnClickListener {

            val count =
                (playersInput.text.toString().toIntOrNull() ?: 2)
                    .coerceIn(2, 8)

            players.clear()

            repeat(count) { index ->
                players.add(
                    Player("Giocatore ${index + 1}")
                )
            }

            round = 0

            saveGame()

            showGame()
        }

        setContentView(root)
    }

    private fun showGame() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 24, 20, 16)
        }

        title = TextView(this).apply {
            text = "SKYJO • Round ${round + 1}"
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(title)

        val instruction = TextView(this).apply {
            text = "Inserisci i punti ottenuti da ogni giocatore"
            textSize = 15f
            setPadding(0, 8, 0, 16)
        }

        root.addView(instruction)

        board = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(this).apply {
            addView(board)
        }

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        val confirm = Button(this).apply {
            text = "CONFERMA ROUND"
        }

        root.addView(confirm)

        val newGame = Button(this).apply {
            text = "NUOVA PARTITA"
        }

        root.addView(newGame)

        confirm.setOnClickListener {
            confirmRound()
        }

        newGame.setOnClickListener {
            showSetup()
        }

        setContentView(root)

        renderBoard()
    }

    private fun renderBoard() {

        board.removeAllViews()
        inputs.clear()

        val header = TextView(this).apply {
            text = "GIOCATORE                    PUNTI       TOTALE"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 8, 0, 12)
        }

        board.addView(header)

        players.forEach { player ->

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val name = TextView(this).apply {
                text = player.name
                textSize = 17f
            }

            row.addView(
                name,
                LinearLayout.LayoutParams(
                    0,
                    60,
                    1f
                )
            )

            val input = EditText(this).apply {
                hint = "0"
                textSize = 17f
                gravity = Gravity.CENTER
                inputType =
                    InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_SIGNED
            }

            inputs.add(input)

            row.addView(
                input,
                LinearLayout.LayoutParams(
                    90,
                    60
                )
            )

            val total = TextView(this).apply {
                text = player.total.toString()
                textSize = 17f
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }

            row.addView(
                total,
                LinearLayout.LayoutParams(
                    80,
                    60
                )
            )

            board.addView(row)
        }

        board.addView(
            TextView(this).apply {
                text = "STORICO ROUND"
                textSize = 20f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(0, 24, 0, 12)
            }
        )

        val maxRounds =
            players.maxOfOrNull { it.scores.size } ?: 0

        for (r in 0 until maxRounds) {

            val values = players.joinToString("   |   ") {
                "${it.name}: ${it.scores.getOrNull(r) ?: 0}"
            }

            board.addView(
                TextView(this).apply {
                    text = "Round ${r + 1}\n$values"
                    textSize = 14f
                    setPadding(0, 6, 0, 10)
                }
            )
        }
    }

    private fun confirmRound() {

        if (inputs.any {
                it.text.toString().trim().isEmpty()
            }
        ) {
            Toast.makeText(
                this,
                "Inserisci tutti i punteggi",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        inputs.forEachIndexed { index, input ->

            val value =
                input.text.toString().toIntOrNull() ?: 0

            players[index].scores.add(value)
        }

        round++

        saveGame()

        checkGameEnd()

        showGame()
    }

    private fun checkGameEnd() {

        val finished =
            players.any { it.total >= 100 }

        if (!finished) return

        val winner =
            players.minByOrNull { it.total } ?: return

        AlertDialog.Builder(this)
            .setTitle("🏆 Partita terminata")
            .setMessage(
                "Vince ${winner.name}!\n\n" +
                "Punteggio: ${winner.total}"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun saveGame() {

        val data =
            players.joinToString(";") { player ->

                val scores =
                    player.scores.joinToString(",")

                "${player.name}|$scores"
            }

        prefs.edit()
            .putString("players", data)
            .putInt("round", round)
            .apply()
    }
}

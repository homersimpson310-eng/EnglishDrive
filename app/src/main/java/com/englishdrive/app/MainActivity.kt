package com.englishdrive.app

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech

    private val questions = listOf(
        "What did you do yesterday?",
        "What are you going to do tomorrow?",
        "Can you describe your hometown?",
        "What would you do if you had more free time?"
    )

    private var currentQuestion = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }

        val title = TextView(this).apply {
            text = "EnglishDrive"
            textSize = 28f
        }

        val question = TextView(this).apply {
            text = questions[currentQuestion]
            textSize = 22f
            setPadding(0, 40, 0, 40)
        }

        val speakButton = Button(this).apply {
            text = "🔊 Frage hören"
            setOnClickListener {
                speak(question.text.toString())
            }
        }

        val nextButton = Button(this).apply {
            text = "Nächste Frage"
            setOnClickListener {
                currentQuestion =
                    (currentQuestion + 1) % questions.size
                question.text = questions[currentQuestion]
            }
        }

        layout.addView(title)
        layout.addView(question)
        layout.addView(speakButton)
        layout.addView(nextButton)

        setContentView(layout)
    }

    private fun speak(text: String) {
        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "englishdrive"
        )
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}

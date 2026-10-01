package com.englishdrive.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var questionText: TextView
    private lateinit var resultText: TextView
    private lateinit var progressText: TextView
    private lateinit var scoreText: TextView

    private var currentQuestion = 0
    private var score = 0

    private val questions = listOf(
        "What did you do last weekend?",
        "What are your plans for the next few months?",
        "What are the advantages and disadvantages of working from home?",
        "Describe a difficult decision you have made in your life.",
        "What would you change about your hometown?",
        "Do you think technology makes our lives better? Why?",
        "What qualities make someone a good leader?",
        "If you could live in another country, where would you live and why?",
        "What is the most important skill people should learn today?",
        "How do you think education will change in the future?"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestAudioPermission()

        tts = TextToSpeech(this, this)

        createInterface()
        showQuestion()
    }

    private fun createInterface() {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 40, 32, 32)
        }

        val title = TextView(this).apply {
            text = "EnglishDrive"
            textSize = 30f
            gravity = Gravity.CENTER
        }

        progressText = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
        }

        questionText = TextView(this).apply {
            textSize = 22f
            gravity = Gravity.CENTER
            setPadding(0, 35, 0, 35)
        }

        val listenButton = Button(this).apply {
            text = "🔊 Frage hören"
            setOnClickListener {
                speak(questionText.text.toString())
            }
        }

        val answerButton = Button(this).apply {
            text = "🎤 Antwort sprechen"
            setOnClickListener {
                startSpeechRecognition()
            }
        }

        resultText = TextView(this).apply {
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 25, 0, 25)
        }

        scoreText = TextView(this).apply {
            textSize = 18f
            gravity = Gravity.CENTER
        }

        val nextButton = Button(this).apply {
            text = "➡️ Nächste Übung"
            setOnClickListener {
                nextQuestion()
            }
        }

        layout.addView(title)
        layout.addView(progressText)
        layout.addView(questionText)
        layout.addView(listenButton)
        layout.addView(answerButton)
        layout.addView(resultText)
        layout.addView(scoreText)
        layout.addView(nextButton)

        setContentView(layout)
    }

    private fun showQuestion() {

        if (currentQuestion >= questions.size) {
            finishSession()
            return
        }

        questionText.text = questions[currentQuestion]

        progressText.text =
            "Übung ${currentQuestion + 1} von ${questions.size}"

        scoreText.text = "Punkte: $score"

        resultText.text =
            "Höre die Frage und antworte auf Englisch."
    }

    private fun startSpeechRecognition() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            resultText.text =
                "Spracherkennung ist auf diesem Gerät nicht verfügbar."
            return
        }

        val recognizer =
            SpeechRecognizer.createSpeechRecognizer(this)

        recognizer.setRecognitionListener(
            object : android.speech.RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    resultText.text = "🎤 Ich höre zu..."
                }

                override fun onBeginningOfSpeech() {
                    resultText.text = "🎤 Sprich jetzt..."
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    resultText.text = "⏳ Auswertung..."
                }

                override fun onError(error: Int) {
                    resultText.text =
                        "Ich konnte deine Antwort nicht erkennen. Bitte versuche es erneut."
                    recognizer.destroy()
                }

                override fun onResults(results: Bundle?) {

                    val answers =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    val answer =
                        answers?.firstOrNull() ?: ""

                    evaluateAnswer(answer)

                    recognizer.destroy()
                }

                override fun onPartialResults(
                    partialResults: Bundle?
                ) {}

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {}
            }
        )

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.US
            )
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Answer in English"
            )
        }

        recognizer.startListening(intent)
    }

    private fun evaluateAnswer(answer: String) {

        if (answer.isBlank()) {
            resultText.text = "Keine Antwort erkannt."
            return
        }

        val words =
            answer.trim()
                .split("\\s+".toRegex())
                .filter { it.isNotBlank() }

        val wordCount = words.size

        val points = when {
            wordCount >= 15 -> 3
            wordCount >= 8 -> 2
            wordCount >= 3 -> 1
            else -> 0
        }

        score += points

        val feedback = when {
            points == 3 ->
                "🌟 Sehr gut! Du hast ausführlich geantwortet."

            points == 2 ->
                "👍 Gut! Versuche beim nächsten Mal noch etwas ausführlicher zu antworten."

            points == 1 ->
                "🙂 Gute Richtung. Versuche mehr Details zu nennen."

            else ->
                "💪 Versuche mit vollständigen Sätzen zu antworten."
        }

        resultText.text =
            "$feedback\n\nDeine Antwort:\n$answer"

        scoreText.text =
            "Punkte: $score"
    }

    private fun nextQuestion() {

        currentQuestion++

        showQuestion()

        speak(questionText.text.toString())
    }

    private fun finishSession() {

        questionText.text =
            "🎉 Training abgeschlossen!"

        progressText.text =
            "EnglishDrive"

        resultText.text =
            "Du hast alle Übungen absolviert."

        scoreText.text =
            "Gesamtpunkte: $score"

        speak(
            "Well done! You have completed today's EnglishDrive session."
        )
    }

    private fun speak(text: String) {

        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "englishdrive"
        )
    }

    private fun requestAudioPermission() {

        if (
            checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        }
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }

    override fun onDestroy() {

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }
}

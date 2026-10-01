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

    private lateinit var content: LinearLayout

    private var score = 0
    private var sessionSeconds = 0
    private var sessionRunning = false

    private val prefs by lazy {
        getSharedPreferences("englishdrive", MODE_PRIVATE)
    }

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

    private var currentQuestion = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        requestAudioPermission()

        showHome()
    }

    // ---------------------------------------------------------
    // STARTSEITE
    // ---------------------------------------------------------

    private fun showHome() {

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 40, 32, 32)
        }

        val title = TextView(this).apply {
            text = "🚗 EnglishDrive"
            textSize = 30f
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Dein persönliches Englischtraining"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 30)
        }

        val level = TextView(this).apply {
            text = "🎯 Lernweg\nA2/B1 → B2 → C1 → C2"
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 30)
        }

        val morning = Button(this).apply {
            text = "🌅 Morgentraining\n15 Minuten"
            textSize = 18f

            setOnClickListener {
                startSession("Morgentraining")
            }
        }

        val evening = Button(this).apply {
            text = "🌙 Abendtraining\n15 Minuten"
            textSize = 18f

            setOnClickListener {
                startSession("Abendtraining")
            }
        }

        val progress = TextView(this).apply {
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 30, 0, 20)

            val minutes =
                prefs.getInt("minutes", 0)

            val words =
                prefs.getInt("words", 0)

            val answers =
                prefs.getInt("answers", 0)

            val streak =
                prefs.getInt("streak", 1)

            text =
                "📊 Dein Fortschritt\n\n" +
                "⏱️ Lernzeit: $minutes Minuten\n" +
                "📚 Wörter: $words\n" +
                "🗣️ Antworten: $answers\n" +
                "🔥 Streak: $streak Tage"
        }

        val reset = Button(this).apply {
            text = "↻ Fortschritt zurücksetzen"

            setOnClickListener {

                prefs.edit().clear().apply()

                showHome()
            }
        }

        content.addView(title)
        content.addView(subtitle)
        content.addView(level)
        content.addView(morning)
        content.addView(evening)
        content.addView(progress)
        content.addView(reset)

        setContentView(content)
    }

    // ---------------------------------------------------------
    // LERNSSESSION
    // ---------------------------------------------------------

    private fun startSession(type: String) {

        currentQuestion = 0
        score = 0
        sessionSeconds = 0
        sessionRunning = true

        showSession(type)
    }

    private fun showSession(type: String) {

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 40, 32, 32)
        }

        val title = TextView(this).apply {
            text = "🚗 $type"
            textSize = 28f
            gravity = Gravity.CENTER
        }

        val timer = TextView(this).apply {
            text = "⏱️ 15:00"
            textSize = 22f
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 20)
        }

        val progress = TextView(this).apply {
            text = "Übung ${currentQuestion + 1} von ${questions.size}"
            textSize = 16f
            gravity = Gravity.CENTER
        }

        val question = TextView(this).apply {
            text = questions[currentQuestion]
            textSize = 22f
            gravity = Gravity.CENTER
            setPadding(0, 30, 0, 30)
        }

        val listen = Button(this).apply {
            text = "🔊 Frage hören"

            setOnClickListener {
                speak(question.text.toString())
            }
        }

        val speakButton = Button(this).apply {
            text = "🎤 Antwort sprechen"

            setOnClickListener {

                startSpeechRecognition(
                    question,
                    progress
                )
            }
        }

        val result = TextView(this).apply {
            text = "Antworte möglichst ausführlich auf Englisch."
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 20)
        }

        val next = Button(this).apply {
            text = "➡️ Nächste Übung"

            setOnClickListener {

                currentQuestion++

                if (currentQuestion >= questions.size) {
                    finishSession()
                } else {

                    progress.text =
                        "Übung ${currentQuestion + 1} von ${questions.size}"

                    question.text =
                        questions[currentQuestion]

                    result.text =
                        "Antworte möglichst ausführlich auf Englisch."

                    speak(question.text.toString())
                }
            }
        }

        val home = Button(this).apply {
            text = "🏠 Startseite"

            setOnClickListener {
                finishSession()
            }
        }

        content.addView(title)
        content.addView(timer)
        content.addView(progress)
        content.addView(question)
        content.addView(listen)
        content.addView(speakButton)
        content.addView(result)
        content.addView(next)
        content.addView(home)

        setContentView(content)

        speak(question.text.toString())
    }

    // ---------------------------------------------------------
    // SPRACHERKENNUNG
    // ---------------------------------------------------------

    private fun startSpeechRecognition(
        question: TextView,
        progress: TextView
    ) {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            return
        }

        val recognizer =
            SpeechRecognizer.createSpeechRecognizer(this)

        recognizer.setRecognitionListener(
            object : android.speech.RecognitionListener {

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {
                    progress.text =
                        "🎤 Ich höre zu..."
                }

                override fun onBeginningOfSpeech() {
                    progress.text =
                        "🎤 Sprich jetzt..."
                }

                override fun onRmsChanged(
                    rmsdB: Float
                ) {}

                override fun onBufferReceived(
                    buffer: ByteArray?
                ) {}

                override fun onEndOfSpeech() {
                    progress.text =
                        "⏳ Auswertung..."
                }

                override fun onError(
                    error: Int
                ) {
                    progress.text =
                        "Bitte versuche es erneut."

                    recognizer.destroy()
                }

                override fun onResults(
                    results: Bundle?
                ) {

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

        val intent =
            Intent(
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

    // ---------------------------------------------------------
    // ANTWORT AUSWERTEN
    // ---------------------------------------------------------

    private fun evaluateAnswer(
        answer: String
    ) {

        if (answer.isBlank()) {
            return
        }

        val words =
            answer
                .trim()
                .split("\\s+".toRegex())

        val wordCount =
            words.size

        val points =
            when {

                wordCount >= 20 -> 3

                wordCount >= 10 -> 2

                wordCount >= 4 -> 1

                else -> 0
            }

        score += points

        val answers =
            prefs.getInt(
                "answers",
                0
            ) + 1

        val minutes =
            prefs.getInt(
                "minutes",
                0
            )

        prefs.edit()
            .putInt(
                "answers",
                answers
            )
            .putInt(
                "minutes",
                minutes + 1
            )
            .apply()
    }

    // ---------------------------------------------------------
    // SESSION ENDE
    // ---------------------------------------------------------

    private fun finishSession() {

        sessionRunning = false

        val answers =
            prefs.getInt(
                "answers",
                0
            )

        val minutes =
            prefs.getInt(
                "minutes",
                0
            )

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 60, 32, 32)
        }

        val title = TextView(this).apply {
            text = "🎉 Training abgeschlossen!"
            textSize = 28f
            gravity = Gravity.CENTER
        }

        val result = TextView(this).apply {

            text =
                "Sehr gut!\n\n" +
                "🏆 Punkte: $score\n" +
                "🗣️ Antworten: $answers\n" +
                "⏱️ Lernzeit: $minutes Minuten"

            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 40)
        }

        val home = Button(this).apply {
            text = "🏠 Zur Startseite"

            setOnClickListener {
                showHome()
            }
        }

        content.addView(title)
        content.addView(result)
        content.addView(home)

        setContentView(content)

        speak(
            "Well done! Your English training is complete."
        )
    }

    // ---------------------------------------------------------
    // TEXT TO SPEECH
    // ---------------------------------------------------------

    private fun speak(
        text: String
    ) {

        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "englishdrive"
        )
    }

    override fun onInit(
        status: Int
    ) {

        if (
            status ==
            TextToSpeech.SUCCESS
        ) {

            tts.language =
                Locale.US
        }
    }

    private fun requestAudioPermission() {

        if (
            checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ),
                100
            )
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

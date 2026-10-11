package com.example.economicsquizgame

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

private data class Question(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

class MainActivity : AppCompatActivity() {

    private lateinit var studentNameEditText: EditText
    private lateinit var timerTextView: TextView
    private lateinit var questionCounterTextView: TextView
    private lateinit var questionTextView: TextView
    private lateinit var option1Button: Button
    private lateinit var option2Button: Button
    private lateinit var option3Button: Button
    private lateinit var option4Button: Button
    private lateinit var skipButton: Button
    private lateinit var nextButton: Button
    private lateinit var quizLayout: View
    private lateinit var resultLayout: ScrollView

    private lateinit var resultStudentTextView: TextView
    private lateinit var resultTotalQuestionsTextView: TextView
    private lateinit var resultCorrectTextView: TextView
    private lateinit var resultWrongTextView: TextView
    private lateinit var resultWrongAttemptsTextView: TextView
    private lateinit var resultSkippedTextView: TextView
    private lateinit var resultScoreTextView: TextView
    private lateinit var resultPercentageTextView: TextView
    private lateinit var resultAccuracyTextView: TextView
    private lateinit var resultTotalTimeTextView: TextView
    private lateinit var resultAverageSpeedTextView: TextView
    private lateinit var retryButton: Button

    private val questions = listOf(
        Question("The basic economic problem is related to:", listOf("Scarcity", "Abundance", "Inflation", "Growth"), 0),
        Question("Who is considered the father of modern economics?", listOf("Adam Smith", "Karl Marx", "John Maynard Keynes", "David Ricardo"), 0),
        Question("GDP stands for:", listOf("Gross Domestic Product", "General Development Plan", "Government Development Program", "Gross Domestic Price"), 0),
        Question("Demand increases when price:", listOf("Falls", "Rises", "Remains constant", "Varies unpredictably"), 0),
        Question("The central bank of India is:", listOf("RBI", "SBI", "SEBI", "NABARD"), 0),
        Question("Inflation means:", listOf("Rise in general price level", "Rise in income", "Fall in population", "Fall in production"), 0),
        Question("Production of goods and services is called:", listOf("Output", "Investment", "Savings", "Trade"), 0),
        Question("A market where goods are exchanged for money is known as:", listOf("Product market", "Factor market", "Labour market", "Capital market"), 0),
        Question("Tax paid by the seller on sales is called:", listOf("Sales tax", "Income tax", "Wealth tax", "Property tax"), 0),
        Question("Opportunity cost means:", listOf("Cost of next best alternative foregone", "Actual money cost", "Total cost", "Average cost"), 0),
        Question("The law of demand states that demand and price are:", listOf("Inversely related", "Directly related", "Unrelated", "Constant"), 0),
        Question("The study of individual behaviour in the economy is called:", listOf("Microeconomics", "Macroeconomics", "Statistics", "Social economics"), 0)
    )

    private var currentQuestionIndex = 0
    private var correct = 0
    private var wrong = 0
    private var wrongAttempts = 0
    private var skipped = 0
    private var score = 0
    private var answeredCount = 0
    private var totalTimeMs = 0L
    private var questionStartMs = 0L
    private var timeLeft = 30
    private var isQuestionLocked = false
    private var quizStartTimeMs = 0L
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        studentNameEditText = findViewById(R.id.studentNameEditText)
        timerTextView = findViewById(R.id.timerTextView)
        questionCounterTextView = findViewById(R.id.questionCounterTextView)
        questionTextView = findViewById(R.id.questionTextView)
        option1Button = findViewById(R.id.option1Button)
        option2Button = findViewById(R.id.option2Button)
        option3Button = findViewById(R.id.option3Button)
        option4Button = findViewById(R.id.option4Button)
        skipButton = findViewById(R.id.skipButton)
        nextButton = findViewById(R.id.nextButton)
        quizLayout = findViewById(R.id.quizLayout)
        resultLayout = findViewById(R.id.resultLayout)

        resultStudentTextView = findViewById(R.id.resultStudentTextView)
        resultTotalQuestionsTextView = findViewById(R.id.resultTotalQuestionsTextView)
        resultCorrectTextView = findViewById(R.id.resultCorrectTextView)
        resultWrongTextView = findViewById(R.id.resultWrongTextView)
        resultWrongAttemptsTextView = findViewById(R.id.resultWrongAttemptsTextView)
        resultSkippedTextView = findViewById(R.id.resultSkippedTextView)
        resultScoreTextView = findViewById(R.id.resultScoreTextView)
        resultPercentageTextView = findViewById(R.id.resultPercentageTextView)
        resultAccuracyTextView = findViewById(R.id.resultAccuracyTextView)
        resultTotalTimeTextView = findViewById(R.id.resultTotalTimeTextView)
        resultAverageSpeedTextView = findViewById(R.id.resultAverageSpeedTextView)
        retryButton = findViewById(R.id.retryButton)

        option1Button.setOnClickListener { handleAnswer(0) }
        option2Button.setOnClickListener { handleAnswer(1) }
        option3Button.setOnClickListener { handleAnswer(2) }
        option4Button.setOnClickListener { handleAnswer(3) }
        skipButton.setOnClickListener { skipCurrentQuestion() }
        nextButton.setOnClickListener { goToNextQuestion() }
        retryButton.setOnClickListener { restartQuiz() }

        quizStartTimeMs = SystemClock.elapsedRealtime()
        showQuestion()
    }

    private fun showQuestion() {
        if (currentQuestionIndex >= questions.size) {
            showResult()
            return
        }

        val question = questions[currentQuestionIndex]
        isQuestionLocked = false
        timeLeft = 30
        questionStartMs = SystemClock.elapsedRealtime()
        questionTextView.text = "${currentQuestionIndex + 1}. ${question.question}"
        questionCounterTextView.text = "Q ${currentQuestionIndex + 1}/${questions.size}"
        timerTextView.text = "${timeLeft}s"

        val optionButtons = listOf(option1Button, option2Button, option3Button, option4Button)
        question.options.forEachIndexed { index, option ->
            optionButtons[index].text = option
            optionButtons[index].isEnabled = true
            optionButtons[index].setBackgroundColor(getColor(android.R.color.holo_blue_dark))
        }

        handler.removeCallbacksAndMessages(null)
        handler.post(object : Runnable {
            override fun run() {
                if (isQuestionLocked) return
                if (timeLeft <= 0) {
                    handleTimeout()
                    return
                }
                timeLeft -= 1
                timerTextView.text = "${timeLeft}s"
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun handleAnswer(selectedIndex: Int) {
        if (isQuestionLocked) return
        isQuestionLocked = true
        totalTimeMs += (SystemClock.elapsedRealtime() - questionStartMs)

        val question = questions[currentQuestionIndex]
        val buttons = listOf(option1Button, option2Button, option3Button, option4Button)

        buttons.forEachIndexed { index, button ->
            button.isEnabled = false
            if (index == question.correctIndex) {
                button.setBackgroundColor(getColor(android.R.color.holo_green_dark))
            }
        }

        answeredCount += 1
        if (selectedIndex == question.correctIndex) {
            correct += 1
            score += 1
            buttons[selectedIndex].setBackgroundColor(getColor(android.R.color.holo_green_dark))
        } else {
            wrong += 1
            wrongAttempts += 1
            buttons[selectedIndex].setBackgroundColor(getColor(android.R.color.holo_red_dark))
            buttons[question.correctIndex].setBackgroundColor(getColor(android.R.color.holo_green_dark))
        }

        nextButton.isEnabled = true
    }

    private fun handleTimeout() {
        if (isQuestionLocked) return
        isQuestionLocked = true
        totalTimeMs += 30000L

        val question = questions[currentQuestionIndex]
        val buttons = listOf(option1Button, option2Button, option3Button, option4Button)

        wrong += 1
        wrongAttempts += 1
        buttons.forEachIndexed { index, button ->
            button.isEnabled = false
            if (index == question.correctIndex) {
                button.setBackgroundColor(getColor(android.R.color.holo_green_dark))
            }
        }

        answeredCount += 1
        nextButton.isEnabled = true
        timerTextView.text = "0s"
    }

    private fun skipCurrentQuestion() {
        if (isQuestionLocked) return
        isQuestionLocked = true
        totalTimeMs += (SystemClock.elapsedRealtime() - questionStartMs)
        skipped += 1
        answeredCount += 1
        nextButton.isEnabled = true
        timerTextView.text = "0s"
        val correctIndex = questions[currentQuestionIndex].correctIndex
        val buttons = listOf(option1Button, option2Button, option3Button, option4Button)
        buttons.forEachIndexed { index, button ->
            button.isEnabled = false
            if (index == correctIndex) {
                button.setBackgroundColor(getColor(android.R.color.holo_green_dark))
            }
        }
    }

    private fun goToNextQuestion() {
        handler.removeCallbacksAndMessages(null)
        currentQuestionIndex += 1
        if (currentQuestionIndex < questions.size) {
            nextButton.isEnabled = false
            showQuestion()
        } else {
            showResult()
        }
    }

    private fun showResult() {
        val studentName = studentNameEditText.text.toString().trim().ifEmpty { "Student" }
        val totalQuestions = questions.size
        val percentage = if (totalQuestions > 0) ((correct.toFloat() / totalQuestions.toFloat()) * 100f) else 0f
        val accuracy = if ((correct + wrong) > 0) ((correct.toFloat() / (correct + wrong).toFloat()) * 100f) else 0f
        val totalSeconds = ((SystemClock.elapsedRealtime() - quizStartTimeMs) / 1000L)
        val averageSpeed = if (answeredCount > 0) (totalSeconds.toFloat() / answeredCount.toFloat()) else 0f

        quizLayout.visibility = View.GONE
        resultLayout.visibility = View.VISIBLE

        resultStudentTextView.text = "Student: $studentName"
        resultTotalQuestionsTextView.text = "Total Questions: $totalQuestions"
        resultCorrectTextView.text = "Correct: $correct"
        resultWrongTextView.text = "Wrong Questions: $wrong"
        resultWrongAttemptsTextView.text = "Wrong Attempts: $wrongAttempts"
        resultSkippedTextView.text = "Skipped: $skipped"
        resultScoreTextView.text = "Score: $score"
        resultPercentageTextView.text = "Percentage: ${"%.2f".format(percentage)}%"
        resultAccuracyTextView.text = "Accuracy: ${"%.2f".format(accuracy)}%"
        resultTotalTimeTextView.text = "Total Time: ${totalSeconds}s"
        resultAverageSpeedTextView.text = "Average Speed / question: ${"%.2f".format(averageSpeed)}s"
    }

    private fun restartQuiz() {
        handler.removeCallbacksAndMessages(null)
        currentQuestionIndex = 0
        correct = 0
        wrong = 0
        wrongAttempts = 0
        skipped = 0
        score = 0
        answeredCount = 0
        totalTimeMs = 0L
        quizStartTimeMs = SystemClock.elapsedRealtime()
        nextButton.isEnabled = false
        resultLayout.visibility = View.GONE
        quizLayout.visibility = View.VISIBLE
        showQuestion()
    }
}

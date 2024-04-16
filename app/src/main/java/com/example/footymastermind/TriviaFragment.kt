package com.example.footymastermind

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.footymastermind.databinding.FragmentTriviaBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlin.random.Random

// eliminare buton de next si buton de finish
// timp total nu per intrebare
// la raspuns gresit se scad 5 secunde din timpul total
// la raspuns corect se aduna 2 secunde la timpul total
// dupa raspuns se asteapta 1 secunda pana sa se treaca la urmatoarea intrebare
// la results sa afisez leaderboard

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TriviaFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TriviaFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var triviaBinding: FragmentTriviaBinding

    val database =
        FirebaseDatabase.getInstance("https://footymastermindapp-default-rtdb.europe-west1.firebasedatabase.app/")
    val databaseReference = database.reference.child("questions")

    var question = ""
    var answerA = ""
    var answerB = ""
    var answerC = ""
    var answerD = ""
    var correctAnswer = ""
    var questionCount = 0
    var questionNumber = 0

    var userAnswer = ""
    var userCorrect = 0
    var userWrong = 0

    lateinit var timer : CountDownTimer
    private val totalTime = 60000L
    var timerContinue = false
    var leftTime = totalTime

    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val scoreRef = database.reference

    val questions = HashSet<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

//        triviaBinding = FragmentTriviaBinding.inflate(layoutInflater)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        triviaBinding = FragmentTriviaBinding.inflate(inflater, container, false)
        return triviaBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        do {
            val number = Random.nextInt(1,31)
            Log.d("number", number.toString())
            questions.add(number)
        }while(questions.size < 10)


        Log.d("numberOfQuestions", questions.toString())

        gameLogic()

        triviaBinding.buttonNext.setOnClickListener {
            resetTimer()
            gameLogic()
        }

        triviaBinding.buttonFinish.setOnClickListener {
            sendScore()
        }

        triviaBinding.textViewA.setOnClickListener {

            pauseTimer()
            userAnswer = "a"
            if (correctAnswer == userAnswer) {
                triviaBinding.textViewA.setBackgroundColor(Color.GREEN)
                userCorrect++
                triviaBinding.textViewCorrect.text = userCorrect.toString()
            } else {
                triviaBinding.textViewA.setBackgroundColor(Color.RED)
                userWrong++
                triviaBinding.textViewWrong.text = userWrong.toString()
                findAnswer()
            }
            disableClickableOfOptions()
        }

        triviaBinding.textViewB.setOnClickListener {

            pauseTimer()
            userAnswer = "b"
            if (correctAnswer == userAnswer) {
                triviaBinding.textViewB.setBackgroundColor(Color.GREEN)
                userCorrect++
                triviaBinding.textViewCorrect.text = userCorrect.toString()
            } else {
                triviaBinding.textViewB.setBackgroundColor(Color.RED)
                userWrong++
                triviaBinding.textViewWrong.text = userWrong.toString()
                findAnswer()
            }
            disableClickableOfOptions()
        }

        triviaBinding.textViewC.setOnClickListener {

            pauseTimer()
            userAnswer = "c"
            if (correctAnswer == userAnswer) {
                triviaBinding.textViewC.setBackgroundColor(Color.GREEN)
                userCorrect++
                triviaBinding.textViewCorrect.text = userCorrect.toString()
            } else {
                triviaBinding.textViewC.setBackgroundColor(Color.RED)
                userWrong++
                triviaBinding.textViewWrong.text = userWrong.toString()
                findAnswer()
            }
            disableClickableOfOptions()
        }

        triviaBinding.textViewD.setOnClickListener {

            pauseTimer()
            userAnswer = "d"
            if (correctAnswer == userAnswer) {
                triviaBinding.textViewD.setBackgroundColor(Color.GREEN)
                userCorrect++
                triviaBinding.textViewCorrect.text = userCorrect.toString()
            } else {
                triviaBinding.textViewD.setBackgroundColor(Color.RED)
                userWrong++
                triviaBinding.textViewWrong.text = userWrong.toString()
                findAnswer()
            }
            disableClickableOfOptions()
        }

    }


    private fun gameLogic() {

        restoreOptions()

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                questionCount = snapshot.childrenCount.toInt()

                if (questionNumber < questions.size) {

                    question =
                        snapshot.child(questions.elementAt(questionNumber).toString()).child("question").value.toString()
                    answerA = snapshot.child(questions.elementAt(questionNumber).toString()).child("a").value.toString()
                    answerB = snapshot.child(questions.elementAt(questionNumber).toString()).child("b").value.toString()
                    answerC = snapshot.child(questions.elementAt(questionNumber).toString()).child("c").value.toString()
                    answerD = snapshot.child(questions.elementAt(questionNumber).toString()).child("d").value.toString()
                    correctAnswer =
                        snapshot.child(questions.elementAt(questionNumber).toString()).child("answer").value.toString()

                    triviaBinding.textViewQuestion.text = question
                    triviaBinding.textViewA.text = answerA
                    triviaBinding.textViewB.text = answerB
                    triviaBinding.textViewC.text = answerC
                    triviaBinding.textViewD.text = answerD

                    triviaBinding.progressBarQuiz.visibility = View.INVISIBLE
                    triviaBinding.linearLayoutInfo.visibility = View.VISIBLE
                    triviaBinding.linearLayoutQuestion.visibility = View.VISIBLE
                    triviaBinding.linearLayoutButtons.visibility = View.VISIBLE

                    startTimer()

                } else {

                    val dialogMessage = AlertDialog.Builder(requireContext())
                    dialogMessage.setTitle("Trivia")
                    dialogMessage.setMessage("Game over!\nDo you want to see the results?")
                    dialogMessage.setCancelable(false)
                    dialogMessage.setPositiveButton("See Result"){ dialogWindow,position ->
                        sendScore()
                    }
                    dialogMessage.setNegativeButton("Play Again"){dialogWindow,position ->
                        val intent = Intent(requireActivity(), TriviaFragment::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    dialogMessage.create().show()

                }

                questionNumber++

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, error.message, Toast.LENGTH_SHORT).show()
            }
        })

    }

    fun findAnswer() {
        when(correctAnswer){
            "a" -> triviaBinding.textViewA.setBackgroundColor(Color.GREEN)
            "b" -> triviaBinding.textViewB.setBackgroundColor(Color.GREEN)
            "c" -> triviaBinding.textViewC.setBackgroundColor(Color.GREEN)
            "d" -> triviaBinding.textViewD.setBackgroundColor(Color.GREEN)
        }

    }

    fun disableClickableOfOptions(){
        triviaBinding.textViewA.isClickable = false
        triviaBinding.textViewB.isClickable = false
        triviaBinding.textViewC.isClickable = false
        triviaBinding.textViewD.isClickable = false
    }

    fun restoreOptions(){
        triviaBinding.textViewA.setBackgroundColor(Color.WHITE)
        triviaBinding.textViewB.setBackgroundColor(Color.WHITE)
        triviaBinding.textViewC.setBackgroundColor(Color.WHITE)
        triviaBinding.textViewD.setBackgroundColor(Color.WHITE)

        triviaBinding.textViewA.isClickable = true
        triviaBinding.textViewB.isClickable = true
        triviaBinding.textViewC.isClickable = true
        triviaBinding.textViewD.isClickable = true
    }

    private fun startTimer(){
        timer = object : CountDownTimer(leftTime, 1000){
            override fun onTick(millisUntilFinished: Long) {
                leftTime = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {

                disableClickableOfOptions()
                resetTimer()
                updateCountDownText()
                triviaBinding.textViewQuestion.text = "Times's up! Next question."
                timerContinue = false
            }

        }.start()

        timerContinue = true
    }

    fun updateCountDownText(){
        val remainingTime : Int = (leftTime/1000).toInt()
        triviaBinding.textViewTime.text = remainingTime.toString()
    }

    fun pauseTimer(){
        timer.cancel()
        timerContinue = false
    }

    fun resetTimer(){
        pauseTimer()
        leftTime = totalTime
        updateCountDownText()
    }

    fun sendScore(){
        user?.let {
            val userUID = it.uid
            scoreRef.child("scores").child(userUID).child("correct").setValue(userCorrect)
            scoreRef.child("scores").child(userUID).child("wrong").setValue(userWrong).addOnSuccessListener {
                Toast.makeText(activity, "Scores sent to database succesfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireActivity(), ResultActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TriviaFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TriviaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
package com.example.footymastermind

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.footymastermind.databinding.FragmentTenaballBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.random.Random

// TODO: Rename parameter arguments, choose names that match
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TenaballFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TenaballFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var tenaballBinding: FragmentTenaballBinding
    val database =
        FirebaseDatabase.getInstance("https://footymastermindapp-default-rtdb.europe-west1.firebasedatabase.app/")
    val databaseReference = database.reference.child("tenable")
    var question = ""
    var answer1 = ""
    var answer2 = ""
    var answer3 = ""
    var answer4 = ""
    var answer5 = ""
    var answer6 = ""
    var answer7 = ""
    var answer8 = ""
    var answer9 = ""
    var answer10 = ""
    var questionNumber = 0
    var correctAnswerCount = 0
    var wrongAnswerCount = 0
    val maxWrongAttempts = 3

    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        tenaballBinding = FragmentTenaballBinding.inflate(inflater, container, false)
        return tenaballBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tenaballBinding.submitButton.setOnClickListener {
            checkAnswer()
        }
        questionNumber = Random.nextInt(1, 11)
        gameLogic()
    }

    private fun gameLogic() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                question =
                    snapshot.child(questionNumber.toString()).child("question").value.toString()
                answer1 = snapshot.child(questionNumber.toString()).child("1").value.toString()
                answer2 = snapshot.child(questionNumber.toString()).child("2").value.toString()
                answer3 = snapshot.child(questionNumber.toString()).child("3").value.toString()
                answer4 = snapshot.child(questionNumber.toString()).child("4").value.toString()
                answer5 = snapshot.child(questionNumber.toString()).child("5").value.toString()
                answer6 = snapshot.child(questionNumber.toString()).child("6").value.toString()
                answer7 = snapshot.child(questionNumber.toString()).child("7").value.toString()
                answer8 = snapshot.child(questionNumber.toString()).child("8").value.toString()
                answer9 = snapshot.child(questionNumber.toString()).child("9").value.toString()
                answer10 = snapshot.child(questionNumber.toString()).child("10").value.toString()

                tenaballBinding.textViewQuestion.text = question
                tenaballBinding.textView1.text = answer1
                tenaballBinding.textView2.text = answer2
                tenaballBinding.textView3.text = answer3
                tenaballBinding.textView4.text = answer4
                tenaballBinding.textView5.text = answer5
                tenaballBinding.textView6.text = answer6
                tenaballBinding.textView7.text = answer7
                tenaballBinding.textView8.text = answer8
                tenaballBinding.textView9.text = answer9
                tenaballBinding.textView10.text = answer10

                tenaballBinding.progressBarTenable.visibility = View.INVISIBLE
                tenaballBinding.responseLayout.visibility = View.VISIBLE
                tenaballBinding.linearLayoutQuestion.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkAnswer() {
        val userAnswer = tenaballBinding.textResponse.text.toString().trim()
        if (userAnswer.length < 4) {
            Toast.makeText(activity, "Answer should be at least 4 letters long", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val correctAnswerIndex = (1..10).firstOrNull {
            val answer = when (it) {
                1 -> answer1
                2 -> answer2
                3 -> answer3
                4 -> answer4
                5 -> answer5
                6 -> answer6
                7 -> answer7
                8 -> answer8
                9 -> answer9
                10 -> answer10
                else -> ""
            }
            answer.contains(userAnswer, ignoreCase = true)
        }

        if (correctAnswerIndex != null) {
            correctAnswerCount++
            val textView = when (correctAnswerIndex) {
                1 -> tenaballBinding.textView1
                2 -> tenaballBinding.textView2
                3 -> tenaballBinding.textView3
                4 -> tenaballBinding.textView4
                5 -> tenaballBinding.textView5
                6 -> tenaballBinding.textView6
                7 -> tenaballBinding.textView7
                8 -> tenaballBinding.textView8
                9 -> tenaballBinding.textView9
                10 -> tenaballBinding.textView10
                else -> null
            }
            textView?.apply {
                setBackgroundColor(Color.GREEN)
                setTextColor(Color.WHITE)
                animate().apply {
                    scaleX(1.2f)
                    scaleY(1.2f)
                    duration = 200
                    withEndAction {
                        animate().apply {
                            scaleX(1f)
                            scaleY(1f)
                            duration = 200
                        }.start()
                    }
                }.start()
                Toast.makeText(activity, "Correct Answer!", Toast.LENGTH_SHORT).show()
            }
        } else {
            wrongAnswerCount++
            if (wrongAnswerCount >= maxWrongAttempts) {
                endGame()
            } else {
                removeLife()
                Toast.makeText(activity, "Incorrect Answer!", Toast.LENGTH_SHORT).show()
            }
        }
        tenaballBinding.textResponse.text.clear()
    }

    private fun removeLife() {
        when (wrongAnswerCount) {
            1 -> tenaballBinding.lifeA.visibility = View.INVISIBLE
            2 -> tenaballBinding.lifeB.visibility = View.INVISIBLE
            3 -> {
                tenaballBinding.lifeC.visibility = View.INVISIBLE
                endGame()
            }
        }
    }

    private fun endGame() {
        if (correctAnswerCount >= 8) {

            val intent = Intent(requireActivity(), ResultActivity::class.java)
            startActivity(intent)
        } else {

            val intent = Intent(requireActivity(), NotResultActivity::class.java)
            startActivity(intent)
        }
        requireActivity().finish()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TenaballFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TenaballFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
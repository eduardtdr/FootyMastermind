package com.example.footymastermind

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.footymastermind.databinding.FragmentGuessWhoBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.random.Random
import kotlin.random.nextInt

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GuessWhoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GuessWhoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var guessWhoBinding: FragmentGuessWhoBinding


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
        // Inflate the layout for this fragment
        guessWhoBinding = FragmentGuessWhoBinding.inflate(inflater, container, false)
        return guessWhoBinding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        guessWhoBinding.playButton.setOnClickListener{
            createGame()
        }

        guessWhoBinding.joinButton.setOnClickListener{
            joinGame()
        }
    }

    fun createGame(){
        GuessWhoData.myID = "Red"
        GuessWhoData.saveGuessWhoModel(
            GuessWhoModel(
                gameStatus = GuessGameStatus.CREATED,
                gameId = Random.nextInt(1000..9000).toString()
            )
        )
        startGame()
    }

    fun joinGame(){
        var gameId = guessWhoBinding.gameIdInput.text.toString()
        if(gameId.isEmpty()){
            guessWhoBinding.gameIdInput.setError("Enter game ID")
            return
        }
        GuessWhoData.myID = "Green"
        Firebase.firestore.collection("guess_who_games").document(gameId).get().addOnSuccessListener {
            val model = it?.toObject(GuessWhoModel::class.java)
            if(model==null){
                guessWhoBinding.gameIdInput.setError("Enter valid game ID")
            }else{
                model.gameStatus = GuessGameStatus.JOINED
                GuessWhoData.saveGuessWhoModel(model)
                startGame()
            }
        }
    }

    fun startGame(){
        startActivity(Intent(requireActivity(), GuessWhoStart::class.java))
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GuessWhoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GuessWhoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
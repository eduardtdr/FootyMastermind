package com.example.footymastermind

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footymastermind.databinding.FragmentHomeBinding
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

interface RecyclerViewInterface {
    fun onItemClick(position: Int)
}

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), RecyclerViewInterface {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var homeBinding: FragmentHomeBinding

    val gameModels: ArrayList<GameModel> = ArrayList()
    lateinit var recyclerView: RecyclerView

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
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return homeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.mRecyclerView)

        setUpGameModels()

        val adapter = Game_RecyclerViewAdapter(requireContext(), gameModels, this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

    }

    private fun setUpGameModels() {
        gameModels.add(
            GameModel(
                getString(R.string.guess_who),
                getString(R.string.multiplayer_games),
                getString(R.string.guess_who_description),
                R.drawable.baseline_psychology_alt_24
            )
        )
        gameModels.add(
            GameModel(
                getString(R.string.tic_tac_toe),
                getString(R.string.multiplayer_games),
                getString(R.string.tic_tac_toe_description),
                R.drawable.baseline_handshake_24
            )
        )
        gameModels.add(
            GameModel(
                getString(R.string.football_trivia),
                getString(R.string.single_player_games),
                getString(R.string.football_trivia_description),
                R.drawable.baseline_timer_24
            )
        )
        gameModels.add(
            GameModel(
                getString(R.string.tenaball),
                getString(R.string.single_player_games),
                getString(R.string.tenaball_description),
                R.drawable.sharp_keyboard_capslock_24
            )
        )
        gameModels.add(
            GameModel(
                getString(R.string.dream_team),
                getString(R.string.single_player_games),
                getString(R.string.dream_team_description),
                R.drawable.baseline_local_fire_department_24
            )
        )
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TicTacToeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(requireActivity(), DescriptionActivity::class.java)
        intent.putExtra("NAME", gameModels.get(position).getGameName())
        intent.putExtra("CATEGORY", gameModels.get(position).getGameCategory())
        intent.putExtra("DESCRIPTION", gameModels.get(position).getGameDescription())
        intent.putExtra("IMAGE", gameModels.get(position).getImage())
        startActivity(intent)
    }
}
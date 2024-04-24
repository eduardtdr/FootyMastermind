package com.example.footymastermind

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.LinearLayout
import com.example.footymastermind.databinding.FragmentDreamTeamBinding
import com.example.footymastermind.databinding.FragmentTicTacToeBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DreamTeamFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DreamTeamFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var dreamTeamBinding: FragmentDreamTeamBinding

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
        dreamTeamBinding = FragmentDreamTeamBinding.inflate(inflater, container, false)
        return dreamTeamBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dreamTeamBinding.configSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                displayButtonsBasedOnSelection()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun displayButtonsBasedOnSelection() {
        // Get the selected value from the spinner
        val selectedValue = dreamTeamBinding.configSpinner.selectedItem as String

        // Convert the selected value to a list of integers
        val buttonCounts = selectedValue.split("-").map { it.toInt() }

        // Clear the existing buttons from the container layout
        dreamTeamBinding.buttonsContainer.removeAllViews()

        // Calculate total number of buttons
        val totalButtons = buttonCounts.sum()

        // Calculate number of buttons in each row
        val rows = buttonCounts.size

        // Calculate the number of buttons in the last row
        val buttonsInLastRow = totalButtons % rows

        // Calculate the size of each button (assuming equal width and height)
        val scale: Float = resources.displayMetrics.density
        val buttonSizeInPixels = (48 * scale + 0.5f).toInt()

        val buttonSize = buttonSizeInPixels

        val totalWidth = buttonSize * buttonsInLastRow

        // Add buttons rows dynamically
        var startIndex = 0
        for (count in buttonCounts) {
            val endIndex = startIndex + count
            val rowLayout = LinearLayout(requireContext())
            rowLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowLayout.orientation = LinearLayout.HORIZONTAL

            for (i in startIndex until endIndex) {
                val button = Button(requireContext())
                button.text = "Button ${i + 1}"
                button.layoutParams = LinearLayout.LayoutParams(buttonSize, buttonSize)
                rowLayout.addView(button)
            }

            dreamTeamBinding.buttonsContainer.addView(rowLayout)

            startIndex = endIndex
        }

    }

        companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DreamTeamFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DreamTeamFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
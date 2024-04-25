package com.example.footymastermind

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.compose.ui.graphics.Color
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
        val selectedValue = dreamTeamBinding.configSpinner.selectedItem as String
        val formation = "1-$selectedValue"
        val buttonCounts = formation.split("-").map { it.toInt() }

        dreamTeamBinding.buttonsContainer.removeAllViews()

        val totalButtons = buttonCounts.sum()
        val buttonSize = convertDpToPx(80f)
        val marginBetweenButtons = convertDpToPx(10f)
        val marginBetweenRows = convertDpToPx(100f)


        var startIndex = 0
        var positionIndex = 0

        for (count in buttonCounts.reversed()) {
            val endIndex = startIndex + count
            val rowLayout = LinearLayout(requireContext())
            rowLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowLayout.gravity = Gravity.CENTER_HORIZONTAL

            val positions = arrayOf("FW", "MF", "DF", "GK")

            val rowPositions = positions[positionIndex]

            for (i in (endIndex - 1) downTo startIndex) {
                val button = Button(requireContext())
                button.text = rowPositions
                val params = LinearLayout.LayoutParams(buttonSize, buttonSize)

                params.marginEnd = marginBetweenButtons
                button.layoutParams = params
                button.setBackgroundResource(R.drawable.button_tshirt_sleeve)
                button.setOnClickListener {
                    showPromptAndUpdateButton(button)
                }
                rowLayout.addView(button)
            }

            dreamTeamBinding.buttonsContainer.addView(rowLayout)

            startIndex = endIndex

            positionIndex = (positionIndex + 1) % positions.size


            if (startIndex != buttonCounts.size) {
                val rowLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                rowLayoutParams.topMargin = marginBetweenRows
                rowLayout.layoutParams = rowLayoutParams
            }
        }

    }

    private fun convertDpToPx(dp: Float): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun showPromptAndUpdateButton(button: Button) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_name_number, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val numberEditText = dialogView.findViewById<EditText>(R.id.numberEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("Enter Name and Number")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                val name = nameEditText.text.toString()
                val number = numberEditText.text.toString()


                button.text = "$name\n$number"

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
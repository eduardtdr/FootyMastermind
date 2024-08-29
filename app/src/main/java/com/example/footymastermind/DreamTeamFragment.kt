package com.example.footymastermind

import android.app.AlertDialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.request.transition.Transition
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.footymastermind.databinding.FragmentDreamTeamBinding
import com.example.footymastermind.databinding.FragmentTicTacToeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
    // Declare the selectedPlayers list to keep track of already selected players
    private val selectedPlayers = mutableListOf<String>()

    private var param1: String? = null
    private var param2: String? = null
    // Declare the score variable
    private var score = 0

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
                val buttonLayout = LinearLayout(requireContext())
                buttonLayout.orientation = LinearLayout.VERTICAL
                buttonLayout.gravity = Gravity.CENTER

                val button = Button(requireContext())
                button.text = rowPositions
                button.textSize = 0f
                val params = LinearLayout.LayoutParams(buttonSize, buttonSize)
                params.marginEnd = marginBetweenButtons
                button.layoutParams = params
                button.setBackgroundResource(R.drawable.button_tshirt_sleeve)
                button.setOnClickListener {
                    showPromptAndUpdateButton(button, rowPositions, buttonLayout)
                }

                // Add button to the layout
                buttonLayout.addView(button)

                // Add the layout to the row
                rowLayout.addView(buttonLayout)
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

        // Reset score when the formation changes
        score = 0
        updateScoreText()
    }

    private fun convertDpToPx(dp: Float): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun showPromptAndUpdateButton(button: Button, rowPosition: String, buttonLayout: LinearLayout) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            return
        }

        // Reference to the user's owned players in Firebase
        val database = FirebaseDatabase.getInstance("https://footymastermindapp-default-rtdb.europe-west1.firebasedatabase.app/")
        val databaseRef = database.reference.child("users/$userId/ownedPlayers")

        // Fetch the owned players
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = mutableListOf<String>()
                for (playerSnapshot in snapshot.children) {
                    val playerName = playerSnapshot.key
                    players.add(playerName!!)
                }

                // Filter out already selected players
                val unselectedPlayers = players.filterNot { selectedPlayers.contains(it) }

                // If there are no players left to select, show an appropriate message
                if (unselectedPlayers.isEmpty()) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("No Players Available")
                        .setMessage("All players have been selected.")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .show()
                    return
                }

                // Create a dialog with the list of unselected players
                val playerNamesArray = unselectedPlayers.toTypedArray()
                AlertDialog.Builder(requireContext())
                    .setTitle("Select a Player")
                    .setItems(playerNamesArray) { dialog, which ->
                        val selectedPlayerName = playerNamesArray[which]

                        // Add the selected player to the list of selected players
                        selectedPlayers.add(selectedPlayerName)

                        // Fetch selected player details from Firebase
                        val selectedPlayerSnapshot = snapshot.child(selectedPlayerName)

                        val playerImage = selectedPlayerSnapshot.child("image").getValue(String::class.java)
                        val playerOverall = selectedPlayerSnapshot.child("overall").getValue(Int::class.java) ?: 0

                        // Extract the last name of the player
                        val lastName = selectedPlayerName.split(" ").last()

                        // Add points based on the overall and position match
                        var pointsToAdd = playerOverall
                        if (button.text == rowPosition) {
                            pointsToAdd += 10
                        }
                        score += pointsToAdd
                        updateScoreText()


                        // Update button with player image
                        if (!playerImage.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(playerImage)
                                .into(object : CustomTarget<Drawable>() {
                                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                        button.background = resource
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {
                                        // Handle cleanup if needed
                                    }
                                })
                        }

                        // Add a TextView below the button for the player's last name
                        val nameTextView = TextView(requireContext())
                        nameTextView.text = lastName
                        nameTextView.gravity = Gravity.CENTER
                        nameTextView.setTextColor(android.graphics.Color.WHITE)
                        buttonLayout.addView(nameTextView)

                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors
            }
        })
    }

    private fun updateScoreText() {
        dreamTeamBinding.scoreTextView.text = "Score: $score"
    }

    companion object {
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

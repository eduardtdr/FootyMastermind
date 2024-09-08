package com.example.footymastermind

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.footymastermind.databinding.FragmentDreamTeamBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DreamTeamFragment : Fragment() {

    private val selectedPlayers = mutableListOf<String>()
    private var score = 0
    lateinit var dreamTeamBinding: FragmentDreamTeamBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dreamTeamBinding = FragmentDreamTeamBinding.inflate(inflater, container, false)
        return dreamTeamBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        dreamTeamBinding.exitButton.setOnClickListener {

            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }


        dreamTeamBinding.sendScoreButton.setOnClickListener {
            val userEmail = FirebaseAuth.getInstance().currentUser?.email
            if (userEmail != null) {

                val playerName = userEmail.substringBefore("@")
                updateScoreInFirebase(playerName)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Failed to retrieve user email",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        dreamTeamBinding.configSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    displayButtonsBasedOnSelection()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

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

                buttonLayout.addView(button)
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

        score = 0
        updateScoreText()
    }

    private fun convertDpToPx(dp: Float): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun showPromptAndUpdateButton(
        button: Button,
        rowPosition: String,
        buttonLayout: LinearLayout
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            return
        }

        val database =
            FirebaseDatabase.getInstance("https://footymastermindapp-default-rtdb.europe-west1.firebasedatabase.app/")
        val databaseRef = database.reference.child("users/$userId/ownedPlayers")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = mutableListOf<String>()
                for (playerSnapshot in snapshot.children) {
                    val playerName = playerSnapshot.key
                    players.add(playerName!!)
                }

                val unselectedPlayers = players.filterNot { selectedPlayers.contains(it) }

                if (unselectedPlayers.isEmpty()) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("No Players Available")
                        .setMessage("All players have been selected.")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .show()
                    return
                }

                val playerNamesArray = unselectedPlayers.toTypedArray()
                AlertDialog.Builder(requireContext())
                    .setTitle("Select a Player")
                    .setItems(playerNamesArray) { dialog, which ->
                        val selectedPlayerName = playerNamesArray[which]

                        selectedPlayers.add(selectedPlayerName)

                        val selectedPlayerSnapshot = snapshot.child(selectedPlayerName)

                        val playerImage =
                            selectedPlayerSnapshot.child("image").getValue(String::class.java)
                        val playerOverall =
                            selectedPlayerSnapshot.child("overall").getValue(Int::class.java) ?: 0

                        val lastName = selectedPlayerName.split(" ").last()

                        var pointsToAdd = playerOverall
                        if (button.text == rowPosition) {
                            pointsToAdd += 10
                        }
                        score += pointsToAdd
                        updateScoreText()

                        if (!playerImage.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(playerImage)
                                .into(object : CustomTarget<Drawable>() {
                                    override fun onResourceReady(
                                        resource: Drawable,
                                        transition: Transition<in Drawable>?
                                    ) {
                                        button.background = resource
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {
                                    }
                                })
                        }

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
            }
        })
    }

    private fun updateScoreText() {
        dreamTeamBinding.scoreTextView.text = "Score: $score"
    }

    private fun updateScoreInFirebase(playerName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            return
        }

        val database =
            FirebaseDatabase.getInstance("https://footymastermindapp-default-rtdb.europe-west1.firebasedatabase.app/")
        val scoresRef = database.reference.child("scores/$userId")

        val scoreData = mapOf(
            "name" to playerName,
            "correct" to score
        )

        scoresRef.setValue(scoreData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Score updated successfully!", Toast.LENGTH_SHORT)
                    .show()

                val intent = Intent(requireActivity(), LeaderboardActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "Failed to update score.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
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

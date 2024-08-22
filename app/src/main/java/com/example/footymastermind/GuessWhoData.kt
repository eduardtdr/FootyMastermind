package com.example.footymastermind

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
object GuessWhoData {
    private var _guessWhoModel : MutableLiveData<GuessWhoModel> = MutableLiveData()
    var guessWhoModel : LiveData<GuessWhoModel> = _guessWhoModel
    var myID = ""

    fun saveGuessWhoModel(model : GuessWhoModel){
        _guessWhoModel.postValue(model)
        if(model.gameId != "-1") {
            Firebase.firestore.collection("guess_who_games").document(model.gameId).set(model)
        }
    }

    fun fetchGuessWhoModel() {
        val currentModel = guessWhoModel.value
        if (currentModel != null && currentModel.gameId != "-1") {
            Firebase.firestore.collection("guess_who_games").document(currentModel.gameId)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        // Handle the error appropriately
                        Log.e("GuessWhoData", "Error fetching document", error)
                        return@addSnapshotListener
                    }

                    val model = value?.toObject(GuessWhoModel::class.java)
                    if (model != null) {
                        _guessWhoModel.postValue(model!!)
                    } else {
                        // Handle the case where the document doesn't exist
                        Log.w("GuessWhoData", "Document does not exist")
                    }
                }
        } else {
            // Handle the case where the model is null or gameId is invalid
            Log.w("GuessWhoData", "Invalid gameId or model is null")
        }
    }


}
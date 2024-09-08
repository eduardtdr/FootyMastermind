package com.example.footymastermind

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object TicTacToeData {
    private var _ticTacToeModel: MutableLiveData<TicTacToeModel> = MutableLiveData()
    var ticTacToeModel: LiveData<TicTacToeModel> = _ticTacToeModel
    var myID = ""

    fun saveTicTacToeModel(model: TicTacToeModel) {
        _ticTacToeModel.postValue(model)
        if (model.gameId != "-1") {
            Firebase.firestore.collection("tic_tac_toe_games").document(model.gameId).set(model)
        }
    }

    fun fetchTicTacToeModel() {
        val currentModel = ticTacToeModel.value
        if (currentModel != null && currentModel.gameId != "-1") {
            Firebase.firestore.collection("tic_tac_toe_games").document(currentModel.gameId)
                .addSnapshotListener { value, error ->
                    if (error != null) {

                        Log.e("TicTacToeData", "Error fetching document", error)
                        return@addSnapshotListener
                    }

                    val model = value?.toObject(TicTacToeModel::class.java)
                    if (model != null) {
                        _ticTacToeModel.postValue(model!!)
                    } else {

                        Log.w("TicTacToeData", "Document does not exist")
                    }
                }
        } else {

            Log.w("TicTacToeData", "Invalid gameId or model is null")
        }
    }


}
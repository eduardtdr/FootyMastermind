package com.example.footymastermind

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object TicTacToeData {
    private var _ticTacToeModel : MutableLiveData<TicTacToeModel> = MutableLiveData()
    var ticTacToeModel : LiveData<TicTacToeModel> = _ticTacToeModel
    var myID = ""

    fun saveTicTacToeModel(model : TicTacToeModel){
        _ticTacToeModel.postValue(model)
        if(model.gameId != "-1") {
            Firebase.firestore.collection("tic_tac_toe_games").document(model.gameId).set(model)
        }
    }

    fun fetchTicTacToeModel(){
        ticTacToeModel.value?.apply {
            if(gameId != "-1") {
                Firebase.firestore.collection("tic_tac_toe_games").document(gameId)
                    .addSnapshotListener { value, error ->
                        val model = value?.toObject(TicTacToeModel::class.java)
                        _ticTacToeModel.postValue(model!!)
                }
            }
        }
    }

}
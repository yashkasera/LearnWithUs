package com.yashkasera.learnwithus.ui.narration

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashkasera.learnwithus.repository.Repository
import kotlinx.coroutines.launch

class NarrationViewModel : ViewModel() {
    private val repository = Repository()
    val isListening = ObservableBoolean(false)
    val sounds: MutableLiveData<Map<String, String>> = MutableLiveData()
    fun getSounds(text: String) {
        viewModelScope.launch {
            try {
                val res = repository.getSounds(text)
                if (res.isSuccessful) {
                    res.body()?.let {
                        sounds.value = it
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getSound(text: String) {
        viewModelScope.launch {
            try {
                val res = repository.getSound(text)
                if (res.isSuccessful) {
                    res.body()?.let {
                       sounds.value = it
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
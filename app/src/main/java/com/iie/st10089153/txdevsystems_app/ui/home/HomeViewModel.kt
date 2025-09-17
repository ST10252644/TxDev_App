package com.iie.st10089153.txdevsystems_app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel



class HomeViewModel : ViewModel() {

    private val _greetingText = MutableLiveData<String>().apply {
        value = "Hello User"
    }
    val greetingText: LiveData<String> = _greetingText

    private val _subtitleText = MutableLiveData<String>().apply {
        value = "The following devices are on your account"
    }
    val subtitleText: LiveData<String> = _subtitleText

    private val _isLoading = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isLoading: LiveData<Boolean> = _isLoading

    fun updateGreeting(username: String) {
        _greetingText.value = "Hello $username"
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
}
package com.iie.st10089153.txdevsystems_app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _helloText = MutableLiveData<String>().apply {
        value = "Hello Person Nathan"
    }
    val helloText: LiveData<String> = _helloText

    private val _subtitleText = MutableLiveData<String>().apply {
        value = "The following devices are active on your account."
    }
    val subtitleText: LiveData<String> = _subtitleText
}
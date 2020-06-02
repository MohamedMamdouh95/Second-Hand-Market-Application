package com.example.lab2.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {
    val latitude = MutableLiveData<String>()
    val longitude = MutableLiveData<String>()
    val locationString = MutableLiveData<String>()


}
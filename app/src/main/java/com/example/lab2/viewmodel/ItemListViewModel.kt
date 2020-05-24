package com.example.lab2.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.model.Item
import com.example.lab2.model.repository.ItemRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ItemListViewModel : ViewModel() {

    private val repository = ItemRepository()

    val itemsOnSale: MutableLiveData<List<Item>>
            by lazy { repository.getAllItemsExceptOneUser(Firebase.auth.currentUser!!.uid) }

    val ownItems: MutableLiveData<List<Item>>
            by lazy { repository.getOwnItems() }

    fun getItemImageAsBitmap(uri: String): LiveData<Bitmap> {
        return repository.getItemImageAsBitmap(uri)
    }
}

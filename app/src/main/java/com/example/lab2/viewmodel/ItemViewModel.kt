package com.example.lab2.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.lab2.model.Item
import com.example.lab2.model.Profile
import com.example.lab2.model.repository.DocumentWriteResult
import com.example.lab2.model.repository.ItemRepository

class ItemViewModel : ViewModel() {
    private val repository = ItemRepository()


    private val itemIdLiveData = MutableLiveData<String?>()

    val detailItem: LiveData<Item> = Transformations.switchMap(itemIdLiveData) { itemId ->
        repository.getItem(itemId)
    }
    val interestedBuyers: LiveData<List<Profile>> = Transformations.switchMap(itemIdLiveData) { itemId ->
        repository.getItemBuyers(itemId)
    }

    private val imageStoragePathLiveData = MutableLiveData<String?>()

    val bitmap: LiveData<Bitmap> =
        Transformations.switchMap(imageStoragePathLiveData) { imageStoragePath ->
            repository.getItemImageAsBitmap(imageStoragePath)
        }

    val newImageBitmap = MutableLiveData<Bitmap>()


    fun setImageStoragePath(path: String?) {
        imageStoragePathLiveData.value = path
    }

    fun setItemId(itemId: String?) {
        itemIdLiveData.value = itemId
    }

    fun createItem(item: Item, bm: Bitmap?): LiveData<DocumentWriteResult> {
        return repository.createItem(item, bm)
    }

    fun updateItem(item: Item, bm: Bitmap?): LiveData<DocumentWriteResult> {
        return repository.updateItem(item, bm)
    }
}
package com.example.lab2.viewmodel

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.lab2.model.Item
import com.example.lab2.model.Profile
import com.example.lab2.model.repository.DocumentWriteResult
import com.example.lab2.model.repository.UserRepository

class UserViewModel: ViewModel() {

    private val repository = UserRepository()
    private val imageStoragePathLiveData = MutableLiveData<String?>()
    private val userIdLiveData = MutableLiveData<String>()
    val newImageBitmap = MutableLiveData<Bitmap>()

    val bitmap: LiveData<Bitmap> = Transformations.switchMap(imageStoragePathLiveData) { imageStoragePath ->
        repository.getUserImageAsBitmap(imageStoragePath)
    }
    val wishlist : LiveData<List<Item>> by lazy { repository.getWishlist() }

    val detailUser: LiveData<Profile> = Transformations.switchMap(userIdLiveData) {
            userId ->
        repository.getUser(userId)
    }

    fun setImageStoragePath(path: String?) {
        imageStoragePathLiveData.value = path
    }

    fun setUserId(userId: String?) {
        userId?.let {  userIdLiveData.value = it }
    }

    fun getUser(userId: String): LiveData<Profile>?{
        return repository.getUser(userId)
    }

    fun createOrUpdateUser(profile: Profile){
        repository.createOrUpdateUser(profile).addOnFailureListener{
            Log.e(TAG, "Creazione utente fallita")
        }
    }

    fun createUser(user: Profile, bm: Bitmap?) : LiveData<DocumentWriteResult> {
        return repository.createUser(user, bm)
    }

    fun updateUser(user: Profile, bm: Bitmap?) : LiveData<DocumentWriteResult> {
        return repository.updateUser(user, bm)
    }

    fun addItemToWishlist(itemId: String) {
        repository.addItemToWishlist(itemId)
    }

    fun removeItemFromWishlist(itemId: String) {
        repository.removeItemFromWishlist(itemId)
    }

}
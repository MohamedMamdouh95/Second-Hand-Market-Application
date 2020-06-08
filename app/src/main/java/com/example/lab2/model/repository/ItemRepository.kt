package com.example.lab2.model.repository

import android.graphics.Bitmap

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.lab2.R
import com.example.lab2.model.Item
import com.example.lab2.model.Profile
import com.example.lab2.model.fbDocumentWithImage
import com.google.firebase.auth.ktx.auth

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class ItemRepository {

    val TAG = "ITEM_REPOSITORY"
    private val db = Firebase.firestore
    private val itemsRef = db.collection(ITEMS_COLLECTION)
    private val imageRepository = ImageRepository()


    // create item in firebase
    fun createItem(item: Item, bitmap: Bitmap?): LiveData<DocumentWriteResult> {
        val resultLiveData = MutableLiveData<DocumentWriteResult>(
            DocumentWriteResult(
                null,
                "Uploading picture",
                false
            )
        )
        val createItemInFB: (fbDocumentWithImage, MutableLiveData<DocumentWriteResult>) -> Unit =
            { itemToCreate, result ->
                itemsRef.add(itemToCreate).addOnFailureListener {
                    result.value = DocumentWriteResult(null, "Creating Item failed", true)
                }
                    .addOnSuccessListener { documentReference ->
                        result.value = DocumentWriteResult(
                            documentReference.id,
                            "Item created successfully",
                            false
                        )
                    }
            }
        bitmap?.let {
            imageRepository.uploadImageAndDataToFirebase(
                it,
                resultLiveData,
                item,
                createItemInFB
            )
        } ?: createItemInFB(item, resultLiveData)
        return resultLiveData
    }


    fun updateItem(item: Item, bitmap: Bitmap?): LiveData<DocumentWriteResult> {
        val resultLiveData = MutableLiveData<DocumentWriteResult>(
            DocumentWriteResult(
                null,
                "Uploading picture",
                false
            )
        )
        val updateItemInFB: (fbDocumentWithImage, MutableLiveData<DocumentWriteResult>) -> Unit =
            { itemToUpdate, result ->
                itemsRef.document(itemToUpdate.documentId!!).set(itemToUpdate)
                    .addOnFailureListener {
                        result.value = DocumentWriteResult(null, "Updatin Item failed", true)
                    }
                    .addOnSuccessListener {
                        result.value = DocumentWriteResult(
                            itemToUpdate.documentId,
                            "Item updated successfully",
                            false
                        )
                    }
            }
        bitmap?.let {
            imageRepository.uploadImageAndDataToFirebase(
                it,
                resultLiveData,
                item,
                updateItemInFB
            )
        } ?: updateItemInFB(item, resultLiveData)
        return resultLiveData
    }

    fun getOwnItems(): MutableLiveData<List<Item>> {
        val itemListData = MutableLiveData<List<Item>>()

        itemsRef.whereEqualTo("vendorId", Firebase.auth.currentUser!!.uid)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                val items = ArrayList<Item>()
                for (document in value!!) {
                    val item = document.toObject(Item::class.java)
                    items.add(item)
                }
                itemListData.value = items
            }
        return itemListData
    }

    fun getItem(itemId: String?): LiveData<Item> {
        val itemLiveData = MutableLiveData<Item>(Item())
        Log.d(TAG, "getItem called with $itemId")
        itemId?.let {
            itemsRef.document(it).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: ${snapshot.data}")
                    itemLiveData.value = snapshot.toObject(Item::class.java)
                } else {
                    Log.d(TAG, "Current data: null")
                }

            }
        }
        return itemLiveData
    }

    fun getItemImageAsBitmap(path: String?): LiveData<Bitmap> {
        return imageRepository.getImageAsBitmap(path, R.drawable.telev)
    }

    fun getAllItemsExceptOneUser(userId: String): MutableLiveData<List<Item>> {
        val returnedItems = MutableLiveData<List<Item>>()
        //get the all items except for certain user
        itemsRef.addSnapshotListener { value, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            val allItems = ArrayList<Item>()
            for (document in value!!) {
                val item = document.toObject(Item::class.java)
                if (item.vendorId != userId && item.blocked == false) {
                    Log.d(TAG, item.toString())
                    allItems.add(item)
                }
            }
            returnedItems.value = allItems
        }
        return returnedItems
    }

    fun getItemBuyers(itemId: String?): MutableLiveData<List<Profile>> {
        val buyersLiveData = MutableLiveData<List<Profile>>()
        itemId?.let {
            itemsRef.document(it).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: ${snapshot.data}")

                    val item = snapshot.toObject(Item::class.java)

                    db.collection(USERS_COLLECTION).addSnapshotListener { value ,e->
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e)
                            return@addSnapshotListener
                        }
                        val buyers = ArrayList<Profile>()
                        for (user in value!!) {
                            val userObj = user.toObject(Profile::class.java)
                            if(item!!.buyers.contains(userObj.documentId)) {
                                buyers.add(userObj)
                            }
                        }
                        buyersLiveData.value = buyers
                    }

                } else {
                    Log.d(TAG, "Current data: null")
                }

            }
        }
        return buyersLiveData
    }

}
package com.example.lab2.model.repository

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.lab2.R
import com.example.lab2.model.Item
import com.example.lab2.model.Profile
import com.example.lab2.model.fbDocumentWithImage
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.math.round

class UserRepository {

    val TAG = "USER_REPOSITORY"
    private val db = Firebase.firestore
    private val usersRef = db.collection(USERS_COLLECTION)
    private val imageRepository = ImageRepository()


    fun getUser(userId: String): LiveData<Profile>? {
        val userLiveData = MutableLiveData<Profile>()
        usersRef.document(userId).addSnapshotListener { snapshot ,e->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
                userLiveData.value = snapshot.toObject(Profile::class.java)
            } else {
                Log.d(TAG, "Current data: null")
            }

        }
        return userLiveData
    }

    fun createOrUpdateUser(profile: Profile): Task<Void> {
        return usersRef.document(profile.documentId!!).set(profile)
    }

    fun getWishlist():MutableLiveData<List<Item>> {
        val returnedWishlist = MutableLiveData<List<Item>>()
        val ref1 = usersRef.document(Firebase.auth.currentUser!!.uid)
        ref1.addSnapshotListener{ snapshot ,e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val userDocument = snapshot.toObject(Profile::class.java)
                val itemsIds = userDocument!!.wishlist
                val ref2 = db.collection(ITEMS_COLLECTION)
                ref2.addSnapshotListener { value , e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    val itemsForWishlist = ArrayList<Item>()
                    for (item in value!!) {
                        val i = item.toObject(Item::class.java)
                        for (id in itemsIds) {
                            if (i.documentId == id && i.blocked == false)
                                itemsForWishlist.add(i)
                        }
                    }
                    returnedWishlist.value = itemsForWishlist

                }
            }
        }
        return returnedWishlist
    }

    fun getBoughtList():MutableLiveData<List<Item>> {
        val returnedBoughtList = MutableLiveData<List<Item>>()
        val ref1 = usersRef.document(Firebase.auth.currentUser!!.uid)
        ref1.addSnapshotListener{ snapshot ,e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val userDocument = snapshot.toObject(Profile::class.java)
                val buyedIds = userDocument!!.boughtItems
                val ref2 = db.collection(ITEMS_COLLECTION)
                ref2.addSnapshotListener { value , e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    val itemsForBoughtList = ArrayList<Item>()
                    for (item in value!!) {
                        val i = item.toObject(Item::class.java)
                        for (id in buyedIds) {
                            if (i.documentId == id)
                                itemsForBoughtList.add(i)
                        }
                    }
                    returnedBoughtList.value = itemsForBoughtList

                }
            }
        }
        return returnedBoughtList
    }

    fun getUserImageAsBitmap(path:String?): LiveData<Bitmap> {
        return imageRepository.getImageAsBitmap(path, R.drawable.profile_pic)
    }

    fun createUser(user: Profile, bitmap: Bitmap?): LiveData<DocumentWriteResult> {
        val resultLiveData = MutableLiveData<DocumentWriteResult>(DocumentWriteResult(null,"Uploading picture", false))
        val createUserInFB: (fbDocumentWithImage, MutableLiveData<DocumentWriteResult>) -> Unit = { userToCreate, result -> usersRef.add(userToCreate).addOnFailureListener { result.value = DocumentWriteResult(null, "Creating User failed", true) }
            .addOnSuccessListener { documentReference ->  result.value = DocumentWriteResult(documentReference.id, "Item created successfully", false)}
        }
        bitmap?.let {
            imageRepository.uploadImageAndDataToFirebase(
                it,
                resultLiveData,
                user,
                createUserInFB
            )
        } ?: createUserInFB(user, resultLiveData)
        return resultLiveData
    }

    fun updateUser(user: Profile, bitmap: Bitmap?): LiveData<DocumentWriteResult> {
        val resultLiveData = MutableLiveData<DocumentWriteResult>(DocumentWriteResult(null,"Uploading picture", false))
        val updateItemInFB: (fbDocumentWithImage, MutableLiveData<DocumentWriteResult>) -> Unit = { userToUpdate, result -> usersRef.document(userToUpdate.documentId!!).set(userToUpdate).addOnFailureListener { result.value = DocumentWriteResult(null, "Updatin User failed", true) }
            .addOnSuccessListener { result.value = DocumentWriteResult(userToUpdate.documentId, "User updated successfully", false)}
        }
        bitmap?.let {
            imageRepository.uploadImageAndDataToFirebase(
                it,
                resultLiveData,
                user,
                updateItemInFB
            )
        } ?: updateItemInFB(user, resultLiveData)
        return resultLiveData
    }

    fun addItemToWishlist(itemId: String) {
        usersRef.document(Firebase.auth.currentUser!!.uid).update("wishlist", FieldValue.arrayUnion(itemId))
        db.collection(ITEMS_COLLECTION).document(itemId).update("buyers", FieldValue.arrayUnion(Firebase.auth.currentUser!!.uid))
    }

    fun removeItemFromWishlist(itemId: String) {
        usersRef.document(Firebase.auth.currentUser!!.uid).update("wishlist", FieldValue.arrayRemove(itemId))
        db.collection(ITEMS_COLLECTION).document(itemId).update("buyers", FieldValue.arrayRemove(Firebase.auth.currentUser!!.uid))
    }

    fun addItemToBoughtList(userId: String, itemId: String) {
        usersRef.document(userId).update("boughtItems", FieldValue.arrayUnion(itemId))
    }

    fun updateNumberOfRewiews(userId: String) {
        usersRef.document(userId).update("numberOfRewiews", FieldValue.increment(1))
        usersRef.document(userId).get().addOnSuccessListener { document ->
            if (document != null) {
                val userDocument = document.toObject(Profile::class.java)
                val n = userDocument!!.numberOfRewiews
                Log.d("MYREWIEWS", "Update number , now : $n")
            }
        }
    }

    fun updateRating(userId: String, rating : Float){
        usersRef.document(userId).get().addOnSuccessListener { document ->
            if(document != null){
                val userDocument = document.toObject(Profile::class.java)
                val oldRating = userDocument!!.rating
                Log.d("MYREWIEWS", "Old rating : $oldRating")
                val n = userDocument!!.numberOfRewiews
                Log.d("MYREWIEWS", "Number of rewiews : $n")
                var newRating = oldRating + ((rating - oldRating)/n)
                newRating = round(newRating*10/10)
                Log.d("MYREWIEWS", "NewRating: $newRating")
                usersRef.document(userId).update("rating", newRating)
            }
        }
    }

}


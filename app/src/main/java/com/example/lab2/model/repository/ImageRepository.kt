package com.example.lab2.model.repository

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.lab2.model.fbDocumentWithImage
import com.google.firebase.ktx.Firebase

import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

import java.util.*

class ImageRepository {
    private val TAG = "IMAGE_REPOSITORY"
    private val storage = Firebase.storage
    private fun encodeImage(bm: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        return data
    }

    fun uploadImageAndDataToFirebase(
        bm: Bitmap,
        resultLiveData: MutableLiveData<DocumentWriteResult>,
        uploadable: fbDocumentWithImage,
        firestoreTask: (fbDocumentWithImage, MutableLiveData<DocumentWriteResult>) -> Unit
    ) {
        val imageStoragePath = UUID.randomUUID().toString()
        val imageStorageRef = storage.reference.child(imageStoragePath)
        imageStorageRef.putBytes(encodeImage(bm)).addOnSuccessListener { task ->
            resultLiveData.value = DocumentWriteResult(null, "Picture uploaded", false)
            uploadable.image = imageStoragePath
            firestoreTask(uploadable, resultLiveData)
        }.addOnFailureListener {
            resultLiveData.value = DocumentWriteResult(null, "Uploading picture failed", true)
        }
    }

    fun getImageAsBitmap(uri: String?, defaultDrawable: Int): MutableLiveData<Bitmap> {
        val returnedImage = MutableLiveData<Bitmap>(
            BitmapFactory.decodeResource(
                Resources.getSystem(),
                defaultDrawable
            )
        )
        Log.d(TAG, "DOWNLOADING PICTURE")
        uri?.let {
            storage.getReference(it).getBytes(1024 * 1024 * 10)
                .addOnSuccessListener { it1 ->
                    val bitmap = BitmapFactory.decodeByteArray(it1, 0, it1.size)
                    returnedImage.value = bitmap
                    Log.d(TAG, "PICTURE DOWNLOADED")
                }
        }
        return returnedImage
    }
}
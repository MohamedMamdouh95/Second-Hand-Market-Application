package com.example.lab2.model

import java.io.Serializable
import com.google.firebase.firestore.DocumentId

data class Profile(
    var fullname: String,
    var nickname: String,
    var email: String,
    var location: String,
    var latitude: Double?,
    var longitude: Double?,
    override var image: String?,
    @DocumentId override var documentId: String?,
    val wishlist: ArrayList<String>
) : Serializable, fbDocumentWithImage {
    constructor() : this(
        "", "", "",
        "", null, null, null, null, ArrayList<String>()
    )
}

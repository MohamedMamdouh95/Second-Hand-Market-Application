package com.example.lab2.model

import java.io.Serializable
import com.google.firebase.firestore.DocumentId

data class Profile(
    val fullname: String,
    val nickname: String,
    val email: String,
    val location: String,
    override var image: String?,
    @DocumentId override var documentId: String?,
    val wishlist : ArrayList<String>
) : Serializable ,fbDocumentWithImage{
    constructor() : this("", "", "",
        "", null, null , ArrayList<String>())
}

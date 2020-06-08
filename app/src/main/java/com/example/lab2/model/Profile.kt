package com.example.lab2.model

import java.io.Serializable
import com.google.firebase.firestore.DocumentId
//TODO: ADD rating: float to profile
data class Profile(
    var fullname: String,
    var nickname: String,
    var email: String,
    var location: String,
    var latitude: Double?,
    var longitude: Double?,
    override var image: String?,
    @DocumentId override var documentId: String?,
    val wishlist : ArrayList<String>,
    var boughtItems : ArrayList<String>,
    var rating : Float,
    var numberOfRewiews : Int
) : Serializable ,fbDocumentWithImage{
    constructor() : this("", "", "",
        "", null, null, null, null , ArrayList<String>(),ArrayList<String>(),0.0f,0)
}

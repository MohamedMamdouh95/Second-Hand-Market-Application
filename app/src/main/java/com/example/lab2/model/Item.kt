package com.example.lab2.model

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class Item(
    var title: String,
    var category: String,
    var subcategory: String,
    var location: String,
    var expiryDate: String,
    var price: String,
    var description: String,
    var latitude: Double?,
    var longitude: Double?,
    override var image: String?,
    var vendorId: String?,
    @DocumentId override var documentId: String?,
    var buyers: ArrayList<String>,
    var blocked : Boolean,
    var rated : Boolean
) : Serializable, fbDocumentWithImage {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        null,
        null,
        null,
        "",
        null,
        ArrayList<String>(),
        false,
        false
    )
}
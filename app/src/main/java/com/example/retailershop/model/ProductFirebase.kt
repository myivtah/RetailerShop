package com.example.retailershop.model

// Tambahkan model khusus untuk Firebase deserialization
data class ProductFirebase(
    val barcode: String = "",
    val name: String = "",
    val price: Int = 0,
    var quantity: Int = 0
) {
    constructor() : this("", "", 0, 0) // Konstruktor tanpa argumen
}

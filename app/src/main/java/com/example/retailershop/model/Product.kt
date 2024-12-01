package com.example.retailershop.model

data class Product(
    val barcode: String,  // Properti barcode
    val name: String,
    val price: Double,
    var quantity: Int = 0
)

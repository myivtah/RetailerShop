package com.example.retailershop.model

data class Product(
    val barcode: String,
    val name: String,
    val price: Int,
    var quantity: Int = 0
)

package com.example.retailershop.model

data class Item(
    var barcode: String = "",
    var name: String = "",
    var price: Int = 0,
    var purchasePrice: Int = 0,
    var quantity: Int = 0,
    var date: String = "",
    var supplier: String = ""
)

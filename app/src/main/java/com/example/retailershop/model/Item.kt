package com.example.retailershop.model

data class Item(
    val codebar: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val purchasePrice: Double = 0.0,
    val quantity: Int = 0,
    val date: String = "",
    val supplier: String = ""
)

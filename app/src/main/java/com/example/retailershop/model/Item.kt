package com.example.retailershop.model

data class Item(
    val codebar: String = "",
    val name: String = "",
    val price: Int = 0,
    val purchasePrice: Int = 0,
    val quantity: Int = 0,
    val date: String = "",
    val supplier: String = ""
)

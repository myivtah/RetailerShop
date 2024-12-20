package com.example.retailershop.model

data class Product(
    val barcode: String,
    val name: String,
    val price: Int,
    var quantity: Int = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "barcode" to barcode,
            "name" to name,
            "price" to price,
            "quantity" to quantity
        )
    }
}

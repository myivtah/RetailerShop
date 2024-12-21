package com.example.retailershop.model

data class Transaction(
    var id: String? = null,
    var transactionDate: String? = null,
    var cashChange: String? = null,
    var cashPaid: String? = null,
    var totalPrice: Int = 0,
    var userEmail: String? = null,
    var productList: List<ProductFirebase>? = null // Gunakan ProductFirebase di sini
) {
    constructor() : this(null, null, null, null, 0, null, null) // Konstruktor tanpa argumen
}

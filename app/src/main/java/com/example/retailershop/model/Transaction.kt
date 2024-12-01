package com.example.retailershop.model

data class Transaction(
    val transactionId: String? = null,
    val productCode: String? = null,
    val quantity: Int? = null,
    val totalAmount: Double? = null,
    val transactionDate: String? = null
)

package com.example.retailershop.model

data class Supplier(
    val id: String = "",
    val name: String = "",
    val phone: String = ""
) {
    constructor() : this("", "", "")  // Constructor kosong untuk Firebase
}

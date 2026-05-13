package com.example.moneyflow.data.model

data class User(
    val id: Int = 1,
    val login: String,
    val passwordHash: String
)

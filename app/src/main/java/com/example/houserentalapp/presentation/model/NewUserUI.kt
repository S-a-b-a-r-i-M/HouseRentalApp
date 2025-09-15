package com.example.houserentalapp.presentation.model

import com.example.houserentalapp.domain.model.User

data class NewUserUI(
    val name: String,
    val phone: String,
    val password: String,
) {
    fun toDomainUser() = User(
        id = 0,
        name = this.name,
        phone = this.phone,
        email = null,
        password = this.password,
        createdAt = System.currentTimeMillis()
    )
}

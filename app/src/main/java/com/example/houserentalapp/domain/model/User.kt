package com.example.houserentalapp.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User (
    val id: Long,
    val name: String,
    val phone: String,
    val email: String? = null,
    val password: String,
    val profileImageSource: ImageSource? = null,
    val createdAt: Long
) : Parcelable {
    override fun toString()= "User(id=$id, name=$name, phone=$phone, email=$email, profileImageSource=$profileImageSource, createdAt=$createdAt"
}
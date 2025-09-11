package com.example.houserentalapp.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User (
    val id: Long,
    val name: String,
    val phone: String,
    val email: String,
    val password: String,
    val createdAt: Long
) : Parcelable
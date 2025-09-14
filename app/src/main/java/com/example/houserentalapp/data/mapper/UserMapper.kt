package com.example.houserentalapp.data.mapper

import com.example.houserentalapp.data.local.db.entity.UserEntity
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.presentation.utils.extensions.logError

object UserMapper {
    fun entityToDomain(entity: UserEntity): User {
        return User(
            id = entity.id,
            name = entity.name,
            email = entity.email,
            phone = entity.phone,
            password = entity.password,
            createdAt = entity.createdAt
        )
    }

    fun domainToEntity(domain: User) = UserEntity (
        id = domain.id,
        name = domain.name,
        email = domain.email,
        phone = domain.phone,
        password = domain.password,
        createdAt = domain.createdAt
    )
}
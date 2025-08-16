package com.example.houserentalapp.data.mapper

import com.example.houserentalapp.data.local.db.entity.UserEntity
import com.example.houserentalapp.domain.model.User

object UserMapper {
    fun entityToDomain(entity: UserEntity) = User (
        id = entity.id,
        name = entity.name,
        email = entity.email,
        phone = entity.phone,
        createdAt = entity.createdAt
    )

    fun domainToEntity(domain: User) = UserEntity (
        id = domain.id,
        name = domain.name,
        email = domain.email,
        phone = domain.phone,
        createdAt = domain.createdAt
    )
}
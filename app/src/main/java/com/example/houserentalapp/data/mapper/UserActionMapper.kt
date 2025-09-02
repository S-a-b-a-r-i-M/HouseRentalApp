package com.example.houserentalapp.data.mapper

import com.example.houserentalapp.data.local.db.entity.UserActionEntity
import com.example.houserentalapp.domain.model.UserActionData
import com.example.houserentalapp.domain.model.enums.UserActionEnum

object UserActionMapper {
    fun fromDomain(domain: UserActionData) = UserActionEntity(
        id = domain.id,
        propertyId = domain.propertyId,
        action = domain.action.name,
        createdAt = domain.createdAt
    )

    fun toDomain(entity: UserActionEntity) = UserActionData(
        id = entity.id,
        propertyId = entity.propertyId,
        action = UserActionEnum.valueOf(entity.action),
        createdAt = entity.createdAt
    )
}
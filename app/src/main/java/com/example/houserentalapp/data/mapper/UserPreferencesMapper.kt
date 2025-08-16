package com.example.houserentalapp.data.mapper

import com.example.houserentalapp.data.local.db.entity.UserPreferenceEntity
import com.example.houserentalapp.data.util.fromString
import com.example.houserentalapp.domain.model.UserPreferences
import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.LookingTo

object UserPreferencesMapper {
    fun entityToDomain(entity: UserPreferenceEntity) = UserPreferences(
        city = entity.city,
        lookingTo = entity.lookingTo?.let { LookingTo.entries.fromString(it) },
        bhk = entity.bhk?.let {  BHK.entries.fromString(entity.bhk) }
    )

    fun domainToEntity(userId: Long, domain: UserPreferences) = UserPreferenceEntity (
        userId,
        city = domain.city,
        lookingTo = domain.lookingTo?.name,
        bhk = domain.bhk?.name
    )
}
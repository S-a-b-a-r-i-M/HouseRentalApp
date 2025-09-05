package com.example.houserentalapp.data.mapper

import com.example.houserentalapp.data.local.db.entity.SearchHistoryEntity
import com.example.houserentalapp.domain.model.PropertyFilters
import com.google.gson.Gson

object SearchHistoryMapper {
    private val gson = Gson()

    fun toEntity(domain: PropertyFilters, userId: Long) = SearchHistoryEntity(
        userId = userId,
        query = domain.searchQuery,
        meta = gson.toJson(domain)
    )

    fun toPropertyFilter(entity: SearchHistoryEntity): PropertyFilters = gson.fromJson(
        entity.meta, PropertyFilters::class.java
    )
}
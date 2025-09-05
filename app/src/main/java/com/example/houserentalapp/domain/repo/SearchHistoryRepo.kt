package com.example.houserentalapp.domain.repo

import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.utils.Result

interface SearchHistoryRepo {
    // CREATE
    suspend fun storeSearchHistory(userId: Long, filters: PropertyFilters): Result<Boolean>

    // READ
    suspend fun getRecentSearchHistories(userId: Long, limit: Int): Result<List<PropertyFilters>>
}
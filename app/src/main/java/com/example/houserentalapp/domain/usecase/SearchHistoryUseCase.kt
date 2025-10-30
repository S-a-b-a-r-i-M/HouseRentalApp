package com.example.houserentalapp.domain.usecase

import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.repo.SearchHistoryRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logError
import javax.inject.Inject

class SearchHistoryUseCase @Inject constructor(private val searchHistoryRepo: SearchHistoryRepo) {
    suspend fun getResentSearchHistories(userId: Long, limit: Int): Result<List<PropertyFilters>> {
        return try {
            return searchHistoryRepo.getRecentSearchHistories(userId, limit)
        } catch (exp: Exception) {
            logError("${exp.message.toString()} on getResentSearchHistories (id: $userId)")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun storeSearchHistory(userId: Long, filters: PropertyFilters): Result<Boolean> {
        return try {
            return searchHistoryRepo.storeSearchHistory(userId, filters)
        } catch (exp: Exception) {
            logError("${exp.message.toString()} on storeSearchHistory (id: $userId)")
            Result.Error(exp.message.toString())
        }
    }
}
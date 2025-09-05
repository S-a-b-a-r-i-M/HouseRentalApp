package com.example.houserentalapp.data.repo

import android.content.Context
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.dao.SearchHistoryDao
import com.example.houserentalapp.data.mapper.SearchHistoryMapper
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.repo.SearchHistoryRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchHistoryRepoImpl(context: Context) : SearchHistoryRepo {
    private val dao = SearchHistoryDao(DatabaseHelper.getInstance(context))

    override suspend fun storeSearchHistory(userId: Long, filters: PropertyFilters): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val searchHistoryEntity = SearchHistoryMapper.toEntity(filters, userId)
                val isCreated = dao.storeSearchHistory(searchHistoryEntity)
                logInfo("Search history stored result: $isCreated")
                Result.Success(isCreated)
            }
        } catch (e: Exception) {
            logError("Error on storing search history", e)
            Result.Error(e.message.toString())
        }
    }

    override suspend fun getRecentSearchHistories(userId: Long, limit: Int): Result<List<PropertyFilters>> {
        return try {
            withContext(Dispatchers.IO) {
                val searchHistoryEntityList = dao.getRecentSearchHistories(userId, limit)
                val propertyFiltersList = searchHistoryEntityList.map {
                    SearchHistoryMapper.toPropertyFilter(it)
                }
                logInfo("recent Search history retrieved successfully.")
                Result.Success(propertyFiltersList)
            }
        } catch (e: Exception) {
            logError("Error on read search history", e)
            Result.Error(e.message.toString())
        }
    }
}
package com.example.houserentalapp.data.local.db.dao

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.entity.SearchHistoryEntity
import com.example.houserentalapp.data.local.db.tables.SearchHistoryTable
import javax.inject.Inject

class SearchHistoryDao @Inject constructor(private val dbHelper: DatabaseHelper) {
    private val writableDB: SQLiteDatabase
        get() = dbHelper.writableDatabase

    private val readableDB: SQLiteDatabase
        get() = dbHelper.readableDatabase

    // -------------- CREATE --------------
    fun storeSearchHistory(searchHistoryEntity: SearchHistoryEntity): Boolean {
        val query = """
            INSERT OR REPLACE INTO ${SearchHistoryTable.TABLE_NAME} 
            (${SearchHistoryTable.COLUMN_USER_ID}, 
            ${SearchHistoryTable.COLUMN_QUERY}, 
            ${SearchHistoryTable.COLUMN_META})
            VALUES (?, ?, ?)
        """

        writableDB.execSQL(query, arrayOf<Any>(
            searchHistoryEntity.userId,
            searchHistoryEntity.query,
            searchHistoryEntity.meta
        ))
        return true
    }

    // -------------- READ --------------
    fun getRecentSearchHistories(userId: Long, limit: Int): List<SearchHistoryEntity> {
        readableDB.query(
            SearchHistoryTable.TABLE_NAME,
            null,
            "${SearchHistoryTable.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null,
            "${SearchHistoryTable.COLUMN_CREATED_AT} DESC",
            limit.toString()
        ).use { cursor ->
            val historyEntities = mutableListOf<SearchHistoryEntity>()
            while (cursor.moveToNext())
                historyEntities.add(parseSearchHistoryEntityFromCursor(cursor))

            return historyEntities
        }
    }

    //  --------- HELPER ---------------
    private fun parseSearchHistoryEntityFromCursor(cursor: Cursor) = with(cursor) {
        SearchHistoryEntity(
            userId = getLong(getColumnIndexOrThrow(SearchHistoryTable.COLUMN_USER_ID)),
            query = getString(getColumnIndexOrThrow(SearchHistoryTable.COLUMN_QUERY)),
            meta = getString(getColumnIndexOrThrow(SearchHistoryTable.COLUMN_META)),
            createdAt = getLong(getColumnIndexOrThrow(SearchHistoryTable.COLUMN_CREATED_AT)),
        )
    }
}
package com.example.houserentalapp.data.mapper

import android.database.Cursor
import androidx.core.database.getStringOrNull
import com.example.houserentalapp.data.local.db.tables.UserTable
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.domain.model.User

object UserMapper {
    fun toUser(cursor: Cursor): User {
        with(cursor) {
            val imageSource = getStringOrNull(
                getColumnIndexOrThrow(UserTable.COLUMN_PROFILE_IMAGE_ADDRESS)
            )?.let {
                ImageSource.LocalFile(it)
            }

            return User(
                id = getLong(cursor.getColumnIndexOrThrow(UserTable.COLUMN_ID)),
                name = getString(getColumnIndexOrThrow(UserTable.COLUMN_NAME)),
                email = getStringOrNull(getColumnIndexOrThrow(UserTable.COLUMN_EMAIL)),
                phone = getString(getColumnIndexOrThrow(UserTable.COLUMN_PHONE)),
                password = getString(getColumnIndexOrThrow(UserTable.COLUMN_HASHED_PASSWORD)),
                profileImageSource = imageSource,
                createdAt = getLong(getColumnIndexOrThrow(UserTable.COLUMN_CREATED_AT))
            )
        }
    }
}
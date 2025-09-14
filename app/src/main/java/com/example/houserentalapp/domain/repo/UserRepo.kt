package com.example.houserentalapp.domain.repo

import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.UserPreferences
import com.example.houserentalapp.domain.model.enums.UserField
import com.example.houserentalapp.domain.utils.Result

interface UserRepo {
    // CREATE
    suspend fun createUser(
        name: String,
        phoneNumber: String,
        email: String,
        hashedPassWord: String
    ): Result<Long>

    suspend fun createUserSession(userId: Long): Result<Boolean> // On Login

    suspend fun createUserPreferences(userId: Long, preferences: UserPreferences): Result<Long>

    // READ
    suspend fun getUserById(userId: Long): Result<User?>

    suspend fun getUserByPhone(phone: String): Result<User?>

    suspend fun getUserPreferences(userId: Long): Result<UserPreferences?>

    // UPDATE
    suspend fun updateUserPreferences(userId: Long, preferences: UserPreferences): Result<Boolean>

    suspend fun updateUser(modifiedUser: User, updatedFields: List<UserField>): Result<User>

    // DELETE
    suspend fun removeUserSession(userId: Long): Result<Boolean> // On Logout

    // EXISTS
    suspend fun isPhoneNumberExists(phoneNumber: String): Result<Boolean>
}
package com.example.houserentalapp.data.repo

import android.content.Context
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.dao.UserDao
import com.example.houserentalapp.data.local.db.entity.UserEntity
import com.example.houserentalapp.data.local.prefs.SessionManager
import com.example.houserentalapp.data.mapper.UserMapper
import com.example.houserentalapp.data.mapper.UserPreferencesMapper
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.UserPreferences
import com.example.houserentalapp.domain.repo.UserRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo

class UserRepoImpl(context: Context) : UserRepo {
    private val userDao = UserDao(DatabaseHelper.getInstance(context))
    private val sessionManager = SessionManager(context)

    // -------------- CREATE --------------
    override suspend fun createUser(
        name: String,
        phoneNumber: String,
        email: String,
        hashedPassWord: String
    ): Result<Long> {
        return try {
            val userEntity = UserEntity(
                name = name,
                email = email,
                phone = phoneNumber,
                password = hashedPassWord,
                createdAt = System.currentTimeMillis()
            )

            val userId = userDao.insertUser(userEntity, hashedPassWord)
            logInfo("User($userId) created successfully")
            Result.Success(userId)
        } catch (e: Exception) {
            logError("Error creating user", e)
            Result.Error(e.message.toString())
        }
    }

    override suspend fun createUserSession(userId: Long): Result<Boolean> {
        return try {
            val success = sessionManager.createSession(userId)
            Result.Success(success)
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun createUserPreferences(userId: Long, preferences: UserPreferences): Result<Long> {
        return try {
            val entity = UserPreferencesMapper.domainToEntity(userId, preferences)
            val id = userDao.insertUserPreferences(entity)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    // -------------- READ --------------
    override suspend fun getUserById(userId: Long): Result<User?> {
        return try {
            val entity = userDao.getUserById(userId) ?: return Result.Success(null)
            Result.Success(UserMapper.entityToDomain(entity))
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun getUserPreferences(userId: Long): Result<UserPreferences?> {
        return try {
            val entity = userDao.getUserPreferences(userId) ?: return Result.Success(null)
            Result.Success(UserPreferencesMapper.entityToDomain(entity))
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun getUserByPhone(phone: String): Result<User?> {
        return try {
            val entity = userDao.getUserByPhone(phone) ?: return Result.Success(null)
            return Result.Success(UserMapper.entityToDomain(entity))
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    // -------------- UPDATE --------------
    override suspend fun updateUserPreferences(
        userId: Long, preferences: UserPreferences
    ): Result<Boolean> {
        return try {
            val entity = UserPreferencesMapper.domainToEntity(userId, preferences)
            val rowsAffected = userDao.updateUserPreferences(entity)
            Result.Success(rowsAffected > 0)
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    // -------------- DELETE --------------
    override suspend fun removeUserSession(userId: Long): Result<Boolean> {
        return try {
            val success = sessionManager.clearSession()
            Result.Success(success)
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    // -------------- EXISTS --------------
    override suspend fun isPhoneNumberExists(phoneNumber: String): Result<Boolean> {
        return try {
            val exists = userDao.isPhoneNumberExists(phoneNumber)
            Result.Success(exists)
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }
}

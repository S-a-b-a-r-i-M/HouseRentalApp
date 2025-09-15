package com.example.houserentalapp.data.repo

import android.content.Context
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.dao.ImageStorageDao
import com.example.houserentalapp.data.local.db.dao.UserDao
import com.example.houserentalapp.data.local.prefs.SessionManager
import com.example.houserentalapp.data.mapper.UserPreferencesMapper
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.UserPreferences
import com.example.houserentalapp.domain.model.enums.UserField
import com.example.houserentalapp.domain.repo.UserRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepoImpl(context: Context) : UserRepo {
    private val userDao = UserDao(DatabaseHelper.getInstance(context))
    private val sessionManager = SessionManager(context)
    private val imageStorageDao = ImageStorageDao(context)

    // -------------- CREATE --------------
    override suspend fun createUser(newUser: User): Result<Long> {
        return try {
            withContext(Dispatchers.IO) {
                val userId = userDao.insertUser(newUser)
                logInfo("User($userId) created successfully")
                Result.Success(userId)
            }
        } catch (e: Exception) {
            logError("Error creating user", e)
            Result.Error(e.message.toString())
        }
    }

    override suspend fun createUserSession(userId: Long): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val success = sessionManager.createSession(userId)
                logInfo("User($userId) session created")
                Result.Success(success)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun createUserPreferences(userId: Long, preferences: UserPreferences): Result<Long> {
        return try {
            withContext(Dispatchers.IO) {
                val entity = UserPreferencesMapper.domainToEntity(userId, preferences)
                val id = userDao.insertUserPreferences(entity)
                Result.Success(id)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    // -------------- READ --------------
    override suspend fun getUserById(userId: Long): Result<User> {
        return try {
            withContext(Dispatchers.IO) {
                val user = userDao.getUserById(userId)
                logDebug("User for id: $user")
                Result.Success(user)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun getUserIdFromSession(): Result<Long?> {
        return try {
            withContext(Dispatchers.IO) {
                val userId = sessionManager.getLoggedInUserId()
                logInfo("Currently log in user id is $userId")
                Result.Success(if (userId == -1L) null else userId)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun getUserPreferences(userId: Long): Result<UserPreferences?> {
        return try {
            withContext(Dispatchers.IO) {
                val entity = userDao.getUserPreferences(userId) ?: return@withContext Result.Success(null)
                Result.Success(UserPreferencesMapper.entityToDomain(entity))
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun getUserByPhone(phone: String): Result<User?> {
        return try {
            withContext(Dispatchers.IO) {
                val user = userDao.getUserByPhone(phone) ?: return@withContext Result.Success(null)
                logDebug("User for phone($phone): $user")
                Result.Success(user)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    // -------------- UPDATE --------------
    override suspend fun updateUserPreferences(
        userId: Long, preferences: UserPreferences
    ): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val entity = UserPreferencesMapper.domainToEntity(userId, preferences)
                val rowsAffected = userDao.updateUserPreferences(entity)
                Result.Success(rowsAffected > 0)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun updateUser(
        modifiedUser: User, updatedFields: List<UserField>
    ): Result<User> {
        return try {
            withContext(Dispatchers.IO) {
                var _modifiedUser = modifiedUser
                if (UserField.PROFILE_IMAGE in updatedFields && modifiedUser.profileImageSource is ImageSource.Uri) {
                    val existingProfilePath = userDao.getUserProfileImageAddress(modifiedUser.id)
                    if (existingProfilePath != null) // Delete if exits
                        imageStorageDao.deleteImageByPath(existingProfilePath)
                    // Create
                    imageStorageDao.saveUserImage(
                        modifiedUser.id, modifiedUser.profileImageSource.uri
                    )?.let {
                        _modifiedUser = modifiedUser.copy(profileImageSource = ImageSource.LocalFile(it))
                    }
                    logDebug("User profile image updated")
                }

                val rowsAffected = userDao.updateUser(_modifiedUser, updatedFields)
                logDebug("Update user details row count: $rowsAffected")
                getUserById(modifiedUser.id)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    // -------------- DELETE --------------
    override suspend fun removeUserSession(userId: Long): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val success = sessionManager.clearSession()
                logDebug("User(${userId}) session cleared")
                Result.Success(success)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    // -------------- EXISTS --------------
    override suspend fun isPhoneNumberExists(phoneNumber: String): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val exists = userDao.isPhoneNumberExists(phoneNumber)
                Result.Success(exists)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }
}

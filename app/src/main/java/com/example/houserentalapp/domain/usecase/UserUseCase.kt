package com.example.houserentalapp.domain.usecase

import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.enums.UserField
import com.example.houserentalapp.domain.repo.UserRepo
import com.example.houserentalapp.domain.utils.ErrorCode
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning

class UserUseCase(private val userRepo: UserRepo) {
    suspend fun createUser(newUser: User): Result<User> {
        return try {
            val isPhoneExistsResult = userRepo.isPhoneNumberExists(newUser.phone)
            if (isPhoneExistsResult is Result.Success && isPhoneExistsResult.data) {
                logWarning("Phone number already exists")
                return Result.Error("Phone number already exists", ErrorCode.DUPLICATION)
            }

            when(val res = userRepo.createUser(newUser)) {
                is Result.Success<Long> -> {
                    logInfo("User created successfully.")
                    logDebug("Now getting new user by userId:${res.data}")
                    userRepo.createUserSession(res.data) // Create Session
                    userRepo.getUserById(res.data) // Retrieve User by id
                }
                is Result.Error -> {
                    logError("Error:${res.message} while signUp User")
                    Result.Error(res.message)
                }
            }
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while signUp User")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun getUserFromSession(): Result<User?> {
        return try {
            when(val res = userRepo.getUserIdFromSession()) {
                is Result.Success<Long?> -> {
                    if (res.data == null) {
                        logInfo("No User session is found")
                        return Result.Success(null)
                    }

                    userRepo.getUserById(res.data)
                }
                is Result.Error -> res
            }
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while fetching property summaries")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun getUser(phone: String, password: String): Result<User> {
        return try {
            when(val res = userRepo.getUserByPhone(phone)) {
                is Result.Success<User?> -> {
                    if (res.data == null)
                        Result.Error(
                            "No User found for given phone", ErrorCode.RESOURCE_NOT_FOUND
                        )
                    else {
                        // Check PassWord is Matching
                        val user = res.data
                        if (user.password != password)
                            return Result.Error(
                                "Password mismatched.", ErrorCode.VALIDATION
                            )
                        userRepo.createUserSession(res.data.id) // Create Session
                        Result.Success(res.data)
                    }
                }
                is Result.Error -> res
            }
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while fetching property summaries")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun isPhoneNumberExists(phone: String): Result<Boolean> {
        return try {
            return userRepo.isPhoneNumberExists(phone)
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while fetching property summaries")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun updateUser(modifiedUser: User, updatedFields: List<UserField>): Result<User> {
        return try {
            return userRepo.updateUser(modifiedUser, updatedFields)
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while updating user")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun getLoggedInUserId() : Result<Long?> {
        return try {
           return userRepo.getUserIdFromSession()
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while removing user session")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun logOutUser(userId: Long): Result<Boolean> {
        return try {
            when(val res = userRepo.removeUserSession(userId)){
                is Result.Success<Boolean> -> {
                    if (res.data) return res

                    logError("User Session is not removed properly")
                    Result.Error("Session not cleared")
                }
                is Result.Error -> {
                    logError("${res.message} while clearing user session")
                    Result.Error("Session not cleared")
                }
            }
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while removing user session")
            Result.Error(exp.message.toString())
        }
    }
}
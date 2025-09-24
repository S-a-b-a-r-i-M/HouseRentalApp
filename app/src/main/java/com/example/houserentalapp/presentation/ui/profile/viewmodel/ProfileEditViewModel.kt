package com.example.houserentalapp.presentation.ui.profile.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.enums.UserField
import com.example.houserentalapp.domain.usecase.UserUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import com.example.houserentalapp.presentation.utils.helpers.validateEmailFormat
import com.example.houserentalapp.presentation.utils.helpers.validateUserName
import kotlinx.coroutines.launch

enum class UserEditFormField{
    NAME,
    EMAIL,
    PROFILE_IMAGE
}

class ProfileEditViewModel(
    private val currentUser: User, private val userUC: UserUseCase
) : ViewModel() {
    private var _editableUser = MutableLiveData(currentUser.copy())
    val editableUser: LiveData<User> = _editableUser

    private val _isFormDirty = MutableLiveData(false)
    val isFormDirty: LiveData<Boolean> = _isFormDirty

    private val _validationError = MutableLiveData<String?>(null)
    val validationError: LiveData<String?> = _validationError

    private val formErrorsMap = UserEditFormField.entries.associateWith {
        MutableLiveData<String?>(null)
    }

    fun getFieldError(filed: UserEditFormField): LiveData<String?> = formErrorsMap.getValue(filed)

    fun clearFieldError(filed: UserEditFormField) {
        val fieldErrLD = formErrorsMap.getValue(filed)
        if (fieldErrLD.value != null) fieldErrLD.value = null
    }

    private inline fun updateUserData(update: (User) -> User) {
        _editableUser.value = update(_editableUser.value!!)
        _isFormDirty.value = _editableUser.value != currentUser
    }

    private inline fun onValueChange(field: UserEditFormField, update: (User) -> User) {
        updateUserData(update)
        clearFieldError(field)
    }

    fun updateChanges(field: UserEditFormField, value: Any?) {
        when (field) {
            UserEditFormField.NAME -> onValueChange(field) { it.copy(name = value as String) }
            UserEditFormField.EMAIL -> onValueChange(field) { it.copy(email = value as? String) }
            UserEditFormField.PROFILE_IMAGE ->
                onValueChange(field) { it.copy(profileImageSource = ImageSource.Uri(value as Uri)) }
        }
    }

    private fun getModifiedFields(): List<UserField> = mutableListOf<UserField>().apply {
        val modifiedUser = _editableUser.value!!
        if (modifiedUser.name != currentUser.name) add(UserField.NAME)
        if (modifiedUser.email != currentUser.email) add(UserField.EMAIL)
        if (modifiedUser.profileImageSource != currentUser.profileImageSource) add(UserField.PROFILE_IMAGE)
    }

    // Simple validation method
    private fun validateUser(): Boolean {
        var isValid = true
        val user = _editableUser.value!!

        // Name validation
        validateUserName(user.name)?.let {
            formErrorsMap.getValue(UserEditFormField.NAME).value = it
            isValid = false
        }

        // Email validation (if provided)
        if (user.email != null)
            validateEmailFormat(user.email)?.let {
                formErrorsMap.getValue(UserEditFormField.EMAIL).value = it
                isValid = false
            }

        return isValid
    }

    fun clearValidationError() {
        _validationError.value = null
    }

    fun saveUserChanges(onSuccess: (User) -> Unit, onFailure: (String) -> Unit) {
        if (_isFormDirty.value != true) {
            logWarning("Trying to save changes on not changed form isFormDirty: $_isFormDirty")
            return
        }

        // Validate user data before saving
        if (!validateUser()) {
            _validationError.value = "Please resolve the above errors"
            logDebug("Validation failed to save user")
            return
        }

        viewModelScope.launch {
            val updatedFields = getModifiedFields()
            if (updatedFields.isEmpty()) {
                logWarning("No Modified fields found to update user")
                return@launch
            }

            when(val res = userUC.updateUser(_editableUser.value!!, updatedFields)) {
                is Result.Success<User> -> {
                    logInfo("User updated successfully. ${res.data}")
                    onSuccess(res.data)
                }
                is Result.Error -> {
                    logError("User update failed")
                    onFailure("Changes are not saved, try again later.")
                }
            }
        }
    }
}

class ProfileEditViewModelFactory(
    private val currentUser: User,
    private val userUC: UserUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileEditViewModel::class.java))
            return ProfileEditViewModel(currentUser, userUC) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
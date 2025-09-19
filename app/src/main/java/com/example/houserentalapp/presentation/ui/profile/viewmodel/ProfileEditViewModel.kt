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
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import kotlinx.coroutines.launch

class ProfileEditViewModel(
    private val currentUser: User, private val userUC: UserUseCase
) : ViewModel() {
    private var _editableUser = MutableLiveData(currentUser.copy())
    val editableUser: LiveData<User> = _editableUser

    private val _isFormDirty = MutableLiveData(false)
    val isFormDirty: LiveData<Boolean> = _isFormDirty

    private inline fun updateUserData(update: (User) -> User) {
        _editableUser.value = update(_editableUser.value!!)
        _isFormDirty.value = _editableUser.value != currentUser
    }

    fun updateChanges(field: UserField, value: Any?) {
        when (field) {
            UserField.NAME -> updateUserData { it.copy(name = value as String) }
            UserField.PHONE -> updateUserData { it.copy(phone = value as String) }
            UserField.EMAIL -> updateUserData { it.copy(email = value as? String) }
            UserField.PROFILE_IMAGE ->
                updateUserData { it.copy(profileImageSource = ImageSource.Uri(value as Uri)) }
        }
    }

    private fun getModifiedFields(): List<UserField> = mutableListOf<UserField>().apply {
        val modifiedUser = _editableUser.value!!
        if (modifiedUser.name != currentUser.name) add(UserField.NAME)
        if (modifiedUser.email != currentUser.email) add(UserField.EMAIL)
        if (modifiedUser.phone != currentUser.phone) add(UserField.PHONE)
        if (modifiedUser.profileImageSource != currentUser.profileImageSource) add(UserField.PROFILE_IMAGE)
    }

    fun saveUserChanges(onSuccess: (User) -> Unit, onFailure: (String) -> Unit) {
        if (_isFormDirty.value != true) {
            logWarning("Trying to save changes on not changed form isFormDirty: $_isFormDirty")
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
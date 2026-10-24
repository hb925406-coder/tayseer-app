package com.tayseer.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tayseer.app.data.entity.Student
import com.tayseer.app.data.entity.User
import com.tayseer.app.data.repository.TayseerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    object NeedsInitialAdmin : AuthState()
    object Unauthenticated : AuthState()
    data class AuthenticatedUser(val user: User) : AuthState()
    data class AuthenticatedParent(val student: Student) : AuthState()
}

class AuthViewModel(private val repository: TayseerRepository) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        checkInitialSetup()
    }

    fun checkInitialSetup() {
        viewModelScope.launch {
            try {
                val userCount = repository.getUserCount()
                if (userCount == 0) {
                    _authState.value = AuthState.NeedsInitialAdmin
                } else if (_authState.value !is AuthState.AuthenticatedUser && _authState.value !is AuthState.AuthenticatedParent) {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _errorMessage.value = "خطأ في الاتصال بقاعدة البيانات: ${e.localizedMessage}"
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun createInitialAdmin(password: String, onComplete: () -> Unit) {
        if (password.length < 4) {
            _errorMessage.value = "يجب أن لا تقل كلمة المرور عن 4 أحرف أو أرقام"
            return
        }
        viewModelScope.launch {
            try {
                repository.createInitialAdmin(password)
                _errorMessage.value = null
                val user = repository.login("admin", password)
                if (user != null) {
                    _authState.value = AuthState.AuthenticatedUser(user)
                    onComplete()
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _errorMessage.value = "فشل إنشاء الحساب: ${e.localizedMessage}"
            }
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _errorMessage.value = "يرجى إدخال اسم المستخدم وكلمة المرور"
            return
        }
        viewModelScope.launch {
            try {
                val user = repository.login(username, password)
                if (user != null) {
                    _errorMessage.value = null
                    _authState.value = AuthState.AuthenticatedUser(user)
                } else {
                    _errorMessage.value = "اسم المستخدم أو كلمة المرور غير صحيحة"
                }
            } catch (e: Exception) {
                _errorMessage.value = "حدث خطأ أثناء تسجيل الدخول: ${e.localizedMessage}"
            }
        }
    }

    fun loginParent(accessCode: String) {
        if (accessCode.isBlank()) {
            _errorMessage.value = "يرجى إدخال رمز وصول ولي الأمر"
            return
        }
        viewModelScope.launch {
            try {
                val student = repository.loginParentByCode(accessCode)
                if (student != null) {
                    _errorMessage.value = null
                    _authState.value = AuthState.AuthenticatedParent(student)
                } else {
                    _errorMessage.value = "رمز وصول ولي الأمر غير صحيح أو غير مسجل"
                }
            } catch (e: Exception) {
                _errorMessage.value = "حدث خطأ أثناء تسجيل الدخول: ${e.localizedMessage}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun logout() {
        _authState.value = AuthState.Unauthenticated
    }
}

class AuthViewModelFactory(private val repository: TayseerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example.pulselog.ui.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.pulselog.data.model.User
import com.example.pulselog.data.repository.MistakeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: MistakeRepository) : ScreenModel {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    init {
        // Ensure default admin exists with correct credentials
        screenModelScope.launch {
            val existingAdmin = repository.getUserByUsername("admin")
            if (existingAdmin == null) {
                repository.insertUser(
                    username = "admin",
                    role = "Admin",
                    pin = "12345",
                    password = "password123"
                )
            } else if (existingAdmin.role != "Admin" || existingAdmin.pin != "12345") {
                // Force Admin role and new PIN
                repository.updateUser(
                    id = existingAdmin.id,
                    username = "admin",
                    role = "Admin",
                    pin = "12345",
                    // password = "password123"
                )
            }
        }
    }

    fun login(usernameInput: String, passInput: String, pinInput: String, onSuccess: (User) -> Unit) {
        val username = usernameInput.trim()
        val pass = passInput.trim()
        val pin = pinInput.trim()

        screenModelScope.launch {
            val user = repository.getUserByUsername(username)
            if (user != null && user.password == pass && user.pin == pin) {
                // IMPORTANT: Fetch the latest user data from the database 
                // to ensure the role is correct after any updates
                val freshUser = repository.getUserByUsername(username) ?: user
                _currentUser.value = freshUser
                _loginError.value = null
                onSuccess(freshUser)
            } else {
                _loginError.value = "Invalid Username, Password, or PIN"
            }
        }
    }

    suspend fun getUserByUsername(username: String): User? {
        return repository.getUserByUsername(username)
    }

    fun logout() {
        _currentUser.value = null
    }
}

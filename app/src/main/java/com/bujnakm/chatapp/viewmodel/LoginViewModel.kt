package com.bujnakm.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * ViewModel responsible for handling user login logic.
 *
 * This ViewModel interacts with Firebase Authentication to log users into the application.
 */
class LoginViewModel : ViewModel() {

    // Firebase Authentication instance for handling user login
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // LiveData object to observe login result
    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> get() = _loginResult

    /**
     * Logs in the user using Firebase Authentication.
     *
     * @param email The email address of the user.
     * @param password The password entered by the user.
     */
    fun login(email: String, password: String) {
        // Check if email or password fields are empty
        if (email.isBlank() || password.isBlank()) {
            _loginResult.value = LoginResult(false, "Email or password cannot be empty")
            return
        }

        // Use Firebase Authentication to log in the user
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login successful, update LiveData
                    _loginResult.value = LoginResult(true, "Login successful")
                } else {
                    // Login failed, retrieve error message
                    val errorMessage = task.exception?.message ?: "Login failed"
                    _loginResult.value = LoginResult(false, errorMessage)
                }
            }
    }
}

/**
 * Data class representing the result of a login attempt.
 *
 * @property success Indicates whether login was successful.
 * @property message Provides additional information about the login result.
 */
data class LoginResult(val success: Boolean, val message: String)

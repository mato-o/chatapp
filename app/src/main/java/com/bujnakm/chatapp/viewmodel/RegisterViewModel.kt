package com.bujnakm.chatapp.viewmodel

import RegistrationResult
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

/**
 * ViewModel responsible for handling user registration logic.
 *
 * This ViewModel manages user sign-up using Firebase Authentication and stores user details
 * in Firestore.
 */
class RegisterViewModel : ViewModel() {

    // Firebase Authentication instance for user registration
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // Firestore instance to store user data
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // LiveData object to observe registration result
    private val _registrationResult = MutableLiveData<RegistrationResult>()
    val registrationResult: LiveData<RegistrationResult> get() = _registrationResult

    /**
     * Registers a new user with the provided email, password, and username.
     *
     * @param email The email address of the user.
     * @param password The password chosen by the user.
     * @param username The username entered by the user.
     */
    fun registerUser(email: String, password: String, username: String) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        val trimmedUsername = username.trim()

        // Validate that input fields are not empty
        if (trimmedEmail.isBlank() || trimmedPassword.isBlank() || trimmedUsername.isBlank()) {
            _registrationResult.value = RegistrationResult(false, "Email, password, or username cannot be empty")
            Log.e("RegisterViewModel", "Empty email, password, or username")
            return
        }

        // Create a new user with Firebase Authentication
        firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Get the newly created user ID
                    val userId = firebaseAuth.currentUser?.uid ?: return@addOnCompleteListener

                    // Create a user object with necessary details
                    val user = hashMapOf(
                        "email" to trimmedEmail,
                        "userId" to userId,
                        "username" to trimmedUsername  // 🔥 Save username here
                    )

                    // Save user profile in Firestore
                    firestore.collection("users").document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            _registrationResult.value = RegistrationResult(true, "Registration successful")
                            Log.i("RegisterViewModel", "User profile saved successfully for userId: $userId")
                        }
                        .addOnFailureListener { e ->
                            _registrationResult.value = RegistrationResult(false, "Failed to save user data: ${e.message}")
                            Log.e("RegisterViewModel", "Firestore error: ${e.message}", e)
                        }
                } else {
                    // Handle possible errors during Firebase Authentication
                    val errorMessage = when (val exception = task.exception) {
                        is FirebaseAuthWeakPasswordException -> "Password is too weak. Must be at least 6 characters."
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                        is FirebaseAuthUserCollisionException -> "This email is already registered."
                        else -> exception?.message ?: "Registration failed"
                    }
                    _registrationResult.value = RegistrationResult(false, errorMessage)
                    Log.e("RegisterViewModel", "Firebase Auth error: $errorMessage", task.exception)
                }
            }
    }
}

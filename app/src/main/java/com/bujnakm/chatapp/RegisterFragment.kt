package com.bujnakm.chatapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.bujnakm.chatapp.databinding.FragmentRegisterBinding
import com.bujnakm.chatapp.viewmodel.RegisterViewModel

/**
 * Fragment for user registration.
 * Handles user input, validates fields, and interacts with the ViewModel to register a new user.
 */
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding // View binding for accessing UI elements
    private val registerViewModel: RegisterViewModel by viewModels() // ViewModel for managing registration logic

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        // Set up Register button click listener
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val username = binding.etUsername.text.toString().trim() // 🔥 Get username input

            // Check if any field is empty before proceeding
            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(requireContext(), "Email, password, or username cannot be empty", Toast.LENGTH_SHORT).show()
                Log.e("RegisterFragment", "Empty fields")
                return@setOnClickListener
            }

            // Call ViewModel with username
            registerViewModel.registerUser(email, password, username)
        }

        // Observe registration result
        registerViewModel.registrationResult.observe(viewLifecycleOwner) {result ->
            if (result.success) {
                Toast.makeText(requireContext(), "Registration successful! Please log in.", Toast.LENGTH_SHORT).show()
                Log.d("RegisterFragment", "Registration successful, switching to Login tab.")

                // Automatically switch to Login tab after success
                val viewPager = activity?.findViewById<ViewPager2>(R.id.view_pager)
                viewPager?.currentItem = 0 // Switch to Login tab
            } else {
                // Notify user of registration failure
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                Log.e("RegisterFragment", "Registration failed: ${result.message}")
            }
        }

        return binding.root  // Return the root view of the fragment
    }
}

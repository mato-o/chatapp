package com.bujnakm.chatapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.bujnakm.chatapp.databinding.FragmentLoginBinding
import com.bujnakm.chatapp.viewmodel.ChatActivity
import com.bujnakm.chatapp.viewmodel.LoginViewModel

/**
 * LoginFragment handles the user login functionality.
 * It allows users to enter their email and password, switch to the registration tab,
 * and navigate to the chat screen upon successful login.
 */
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding // View binding for UI elements
    private val loginViewModel: LoginViewModel by viewModels() // ViewModel for login logic handling

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using view binding
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        // Set up login button click listener
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString() // Get email input
            val password = binding.etPassword.text.toString() // Get password input
            loginViewModel.login(email, password) // Call ViewModel function to handle login
        }

        // Change Tabs Instead of Using Navigation Component
        binding.btnRegister.setOnClickListener {
            val viewPager = activity?.findViewById<ViewPager2>(R.id.view_pager)
            viewPager?.currentItem = 1 // Switch to Register Tab
        }

        // Observe ViewModel for login state
        loginViewModel.loginResult.observe(viewLifecycleOwner) { result ->
            if (result.success) {
                // Navigate to the main chat screen using activity intent
                requireActivity().startActivity(android.content.Intent(requireActivity(), ChatActivity::class.java))
                requireActivity().finish() // Close auth screen so user can't go back
            }
        }

        return binding.root // Return the inflated layout with binding
    }
}

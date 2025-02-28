package com.bujnakm.chatapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bujnakm.chatapp.viewmodel.ChatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

/**
 * The main activity that acts as the entry point of the application.
 * It checks if a user is logged in and navigates accordingly.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Firebase authentication instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Log the current user's ID (or indicate if no user is logged in)
        Log.d("MainActivity", "Current user: ${auth.currentUser?.uid ?: "No user logged in"}")

        if (auth.currentUser != null) {
            // If a user is already logged in, navigate to the chat screen
            Log.d("MainActivity", " User is logged in, opening Chats/Profile screen")
            startActivity(Intent(this, ChatActivity::class.java))
            finish() // Close MainActivity so user can't go back to login
        } else {
            // If no user is logged in, show the login/register screen
            Log.d("MainActivity", " No user logged in, showing Login/Register screen")
            setContentView(R.layout.activity_auth) // Make sure this has ViewPager2 & TabLayout
            setupAuthTabs()
        }
        // Retrieve the Firebase Cloud Messaging (FCM) token
        getFCMToken()
    }

    /**
     * Sets up the authentication tabs (Login and Register) using ViewPager2 and TabLayout.
     */
    private fun setupAuthTabs() {
        val viewPager: ViewPager2? = findViewById(R.id.view_pager)
        val tabLayout: TabLayout? = findViewById(R.id.tab_layout)

        // Check if the ViewPager2 and TabLayout exist before setting up tabs
        if (viewPager == null || tabLayout == null) {
            Log.e("MainActivity", "setupAuthTabs: view_pager or tab_layout is null. Skipping setup.")
            return
        }

        viewPager.adapter = AuthPagerAdapter(this) // Set up the adapter for the authentication pages

        // Attach the TabLayout to the ViewPager2 and set tab titles
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Login" else "Register"
        }.attach()
    }

    /**
     * Retrieves the Firebase Cloud Messaging (FCM) token for push notifications and updates it in Firestore.
     */
    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    // Log the failure if the FCM token could not be retrieved
                    Log.w("FCM", "Fetching FCM token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get the new FCM token
                val token = task.result
                Log.i("FCM", "My token: $token")

                // Update Firestore with the new token
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnCompleteListener
                val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

                userRef.update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.i("FCM", "FCM token updated successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FCM", "Failed to update FCM token", e)
                    }
            }
    }
}

package com.bujnakm.chatapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bujnakm.chatapp.model.User
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        // Check if the activity was opened from a notification (contains userId)
        if (intent.extras != null) {
            val userId = intent.extras?.getString("userId") ?: return

            // Fetch user data from Firestore based on the provided userId
            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.toObject(User::class.java)
                        if (user != null) {
                            Log.d("FCMNotificationService", "User data retrieved successfully: ${user.username}")

                            // Open the MainActivity first to ensure a smooth navigation flow
                            val mainIntent = Intent(this, MainActivity::class.java)
                            mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(mainIntent)

                            // Open MessageActivity with the user details retrieved from Firestore
                            val intent = Intent(this, MessageActivity::class.java)
                            intent.putExtra("username", user.username)
                            intent.putExtra("profilePicBase64", user.profilePicBase64)
                            intent.putExtra("userId", user.userId)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            Log.d("FCMNotificationService", "Launching MessageActivity with user: ${user.username}")
                            startActivity(intent)
                            // Close SplashActivity so the user doesn't return to it when pressing back
                            finish()
                        }
                    }
                }
        }
        else{
            // If no notification was received, show the splash screen for a short time
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish() // Close SplashActivity
            }, 1000) // 1-second delay
        }
    }
}
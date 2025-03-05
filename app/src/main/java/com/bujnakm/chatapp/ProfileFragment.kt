package com.bujnakm.chatapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import com.bujnakm.chatapp.util.Util.decodeBase64ToBitmap


/**
 * Fragment for displaying and managing the user's profile.
 * Allows users to view their username, change their profile picture, and log out.
 */
class ProfileFragment : Fragment() {

    private lateinit var tvUsername: TextView // TextView to display the username
    private lateinit var ivProfilePicture: ImageView // ImageView for the profile picture
    private lateinit var auth: FirebaseAuth // Firebase authentication instance
    private lateinit var firestore: FirebaseFirestore // Firestore database instance
    private lateinit var storage: FirebaseStorage // Firebase storage instance

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize UI elements
        tvUsername = view.findViewById(R.id.tvUsername)
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture)

        // Load user profile information from Firestore
        loadUserProfile()

        // Click to change profile picture
        ivProfilePicture.setOnClickListener {
            selectImage()
        }

        // Logout Button
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        btnLogout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Log.d("ProfileFragment", "User logged out.")

            // Restart app and go back to Login screen
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }

        return view
    }

    /**
     * Opens the image picker to allow the user to select a new profile picture.
     */
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    /**
     * Handles the result of the image picker activity.
     */
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { imageUri ->  // Safe check for null
                val bitmap = decodeUriToBitmap(imageUri)
                val base64String = encodeBitmapToBase64(bitmap)
                saveBase64ToFirestore(base64String)
                ivProfilePicture.setImageBitmap(bitmap) // Display selected image
            } ?: Log.e("ProfileFragment", "No image URI found in result data")
        }
    }

    /**
     * Converts a selected image URI to a Bitmap.
     * @param imageUri The URI of the selected image.
     * @return The decoded Bitmap.
     */
    private fun decodeUriToBitmap(imageUri: Uri): Bitmap {
        return BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(imageUri))
    }

    /**
     * Encodes a Bitmap image into a Base64 string.
     * @param bitmap The bitmap to be encoded.
     * @return The Base64 encoded string.
     */
    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream) // Compress to save space
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Saves the Base64 encoded profile picture to Firestore.
     * @param base64String The Base64 encoded profile picture.
     */
    private fun saveBase64ToFirestore(base64String: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("profilePicBase64", base64String)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Failed to save profile picture: ${e.message}")
            }
    }

    /**
     * Loads the user's profile information from Firestore.
     * This includes their username and profile picture (if available).
     */
    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: "Unknown User"
                    tvUsername.text = username

                    val base64String = document.getString("profilePicBase64")
                    if (!base64String.isNullOrEmpty()) {
                        ivProfilePicture.setImageBitmap(decodeBase64ToBitmap(base64String))
                    }
                }
            }
    }
}

package com.bujnakm.chatapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bujnakm.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Fragment for starting a new chat.
 * Displays a list of users that the current user can chat with.
 */
class NewChatFragment : DialogFragment() {

    private lateinit var recyclerViewUsers: RecyclerView // RecyclerView to display users
    private lateinit var userListAdapter: UserListAdapter // Adapter for the user list
    private val userList = mutableListOf<User>() // List to hold users

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance() // Firestore instance
    private val auth: FirebaseAuth = FirebaseAuth.getInstance() // Firebase Authentication instance

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_new_chat, container, false)

        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers)
        recyclerViewUsers.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the adapter and set click listener for user selection
        userListAdapter = UserListAdapter(userList) { user ->
            startChat(user)
        }
        recyclerViewUsers.adapter = userListAdapter

        loadUsers() // Load users from Firestore

        return view
    }

    override fun onStart(){
        super.onStart()
        // Set the dialog to full screen
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /**
     * Fetches the list of users from Firestore, excluding the current user.
     */
    private fun loadUsers() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                userList.clear()
                for (document in documents) {
                    val user = document.toObject(User::class.java)
                    if (user.userId != currentUserId) { // Exclude the current user
                        userList.add(user)
                    }
                }
                userListAdapter.notifyDataSetChanged() // Refresh the adapter with new data
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load users", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Starts a chat with the selected user.
     * First checks if a chat already exists before creating a new one.
     */
    private fun startChat(user: User) {
        val currentUserId = auth.currentUser?.uid ?: return

        // Check if a chat already exists between these two users
        firestore.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val participants = document.get("participants") as? List<String>
                    if (participants != null && participants.contains(user.userId)) {
                        // Found an existing chat, open it instead of creating a new one
                        openChat(document.id, user.userId)
                        return@addOnSuccessListener
                    }
                }

                // If no chat exists, create a new one
                createNewChat(user)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error checking chat existence", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Creates a new chat if one does not already exist.
     */
    private fun createNewChat(user: User) {
        val currentUserId = auth.currentUser?.uid ?: return
        val chatId = firestore.collection("chats").document().id

        val chat = hashMapOf(
            "chatId" to chatId,
            "participants" to listOf(currentUserId, user.userId),
            "lastMessage" to "",
            "timestamp" to System.currentTimeMillis().toString()
        )

        // Store the new chat in Firestore
        firestore.collection("chats").document(chatId)
            .set(chat)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Chat started with ${user.username}!", Toast.LENGTH_SHORT).show()
                openChat(chatId, user.userId) // Open the newly created chat
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to start chat", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Opens an existing chat and retrieves user information.
     */
    private fun openChat(chatId: String, otherUserId: String) {
        // Gets user information from Firestore
        firestore.collection("users").document(otherUserId)
            .get()
            .addOnSuccessListener { document ->
                val otherUser = document.toObject(User::class.java)
                if (otherUser != null) {
                    val intent = Intent(requireContext(), MessageActivity::class.java).apply {
                        putExtra("chatId", chatId)
                        putExtra("userId", otherUser.userId)
                        putExtra("username", otherUser.username) // ✅ Make sure username is set
                        putExtra("profilePicBase64", otherUser.profilePicBase64) // ✅ Make sure profile picture is set
                    }
                    startActivity(intent)
                    dismiss() // Close the dialog
                } else {
                    Toast.makeText(requireContext(), "Failed to retrieve user details", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error retrieving user details", Toast.LENGTH_SHORT).show()
            }
    }


}

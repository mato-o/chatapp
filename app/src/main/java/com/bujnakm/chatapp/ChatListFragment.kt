package com.bujnakm.chatapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bujnakm.chatapp.model.Chat
import com.bujnakm.chatapp.viewmodel.ChatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Fragment that displays a list of existing chats for the current user.
 * It shows a "No Chats Available" message if there are no chats.
 * It also provides a FloatingActionButton to create a new chat.
 */
class ChatListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView // RecyclerView to display chat list
    private lateinit var chatListAdapter: ChatListAdapter // Adapter for the chat list
    private lateinit var tvNoChats: TextView // TextView to display when no chats are available

    private val chatList = mutableListOf<Chat>() // List to store Chat objects
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance() // Firestore instance
    private val auth: FirebaseAuth = FirebaseAuth.getInstance() // Firebase Authentication instance

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat_list, container, false)

        // Initialize RecyclerView and "No Chats" TextView
        recyclerView = view.findViewById(R.id.recyclerViewChats)
        tvNoChats = view.findViewById(R.id.tvNoChats) // Reference to "No Chats Available" message

        // Set layout manager for RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Initialize adapter with an empty chat list and set click listener for chat items
        chatListAdapter = ChatListAdapter(chatList) { chat ->
            navigateToChat(chat) // Open chat when it is clicked
        }
        recyclerView.adapter = chatListAdapter

        // Load chats from Firestore
        loadChats()

        return view
    }

    /**
     * Listens for changes in the "chats" collection for the current user and updates the chat list.
     */
    private fun loadChats() {
        val userId = auth.currentUser?.uid ?: return

        // Query Firestore for chats where the current user is a participant
        firestore.collection("chats")
            .whereArrayContains("participants", userId) // Get only chats where the user is a participant
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Failed to load chats: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Clear existing chat list
                chatList.clear()
                if (snapshots == null || snapshots.isEmpty) {
                    // If no chats are found, display the "No Chats Available" message and hide the RecyclerView
                    tvNoChats.visibility = View.VISIBLE  // Show "No Chats Available" message
                    recyclerView.visibility = View.GONE  // Hide RecyclerView
                } else {
                    // If chats are found, hide the "No Chats Available" message and show the RecyclerView
                    tvNoChats.visibility = View.GONE  // Hide message
                    recyclerView.visibility = View.VISIBLE  // Show RecyclerView

                    // Iterate through each chat document
                    for (document in snapshots) {
                        val chat = document.toObject(Chat::class.java)
                        // Determine the other user's ID by finding the participant that is not the current user
                        val otherUserId = chat.participants.find { it != userId } // Get the other user

                        if (otherUserId != null) {
                            // Fetch other user's details from Firestore
                            firestore.collection("users").document(otherUserId)
                                .get()
                                .addOnSuccessListener { userDoc ->
                                    if (userDoc.exists()) {
                                        val username = userDoc.getString("username") ?: "Unknown User"
                                        val profilePicBase64 = userDoc.getString("profilePicBase64") ?: ""

                                        // Set the retrieved username and profile picture into the Chat object
                                        chat.username = username // Set username in Chat object
                                        chat.profilePicBase64 = profilePicBase64 // Set profile picture

                                        // Notify the adapter that the data has changed
                                        chatListAdapter.notifyDataSetChanged() // Refresh list
                                    }
                                }
                        }
                        // Add the chat to the list regardless; user details may be updated asynchronously
                        chatList.add(chat)
                    }
                    // Notify adapter after processing all chat documents
                    chatListAdapter.notifyDataSetChanged()
                }
            }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize and set click listener on the FloatingActionButton to create a new chat
        val fabNewChat = view.findViewById<FloatingActionButton>(R.id.fabNewChat)
        fabNewChat.setOnClickListener {
            Toast.makeText(requireContext(), "Opening New Chat...", Toast.LENGTH_SHORT).show()
            // Call the parent activity's method to open the new chat fragment
            (requireActivity() as ChatActivity).openNewChatFragment()
        }
    }

    /**
     * Navigates to a chat conversation.
     * Retrieves the other participant's user details from Firestore before launching MessageActivity.
     *
     * @param chat The Chat object representing the selected conversation.
     */
    private fun navigateToChat(chat: Chat) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        // Fetch chat details to find the other participant
        firestore.collection("chats").document(chat.chatId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val participants = document.get("participants") as? List<String>
                    if (participants != null) {
                        // Determine the other user's ID
                        val otherUserId = participants.find { it != currentUserId } ?: return@addOnSuccessListener

                        // Fetch the other user's details
                        firestore.collection("users").document(otherUserId)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                if (userDoc.exists()) {
                                    val username = userDoc.getString("username") ?: "Unknown"
                                    val profilePicBase64 = userDoc.getString("profilePicBase64") ?: ""

                                    // Start `MessageActivity` with correct user data
                                    val intent = Intent(requireContext(), MessageActivity::class.java).apply {
                                        putExtra("chatId", chat.chatId)
                                        putExtra("userId", otherUserId)
                                        putExtra("username", username)
                                        putExtra("profilePicBase64", profilePicBase64)
                                    }
                                    startActivity(intent)
                                } else {
                                    // Log error if user document is missing
                                    Log.e("ChatListFragment", "User document not found for $otherUserId")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("ChatListFragment", "Error fetching user details: ${e.message}")
                            }
                    }
                } else {
                    Log.e("ChatListFragment", "Chat document not found for ${chat.chatId}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChatListFragment", "Error fetching chat details: ${e.message}")
            }
    }


}

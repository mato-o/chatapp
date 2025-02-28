package com.bujnakm.chatapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bujnakm.chatapp.model.ChatMessageModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.bujnakm.chatapp.util.Util.decodeBase64ToBitmap

/**
 * Activity for displaying and handling messages in a chat.
 */
class MessageActivity : AppCompatActivity() {
    private lateinit var chatId: String // The unique ID of the chat
    private lateinit var adapter: ChatRecyclerAdapter // Adapter for RecyclerView
    private lateinit var username: String // Username of the chat partner
    private lateinit var profilePicBase64: String // Profile picture of the chat partner

    private lateinit var messageInput: EditText // Input field for typing messages
    private lateinit var sendMessageBtn: ImageButton // Button to send messages
    private lateinit var backBtn: ImageButton // Button to navigate back
    private lateinit var otherUsername: TextView // TextView to display chat partner's name
    private lateinit var recyclerView: RecyclerView // RecyclerView to display messages

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Initialize UI components
        backBtn = findViewById(R.id.back_btn)
        recyclerView = findViewById(R.id.chat_recycle_view)
        otherUsername = findViewById(R.id.other_username)
        messageInput=findViewById(R.id.chat_message_input)
        sendMessageBtn=findViewById(R.id.message_send_btn)

        // Handle back button click
        backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Retrieve intent extras (chat details)
        val otherUserId = intent.getStringExtra("userId") ?: return
        username = intent.getStringExtra("username") ?: "Unknown"
        profilePicBase64 = intent.getStringExtra("profilePicBase64") ?: ""
        otherUsername.text = username

        // Load profile picture if available
        val profileImageView = findViewById<ImageView>(R.id.profile_picture_image_view)
        if (profilePicBase64.isNotEmpty()) {
            profileImageView.setImageBitmap(decodeBase64ToBitmap(profilePicBase64))
        }

        setupChatRecyclerView()

        // Handle send message button click
        sendMessageBtn.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isEmpty()) {
                return@setOnClickListener
            }
            sendMessageToUser(message)
        }
        // Retrieve or create a chat ID if needed
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        retrieveOrCreateChatId(currentUserId, otherUserId)
        setupChatRecyclerView()
    }

    /**
     * Checks if a chat already exists between the two users, if not, it creates a new chat.
     */
    private fun retrieveOrCreateChatId(currentUserId: String, otherUserId: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Query Firestore for an existing chat where both users are participants
        firestore.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val participants = document.get("participants") as? List<String>
                    if (participants != null && participants.contains(otherUserId)) {
                        // Found an existing chat, use this chatId
                        this.chatId = document.id
                        setupChatRecyclerView()
                        return@addOnSuccessListener
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("MessageActivity", "Error retrieving chatId", e)
            }
    }

    /**
     * Sends a message to the Firestore database and updates the chat information.
     */
    private fun sendMessageToUser(message: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chatroomRef = FirebaseFirestore.getInstance().collection("chats").document(chatId)

        // Update chatroom fields (Last Message + Timestamp)
        chatroomRef.update(
            mapOf(
                "lastMessage" to message,
                "lastMessageTimestamp" to Timestamp.now(),
                "lastMessageSenderId" to userId
            )
        ).addOnSuccessListener {
            messageInput.setText("") // Clear input field after sending
        }.addOnFailureListener {

        }

        // Save the actual message
        val chatMessageModel = ChatMessageModel(message, userId, Timestamp.now())
        chatroomRef.collection("messages").add(chatMessageModel)
    }

    /**
     * Sets up the RecyclerView to display chat messages.
     */
    private fun setupChatRecyclerView() {
        if (!::chatId.isInitialized) return // Ensure chatId is available
        val query = FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatMessageModel>()
            .setQuery(query, ChatMessageModel::class.java)
            .build()

        adapter = ChatRecyclerAdapter(options)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Makes the RecyclerView start at the bottom
        }
        recyclerView.adapter = adapter
        adapter.startListening() // Start listening for real-time updates

        // Scroll to newest message on data change
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerView.scrollToPosition(adapter.itemCount - 1) // Scroll to last message
            }
        })
    }
}

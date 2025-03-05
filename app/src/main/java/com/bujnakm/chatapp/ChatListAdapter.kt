package com.bujnakm.chatapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bujnakm.chatapp.model.Chat
import com.bujnakm.chatapp.util.Util.decodeBase64ToBitmap

/**
 * Adapter for displaying a list of chat conversations in a RecyclerView.
 * Each item in the list represents a chat with a user, including profile picture, username, and last message.
 *
 * @param chatList The list of chat objects to display.
 * @param onChatClick A lambda function that is triggered when a chat item is clicked.
 */
class ChatListAdapter(
    private val chatList: List<Chat>,
    private val onChatClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    /**
     * ViewHolder class that represents each chat item in the list.
     * It holds references to UI elements inside the item layout.
     */
    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.ivProfilePicture) // Profile picture of the user
        val username: TextView = view.findViewById(R.id.tvUsername) // Username of the chat participant
        val lastMessage: TextView = view.findViewById(R.id.tvLastMessage) // Last message in the chat
    }

    /**
     * Inflates the item layout for the chat list and returns a ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    /**
     * Binds the chat data to the corresponding views inside the ViewHolder.
     */
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        holder.username.text = chat.username // Set the username
        holder.lastMessage.text = chat.lastMessage // Set the last message

        // If a profile picture exists, decode and set it. Otherwise, use a default image.
        chat.profilePicBase64?.takeIf { it.isNotEmpty() }?.let { base64String ->
            val decodedBitmap = decodeBase64ToBitmap(base64String)
            holder.profileImage.setImageBitmap(decodedBitmap)
        } ?: holder.profileImage.setImageResource(R.drawable.ic_user) // Default profile image

        // Set a click listener on the item to navigate to the chat when clicked.
        holder.itemView.setOnClickListener { onChatClick(chat) }
    }
    /**
     * Returns the total number of chat items in the list.
     */
    override fun getItemCount() = chatList.size
}


package com.bujnakm.chatapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bujnakm.chatapp.model.ChatMessageModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ChatRecyclerAdapter is responsible for displaying chat messages in a RecyclerView.
 * It extends FirestoreRecyclerAdapter to automatically update the chat when new messages arrive.
 */
class ChatRecyclerAdapter(options: FirestoreRecyclerOptions<ChatMessageModel>) :
    FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder>(options) {

    /**
     * Creates a new ViewHolder for each chat message item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatModelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_message_recycler_row, parent, false) // Inflate message layout
        return ChatModelViewHolder(view)
    }

    /**
     * Binds the message data to the ViewHolder.
     */
    override fun onBindViewHolder(holder: ChatModelViewHolder, position: Int, model: ChatMessageModel) {
        holder.bind(model)
    }

    /**
     * ViewHolder class that holds references to chat message views.
     */
    class ChatModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val leftChatLayout: LinearLayout = itemView.findViewById(R.id.left_chat_layout)
        private val rightChatLayout: LinearLayout = itemView.findViewById(R.id.right_chat_layout)
        private val leftChatTextView: TextView = itemView.findViewById(R.id.left_chat_textview)
        private val rightChatTextView: TextView = itemView.findViewById(R.id.right_chat_textview)
        private val leftTimestampTextView: TextView = itemView.findViewById(R.id.left_chat_timestamp) // Timestamp for received messages
        private val rightTimestampTextView: TextView = itemView.findViewById(R.id.right_chat_timestamp) // Timestamp for sent messages

        /**
         * Binds the message elements to the appropriate UI elements
         */
        fun bind(chatMessage: ChatMessageModel) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            // Convert timestamp to a readable format
            val formattedTime = formatTimestamp(chatMessage.timestamp.toDate())

            if (chatMessage.senderId == currentUserId) {
                // Message sent by the current user (align right)
                rightChatLayout.visibility = View.VISIBLE
                leftChatLayout.visibility = View.GONE
                rightChatTextView.text = chatMessage.message
                rightTimestampTextView.text = formattedTime
            } else {
                // Message received (align left)
                leftChatLayout.visibility = View.VISIBLE
                rightChatLayout.visibility = View.GONE
                leftChatTextView.text = chatMessage.message
                leftTimestampTextView.text = formattedTime
            }
        }

        /**
         * Formats the timestamp to a more human-readable format
         */
        private fun formatTimestamp(date: Date?): String {
            return if (date != null) {
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                sdf.format(date)
            } else {
                "Unknown time"
            }
        }
    }

}

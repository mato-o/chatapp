package com.bujnakm.chatapp.model

import com.google.firebase.Timestamp

/**
 * Data class representing a chat between users.
 *
 * @property chatId The unique identifier of the chat.
 * @property username The username of the other participant in the chat.
 * @property lastMessage The last message sent in the chat.
 * @property lastMessageTimestamp The timestamp of the last message sent (nullable).
 * @property lastMessageSenderId The user ID of the sender of the last message.
 * @property profilePicBase64 The Base64-encoded profile picture of the other participant (nullable).
 * @property participants A list of user IDs participating in this chat.
 */
data class Chat(
    val chatId: String = "", // Unique identifier for the chat
    var username: String = "", // The username of the other participant
    val lastMessage: String = "", // Last message sent in the chat
    val lastMessageTimestamp: Timestamp? =null, // Timestamp of the last message
    val lastMessageSenderId:String="", // ID of the user who sent the last message
    var profilePicBase64: String? = null, // Profile picture of the other participant
    val participants: List<String> = emptyList() // List of user IDs in the chat
)

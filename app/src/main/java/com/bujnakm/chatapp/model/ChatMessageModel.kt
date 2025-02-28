package com.bujnakm.chatapp.model

import com.google.firebase.Timestamp


/**
 * Data class representing a chat message.
 *
 * @property message The content of the message.
 * @property senderId The unique identifier of the user who sent the message.
 * @property timestamp The time at which the message was sent, using Firebase Timestamp.
 */
data class ChatMessageModel(
    var message: String = "", // The text of the message
    var senderId: String = "", // The sender ID
    var timestamp: Timestamp = Timestamp.now() //Timestamp
)

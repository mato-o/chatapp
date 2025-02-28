package com.bujnakm.chatapp.model

/**
 * Data class representing a User in the chat application.
 *
 * @property userId Unique identifier for the user (Firebase Authentication UID).
 * @property username Display name of the user.
 * @property email Email address of the user.
 * @property profilePicBase64 Base64-encoded string representing the user's profile picture.
 * @property FCMToken Firebase Cloud Messaging (FCM) token for push notifications.
 */
data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val profilePicBase64: String = "",
    val FCMToken: String="" // Used to send notifications
)

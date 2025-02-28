/**
 * Data class representing the result of a user registration attempt.
 *
 * @param success Indicates whether the registration was successful (true) or failed (false).
 * @param message A message providing additional details about the registration outcome,
 *                such as error messages or confirmation messages.
 */
data class RegistrationResult(
    val success: Boolean,  // True if registration was successful, false otherwise
    val message: String    // Message describing the outcome of the registration process
)

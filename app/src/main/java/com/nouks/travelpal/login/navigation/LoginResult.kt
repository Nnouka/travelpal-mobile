package com.nouks.travelpal.login.navigation

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: Int? = null,
    val error: Int? = null
)

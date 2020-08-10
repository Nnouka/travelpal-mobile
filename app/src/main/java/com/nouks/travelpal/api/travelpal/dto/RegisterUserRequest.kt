package com.nouks.travelpal.api.travelpal.dto

data class RegisterUserRequest(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val isDriver: Boolean = false
)
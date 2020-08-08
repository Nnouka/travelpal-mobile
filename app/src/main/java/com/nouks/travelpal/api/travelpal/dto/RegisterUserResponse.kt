package com.nouks.travelpal.api.travelpal.dto

data class RegisterUserResponse(
    val name: String,
    val email: String,
    val updatedAt: String,
    val userId: Long,
    val roles: List<String>
)

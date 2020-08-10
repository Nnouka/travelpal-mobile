package com.nouks.travelpal.api.travelpal.dto

import android.provider.ContactsContract

data class RegisterUserResponse(
    val name: String,
    val email: String,
    val phone: String,
    val updatedAt: String,
    val userId: Long,
    val roles: List<String>
)

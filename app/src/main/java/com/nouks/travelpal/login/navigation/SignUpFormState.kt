package com.nouks.travelpal.login.navigation

data class SignUpFormState(
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)
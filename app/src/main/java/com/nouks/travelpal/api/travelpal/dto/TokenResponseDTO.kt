package com.nouks.travelpal.api.travelpal.dto

import android.bluetooth.BluetoothHeadset

data class TokenResponseDTO(
    val header: String,
    val issuer: String,
    val accessToken: String,
    val refreshToken: String,
    val type: String,
    val expiresAt: Long
)
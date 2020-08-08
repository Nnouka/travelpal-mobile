package com.nouks.travelpal.api.travelpal

import android.util.Base64


class AuthService {
    fun generateClientAuthHeader(clientId: String, clientSecret: String): String {
        return "Basic ${Base64.encodeToString("$clientId:$clientSecret".toByteArray(), Base64.DEFAULT)}"
    }
}
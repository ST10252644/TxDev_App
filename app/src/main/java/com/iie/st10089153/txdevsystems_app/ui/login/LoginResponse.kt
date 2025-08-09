package com.iie.st10089153.txdevsystems_app.ui.login

data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String? = null
)
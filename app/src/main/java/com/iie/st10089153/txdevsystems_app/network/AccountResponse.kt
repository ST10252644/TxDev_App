package com.iie.st10089153.txdevsystems_app.network

data class AccountResponse(
    val Admin: Boolean,
    val address: String?,
    val cell: String?,
    val comment: String?,
    val company_name: String?,
    val count_logins: String?,
    val email: String?,
    val first_name: String?,
    val last_name: String?,
    val id: Int,
    val office_nr: String?,
    val timestamp: String?,
    val user_active: Boolean,
    val username: String
)
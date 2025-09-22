// app/src/main/java/com/iie/st10089153/txdevsystems_app/security/SecurityConfig.kt
package com.iie.st10089153.txdevsystems_app.security

object SecurityConfig {
    const val SESSION_TIMEOUT_MINUTES = 30
    const val MAX_LOGIN_ATTEMPTS = 3
    const val PASSWORD_MIN_LENGTH = 8
    const val TOKEN_EXPIRY_HOURS = 24
    const val KEYSTORE_ALIAS = "TMS_AUTH_KEY"

    // Password complexity requirements - Made flexible for existing accounts
    const val REQUIRE_UPPERCASE = false  // Set to false to allow existing passwords
    const val REQUIRE_LOWERCASE = true
    const val REQUIRE_DIGITS = true
    const val REQUIRE_SPECIAL_CHARS = false  // Set to false to allow existing passwords

    // Legacy password support
    const val ALLOW_LEGACY_PASSWORDS = true
    val LEGACY_PASSWORDS = listOf("stusent@123", "admin@123") // Add your existing passwords here
}
// app/src/main/java/com/iie/st10089153/txdevsystems_app/security/PasswordValidator.kt
package com.iie.st10089153.txdevsystems_app.security

import java.util.regex.Pattern

class PasswordValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )

    fun validatePassword(password: String, isNewPassword: Boolean = false): ValidationResult {
        val errors = mutableListOf<String>()

        // Allow legacy passwords for existing users
        if (SecurityConfig.ALLOW_LEGACY_PASSWORDS &&
            SecurityConfig.LEGACY_PASSWORDS.contains(password) &&
            !isNewPassword) {
            return ValidationResult(true, emptyList())
        }

        if (password.length < SecurityConfig.PASSWORD_MIN_LENGTH) {
            errors.add("Password must be at least ${SecurityConfig.PASSWORD_MIN_LENGTH} characters long")
        }

        if (SecurityConfig.REQUIRE_UPPERCASE && !password.any { it.isUpperCase() }) {
            errors.add("Password must contain at least one uppercase letter")
        }

        if (SecurityConfig.REQUIRE_LOWERCASE && !password.any { it.isLowerCase() }) {
            errors.add("Password must contain at least one lowercase letter")
        }

        if (SecurityConfig.REQUIRE_DIGITS && !password.any { it.isDigit() }) {
            errors.add("Password must contain at least one digit")
        }

        if (SecurityConfig.REQUIRE_SPECIAL_CHARS && !password.any { !it.isLetterOrDigit() }) {
            errors.add("Password must contain at least one special character")
        }

        // Only check for weak passwords on new passwords
        if (isNewPassword) {
            val commonWeakPasswords = listOf("password", "123456", "qwerty", "admin")
            if (commonWeakPasswords.any { password.lowercase().contains(it) }) {
                errors.add("Password contains common weak patterns")
            }
        }

        return ValidationResult(errors.isEmpty(), errors)
    }
}
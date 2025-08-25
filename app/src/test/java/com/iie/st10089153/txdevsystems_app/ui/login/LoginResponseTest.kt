package com.iie.st10089153.txdevsystems_app.ui.login

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class LoginResponseTest {

    private val gson = Gson()

    @Test
    fun testLoginResponse_Creation() {
        // Given
        val accessToken = "test_access_token"
        val tokenType = "Bearer"
        val expiresIn = 3600
        val refreshToken = "test_refresh_token"

        // When
        val loginResponse = LoginResponse(
            access_token = accessToken,
            token_type = tokenType,
            expires_in = expiresIn,
            refresh_token = refreshToken
        )

        // Then
        assertEquals(accessToken, loginResponse.access_token)
        assertEquals(tokenType, loginResponse.token_type)
        assertEquals(expiresIn, loginResponse.expires_in)
        assertEquals(refreshToken, loginResponse.refresh_token)
    }

    @Test
    fun testLoginResponse_WithNullRefreshToken() {
        // Given
        val accessToken = "test_access_token"
        val tokenType = "Bearer"
        val expiresIn = 3600

        // When
        val loginResponse = LoginResponse(
            access_token = accessToken,
            token_type = tokenType,
            expires_in = expiresIn,
            refresh_token = null
        )

        // Then
        assertEquals(accessToken, loginResponse.access_token)
        assertEquals(tokenType, loginResponse.token_type)
        assertEquals(expiresIn, loginResponse.expires_in)
        assertNull(loginResponse.refresh_token)
    }

    @Test
    fun testLoginResponse_DefaultRefreshTokenIsNull() {
        // Given
        val accessToken = "test_access_token"
        val tokenType = "Bearer"
        val expiresIn = 3600

        // When
        val loginResponse = LoginResponse(
            access_token = accessToken,
            token_type = tokenType,
            expires_in = expiresIn
        )

        // Then
        assertEquals(accessToken, loginResponse.access_token)
        assertEquals(tokenType, loginResponse.token_type)
        assertEquals(expiresIn, loginResponse.expires_in)
        assertNull(loginResponse.refresh_token)
    }

    @Test
    fun testLoginResponse_JsonDeserialization() {
        // Given
        val jsonString = """
            {
                "access_token": "sample_token_123",
                "token_type": "Bearer",
                "expires_in": 7200,
                "refresh_token": "sample_refresh_456"
            }
        """.trimIndent()

        // When
        val loginResponse = gson.fromJson(jsonString, LoginResponse::class.java)

        // Then
        assertEquals("sample_token_123", loginResponse.access_token)
        assertEquals("Bearer", loginResponse.token_type)
        assertEquals(7200, loginResponse.expires_in)
        assertEquals("sample_refresh_456", loginResponse.refresh_token)
    }

    @Test
    fun testLoginResponse_JsonDeserializationWithoutRefreshToken() {
        // Given
        val jsonString = """
            {
                "access_token": "sample_token_123",
                "token_type": "Bearer",
                "expires_in": 7200
            }
        """.trimIndent()

        // When
        val loginResponse = gson.fromJson(jsonString, LoginResponse::class.java)

        // Then
        assertEquals("sample_token_123", loginResponse.access_token)
        assertEquals("Bearer", loginResponse.token_type)
        assertEquals(7200, loginResponse.expires_in)
        assertNull(loginResponse.refresh_token)
    }

    @Test
    fun testLoginResponse_JsonSerialization() {
        // Given
        val loginResponse = LoginResponse(
            access_token = "serialization_token",
            token_type = "Bearer",
            expires_in = 1800,
            refresh_token = "serialization_refresh"
        )

        // When
        val jsonString = gson.toJson(loginResponse)

        // Then
        assertTrue(jsonString.contains("\"access_token\":\"serialization_token\""))
        assertTrue(jsonString.contains("\"token_type\":\"Bearer\""))
        assertTrue(jsonString.contains("\"expires_in\":1800"))
        assertTrue(jsonString.contains("\"refresh_token\":\"serialization_refresh\""))
    }

    @Test
    fun testLoginResponse_JsonSerializationWithNullRefreshToken() {
        // Given
        val loginResponse = LoginResponse(
            access_token = "serialization_token",
            token_type = "Bearer",
            expires_in = 1800,
            refresh_token = null
        )

        // When
        val jsonString = gson.toJson(loginResponse)

        // Then
        assertTrue(jsonString.contains("\"access_token\":\"serialization_token\""))
        assertTrue(jsonString.contains("\"token_type\":\"Bearer\""))
        assertTrue(jsonString.contains("\"expires_in\":1800"))
        // Gson by default doesn't serialize null values
        assertFalse(jsonString.contains("refresh_token"))
    }
}
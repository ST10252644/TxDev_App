package com.iie.st10089153.txdevsystems_app.network.api

import com.iie.st10089153.txdevsystems_app.network.Api.AuthApi

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

class AuthApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var authApi: AuthApi

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authApi = retrofit.create(AuthApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testLoginApiCall_Success() = runBlocking {
        // Given
        val mockResponseBody = """
            {
                "access_token": "test_access_token_123",
                "token_type": "Bearer",
                "expires_in": 3600,
                "refresh_token": "test_refresh_token_456"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(mockResponseBody)
        )

        // When
        val response = authApi.login(
            grantType = "password",
            username = "testuser",
            password = "testpass"
        ).execute()

        // Then
        assertTrue(response.isSuccessful)
        assertNotNull(response.body())

        val loginResponse = response.body()!!
        assertEquals("test_access_token_123", loginResponse.access_token)
        assertEquals("Bearer", loginResponse.token_type)
        assertEquals(3600, loginResponse.expires_in)
        assertEquals("test_refresh_token_456", loginResponse.refresh_token)

        // Verify request
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/token", request.path)
        assertEquals("application/x-www-form-urlencoded", request.getHeader("Content-Type"))

        val requestBody = request.body.readUtf8()
        assertTrue(requestBody.contains("grant_type=password"))
        assertTrue(requestBody.contains("username=testuser"))
        assertTrue(requestBody.contains("password=testpass"))
    }

    @Test
    fun testLoginApiCall_InvalidCredentials() = runBlocking {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .setBody("{\"error\": \"invalid_grant\"}")
        )

        // When
        val response = authApi.login(
            username = "wronguser",
            password = "wrongpass"
        ).execute()

        // Then
        assertFalse(response.isSuccessful)
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.code())

        // Verify request
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/token", request.path)
    }

    @Test
    fun testLoginApiCall_ServerError() = runBlocking {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
        )

        // When
        val response = authApi.login(
            username = "testuser",
            password = "testpass"
        ).execute()

        // Then
        assertFalse(response.isSuccessful)
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.code())
    }

    @Test
    fun testLoginApiCall_DefaultGrantType() = runBlocking {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("""
                    {
                        "access_token": "token123",
                        "token_type": "Bearer",
                        "expires_in": 3600
                    }
                """.trimIndent())
        )

        // When - not specifying grant_type should use default "password"
        val response = authApi.login(
            username = "testuser",
            password = "testpass"
        ).execute()

        // Then
        assertTrue(response.isSuccessful)

        val request = mockWebServer.takeRequest()
        val requestBody = request.body.readUtf8()
        assertTrue(requestBody.contains("grant_type=password"))
    }
}
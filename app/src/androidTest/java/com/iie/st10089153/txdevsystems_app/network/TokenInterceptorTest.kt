package com.iie.st10089153.txdevsystems_app.network


import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class TokenInterceptorTest {

    private lateinit var tokenInterceptor: TokenInterceptor
    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockChain: Interceptor.Chain
    private lateinit var mockRequest: Request
    private lateinit var mockRequestBuilder: Request.Builder
    private lateinit var mockResponse: Response

    @Before
    fun setup() {
        mockContext = mockk()
        mockSharedPreferences = mockk()
        mockChain = mockk()
        mockRequest = mockk()
        mockRequestBuilder = mockk()
        mockResponse = mockk()

        tokenInterceptor = TokenInterceptor(mockContext)

        every { mockContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) } returns mockSharedPreferences
        every { mockChain.request() } returns mockRequest
        every { mockRequest.newBuilder() } returns mockRequestBuilder
        every { mockRequestBuilder.addHeader(any(), any()) } returns mockRequestBuilder
        every { mockRequestBuilder.build() } returns mockRequest
        every { mockChain.proceed(any()) } returns mockResponse
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testIntercept_WithValidToken_AddsAuthorizationHeader() {
        // Given
        val testToken = "test_access_token_123"
        every { mockSharedPreferences.getString("access_token", null) } returns testToken

        // When
        val response = tokenInterceptor.intercept(mockChain)

        // Then
        verify { mockRequestBuilder.addHeader("Authorization", "Bearer $testToken") }
        verify { mockChain.proceed(mockRequest) }
        assertEquals(mockResponse, response)
    }

    @Test
    fun testIntercept_WithNullToken_DoesNotAddAuthorizationHeader() {
        // Given
        every { mockSharedPreferences.getString("access_token", null) } returns null

        // When
        val response = tokenInterceptor.intercept(mockChain)

        // Then
        verify(exactly = 0) { mockRequestBuilder.addHeader("Authorization", any()) }
        verify { mockChain.proceed(mockRequest) }
        assertEquals(mockResponse, response)
    }

    @Test
    fun testIntercept_WithEmptyToken_DoesNotAddAuthorizationHeader() {
        // Given
        every { mockSharedPreferences.getString("access_token", null) } returns ""

        // When
        val response = tokenInterceptor.intercept(mockChain)

        // Then
        verify(exactly = 0) { mockRequestBuilder.addHeader("Authorization", any()) }
        verify { mockChain.proceed(mockRequest) }
        assertEquals(mockResponse, response)
    }



    @Test
    fun testIntercept_UsesCorrectSharedPreferencesKey() {
        // Given
        every { mockSharedPreferences.getString("access_token", null) } returns "token123"

        // When
        tokenInterceptor.intercept(mockChain)

        // Then
        verify { mockContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) }
        verify { mockSharedPreferences.getString("access_token", null) }
    }

    @Test
    fun testIntercept_CallsChainProceedWithModifiedRequest() {
        // Given
        val testToken = "valid_token"
        every { mockSharedPreferences.getString("access_token", null) } returns testToken

        // When
        tokenInterceptor.intercept(mockChain)

        // Then
        verify { mockRequestBuilder.build() }
        verify { mockChain.proceed(mockRequest) }
    }
}
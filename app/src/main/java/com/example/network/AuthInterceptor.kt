package com.example.network

import android.util.Log
import com.example.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    private val TAG = "AuthInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        val url = originalRequest.url.toString()
        if (BuildConfig.DEBUG) Log.d(TAG, "Intercepted request to URL: $url")

        val token = try {
            val t = tokenProvider()
            if (BuildConfig.DEBUG) Log.d(TAG, "Token retrieved from provider (length: ${t?.length})")
            t
        } catch (e: Exception) {
            Log.e(TAG, "Error calling tokenProvider()", e)
            null
        }

        if (!token.isNullOrEmpty()) {
            val authHeaderValue = "Bearer $token"
            requestBuilder.header("Authorization", authHeaderValue)
            if (BuildConfig.DEBUG) Log.d(TAG, "Added Authorization header")
        } else {
            if (BuildConfig.DEBUG) Log.w(TAG, "Token is null or empty, NOT adding Authorization header!")
        }

        val finalRequest = requestBuilder.build()
        
        if (BuildConfig.DEBUG) {
            // Log all final headers to make 100% sure what OkHttp is sending (except auth value)
            Log.d(TAG, "--- Final Request Headers for ${finalRequest.url} ---")
            finalRequest.headers.forEach { pair ->
                if (pair.first.equals("Authorization", ignoreCase = true)) {
                    Log.d(TAG, "${pair.first}: [REDACTED]")
                } else {
                    Log.d(TAG, "${pair.first}: ${pair.second}")
                }
            }
            Log.d(TAG, "----------------------------------------------------")
        }

        return chain.proceed(finalRequest)
    }
}

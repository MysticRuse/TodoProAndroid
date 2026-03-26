package com.android.mr.todopro.data.remote

import android.util.Log
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object NetworkClient {

    private val json = Json {
        ignoreUnknownKeys = true    // don't crash if server sends unknown keys
        coerceInputValues = true    // handle null -> default values
    }

    val apiService: TodoApiService by lazy {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TodoApiService::class.java)
    }
}

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()

    data class ApiError(val code: Int, val message: String) : NetworkResult<Nothing>()

    data object NetworkError : NetworkResult<Nothing>()

    data object Loading: NetworkResult<Nothing>()
}

// Extension function - call this around every Retrofit call
suspend fun<T> safeApiCall(call: suspend () -> T): NetworkResult<T> {
    return try {
        val result = call()
        NetworkResult.Success(result)
    } catch (e: HttpException) {
        Log.e("NetworkClient", "HttpException: ${e.code()} ${e.message()}")
        NetworkResult.ApiError(e.code(), e.message())
    } catch (e: IOException) {
        Log.e("NetworkClient", "IOException (Network Error): ${e.message}")
        NetworkResult.NetworkError
    } catch (e: Exception) {
        Log.e("NetworkClient", "Unknown Exception: ${e.message}")
        NetworkResult.ApiError(-1, e.message ?: "Unknown Error")
    }
}

package com.example.o2scratch.feature.data.networking

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ScratchClient {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d(ScratchClient::class.toString(), message)
                }
            }
            level = LogLevel.ALL
        }
    }

    suspend fun getVersion(code: String): Result<Int> {
        return try {
            return Result.success(1230000)

            val response = client.get(URL) {
                parameter("code", code)
            }.body<VersionResponse>()

            Log.d(ScratchClient::class.toString(), "Received version: ${response.android}")

            Result.success(response.android)
        } catch (e: Exception) {
            Log.e(ScratchClient::class.toString(), "Error validating code", e)
            Result.failure(e)
        }
    }

    companion object Companion {
        private const val URL = "https://api.o2.sk/version"
    }
}

@Serializable
data class VersionResponse(
    val android: Int
)


package com.cherenkov.videoapp.core.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine){
            install(ContentNegotiation){
                json(
                    json = Json{
                        ignoreUnknownKeys = true
                    }
                )
            }
            install(HttpTimeout){
                socketTimeoutMillis = 20_000L
                requestTimeoutMillis = 20_000L
            }
            install(Logging){
                logger = object : Logger {
                    override fun log(message: String) {
                        println(message)
                    }
                }
                level = LogLevel.ALL
            }
            defaultRequest {
                //Выкладываю токен исключительно для демонстрации и возможности запустить проект без проблем
                header("Authorization", "asrxKZQUTtwSkSqk1hLWOzQCDnIgYrGmUuVALFLSt7tr0IfXIm7qeAxF")
                contentType(ContentType.Application.Json)
            }
        }
    }

}
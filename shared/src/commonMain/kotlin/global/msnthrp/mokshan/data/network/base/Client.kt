package global.msnthrp.mokshan.data.network.base

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.serialization.ContentConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal val client = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
}

internal suspend inline fun <reified T> HttpResponse.getFromJson(): T {
    val body: String = body()
    return Json.decodeFromString(body)
}
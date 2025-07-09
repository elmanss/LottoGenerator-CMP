package me.elmanss.melate

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.elmanss.melate.common.domain.SorteoApi
import me.elmanss.melate.common.domain.model.RequestState

class SorteoApiImpl : SorteoApi {

  companion object {
    private const val TAG = "SorteoApi"
  }

  private val httpClient = HttpClient {
    install(ContentNegotiation) {
      json(
          Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
          })
    }
    install(HttpTimeout) { requestTimeoutMillis = 15000 }
    //        install(DefaultRequest) {
    //            headers {
    //                append("apiKey", API_KEY)
    //            }
    //        }
  }

  override suspend fun fetchSorteos(
      version: String,
      min: String,
      max: String,
      count: String
  ): RequestState<List<Int>> {
    return try {
      val url = "${SorteoApi.URL}$version/random?min=$min&max=$max&count=$count"
      Logger.d(TAG) { "Url: $url" }

      val response = httpClient.get(urlString = url)
      if (response.status.value == 200) {
        Logger.d(TAG) { "Success" }
        val apiResponse = Json.decodeFromString<List<Int>>(response.body())

        RequestState.Success(data = apiResponse)
      } else {
        Logger.e(TAG) { "Http Error code: ${response.status}" }
        RequestState.Error(message = "HTTP Error Code: ${response.status}")
      }
    } catch (e: Exception) {
      Logger.e(TAG, throwable = e) { "Error" }
      RequestState.Error(message = e.message.toString())
    }
  }
}

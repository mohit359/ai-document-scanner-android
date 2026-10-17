package com.mohitkumar.docscanner.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class SummaryRequest(val text: String)

@Serializable
data class SummaryResponse(val summary: String)

/** Talks to our own proxy (see /server), never directly to the LLM, so no API key ships in the APK. */
interface SummaryApi {
    @POST("summarize")
    suspend fun summarize(@Body request: SummaryRequest): SummaryResponse
}

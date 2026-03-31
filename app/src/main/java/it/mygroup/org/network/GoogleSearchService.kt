package it.mygroup.org.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Interface for Google Custom Search API with optional Android headers
 */
interface GoogleSearchService {
    @GET("customsearch/v1")
    suspend fun search(
        @Query("key") apiKey: String,
        @Query("cx") searchEngineId: String,
        @Query("q") query: String,
        @Query("hl") language: String = "it",
        @Query("gl") country: String = "it",
        @Header("X-Android-Package") packageName: String? = null,
        @Header("X-Android-Cert") fingerprint: String? = null
    ): String
}

object GoogleSearchApi {
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl("https://www.googleapis.com/")
        .build()

    val retrofitService: GoogleSearchService by lazy {
        retrofit.create(GoogleSearchService::class.java)
    }
}

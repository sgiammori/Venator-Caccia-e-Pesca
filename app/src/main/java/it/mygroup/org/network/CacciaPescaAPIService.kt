package it.mygroup.org.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

/**
 * Retrofit service object for creating api calls
 */
interface CacciaPescaAPIService {
    @Headers(
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Accept: application/rss+xml, application/xml, text/xml, */*",
        "Accept-Language: en-US,en;q=0.9",
        "Cache-Control: no-cache"
    )
    @GET
    suspend fun getRssFeed(@Url url: String): String

    @Headers("Content-Type: application/json")
    @POST("api/send-event")
    suspend fun sendEvent(@Body jsonBody: String): String

    @GET("api/get-invites")
    suspend fun getInvites(@Query("userId") userId: String): String

    @Headers("Content-Type: application/json")
    @POST("api/get-invites")
    suspend fun deleteEvent(@Body jsonBody: String): String

    @Headers("Content-Type: application/json")
    @POST("api/authenticate-app")
    suspend fun authenticateApp(@Body jsonBody: String): String

    @GET("api/get-all-users")
    suspend fun getAllUsers(): String

    @GET("api/respond-to-invite")
    suspend fun respondToInvite(
        @Query("eventId") eventId: String,
        @Query("userId") userId: String,
        @Query("action") action: String
    ): String

    @Headers("Content-Type: application/json")
    @POST("api/create-event")
    suspend fun createEvent(@Body jsonBody: String): String
}

/**
 * Use the Retrofit builder to build a retrofit object using a scalars converter
 */
object CacciaPescaApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS) // Ridotto timeout per fail fast
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        // Per test in localhost dall'emulatore Android, usa l'IP 10.0.2.2
        // Assicurati che il server sia in ascolto su tutte le interfacce (0.0.0.0) e non solo localhost
        .baseUrl("http://cacciaepesca.azurewebsites.net/")
        .client(client)
        .build()

    val retrofitService: CacciaPescaAPIService by lazy {
        retrofit.create(CacciaPescaAPIService::class.java)
    }
}

package com.dianca.synced

import com.dianca.synced.Score
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ScoreApi {

    @POST("api/scores")
    suspend fun postScore(@Body score: Score): Response<Map<String, String>>

    @GET("api/scores/user/{userId}")
    suspend fun getUserScores(@Path("userId") userId: String): Response<List<Score>>

    @GET("api/scores/friend/{friendId}")
    suspend fun getFriendScores(@Path("friendId") friendId: String): Response<List<Score>>
}

object RetrofitClient {

    private const val BASE_URL = "https://syncedapi.onrender.com/" // Replace with Render URL

    private val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    private val client = OkHttpClient.Builder().addInterceptor(logging).build()

    val instance: ScoreApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ScoreApi::class.java)
    }
}

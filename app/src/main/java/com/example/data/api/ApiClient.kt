package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

  private const val BASE_URL = "https://footballtoday.pro/Scoreadmi/api/v1/"

  private val authInterceptor = Interceptor { chain ->
    val originalRequest = chain.request()
    
    // Read the API key from BuildConfig (populated by Secrets Gradle Plugin via .env)
    val apiKey = try {
      val key = BuildConfig.FOOTBALL_API_KEY
      if (key.isNullOrBlank() || key == "YOUR_FOOTBALL_API_KEY") {
        "football-today-client-key"
      } else {
        key
      }
    } catch (e: Throwable) {
      "football-today-client-key"
    }

    val newRequest = originalRequest.newBuilder()
      .header("X-API-Key", apiKey)
      .header("Accept", "application/json")
      .header("User-Agent", "FootballToday-Android/1.0")
      .build()

    chain.proceed(newRequest)
  }

  private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BASIC
  }

  private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)
    .addInterceptor(loggingInterceptor)
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .writeTimeout(15, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)
    .build()

  private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

  val apiService: FootballApiService by lazy {
    Retrofit.Builder()
      .baseUrl(BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(FootballApiService::class.java)
  }
}

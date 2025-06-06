package com.example.filkomplain

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    //private const val BASE_URL = "https://38bc-175-45-191-11.ngrok-free.app/" // Ganti sesuai IP generated ngrok
    private const val BASE_URL = "http://10.0.2.2/" // Kalau pakai lokal saja
    private const val UPLOADS_PATH = "FILKOMPLAIN/"

    fun getUploadsUrl(): String {
        return BASE_URL + UPLOADS_PATH
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
package com.example.thefesta.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FoodClient {
    private const val BASE_URL = "http://192.168.0.19:9090/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
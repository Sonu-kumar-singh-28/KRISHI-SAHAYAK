package com.ssu.xyvento.sihapp.network

import com.ssu.xyvento.sihapp.api.MarketApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 🔹 GOVERNMENT API (Market / Mandi)
    private const val GOVT_BASE_URL = "https://api.data.gov.in/"

    // 🔹 SOIL & FERTILIZER (Mock / Custom API)
    private const val SOIL_BASE_URL = "https://mock-api-for-farmers.vercel.app/"

    // ✅ Market Price API
    val marketApi: MarketApi by lazy {
        Retrofit.Builder()
            .baseUrl(GOVT_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MarketApi::class.java)
    }
}

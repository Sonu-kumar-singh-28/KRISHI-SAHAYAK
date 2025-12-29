package com.ssu.xyvento.sihapp.api

import com.ssu.xyvento.sihapp.dataclass.MarketPriceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MarketApi {
    @GET("resource/9ef84268-d588-465a-a308-a864a43d0070")
    fun getMarketPrices(
        @Query("api-key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("filters[commodity]") commodity: String? = null,
        @Query("filters[state]") state: String? = null,
        @Query("limit") limit: Int = 100
    ): Call<MarketPriceResponse>
}

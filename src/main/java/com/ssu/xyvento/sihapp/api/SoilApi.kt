package com.ssu.xyvento.sihapp.api

import com.ssu.xyvento.sihapp.dataclass.SoilResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SoilApi {

    @GET("v1/9c4f0d7a-1f77-4a1e-9b7d-6e1a9b8f5c11")
    fun getSoilFertilizerAdvice(
        @Query("crop") crop: String,
        @Query("soil") soil: String
    ): Call<SoilResponse>
}

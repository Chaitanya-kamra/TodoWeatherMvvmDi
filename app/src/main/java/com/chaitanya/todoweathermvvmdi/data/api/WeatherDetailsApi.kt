package com.chaitanya.todoweathermvvmdi.data.api

import com.chaitanya.todoweathermvvmdi.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherDetailsApi {

    //function to request weather details from the API.
    @GET("v1/current.json")
    suspend fun getWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String
    ): Response<WeatherResponse>

}
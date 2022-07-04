package com.example.breweries.retrofit

import com.example.breweries.data.BreweryObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("/breweries")
    suspend fun getBreweries(): Response<List<BreweryObject>>

    @GET("/breweries")
    suspend fun getBreweriesByCity(@Query("by_city") city: String): Response<List<BreweryObject>>

}
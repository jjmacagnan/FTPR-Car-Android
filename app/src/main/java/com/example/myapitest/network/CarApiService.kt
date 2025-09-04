package com.example.myapitest.network

import com.example.myapitest.models.Car
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CarApiService {
    @GET("car")
    suspend fun getCars(): Response<List<Car>>

    @POST("car")
    suspend fun createCar(@Body car: Car): Response<Car>

    @PUT("car/{id}")
    suspend fun updateCar(@Path("id") id: String, @Body car: Car): Response<Car>

    @DELETE("car/{id}")
    suspend fun deleteCar(@Path("id") id: String): Response<Unit>
}
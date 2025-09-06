package com.example.myapitest.network

import com.example.myapitest.models.Car
import retrofit2.Response
import retrofit2.http.*

interface CarApiService {
    @GET("car")
    suspend fun getCars(): Response<List<Car>>

    @GET("car/{id}")
    suspend fun getCar(@Path("id") id: String): Response<Car>

    @POST("car")
    suspend fun createCar(@Body car: Car): Response<Car>

    @POST("car")
    suspend fun createCars(@Body cars: List<Car>): Response<List<Car>>

    @PATCH("car/{id}")
    suspend fun updateCar(@Path("id") id: String, @Body car: Car): Response<Car>

    @DELETE("car/{id}")
    suspend fun deleteCar(@Path("id") id: String): Response<Void>
}
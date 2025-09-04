package com.example.myapitest.network

import com.example.myapitest.models.Car

class CarRepository {
    private val apiService = ApiClient.carService

    suspend fun getCars(): Result<List<Car>> {
        return try {
            val response = apiService.getCars()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Erro na API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCar(car: Car): Result<Car> {
        return try {
            val response = apiService.createCar(car)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro na API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCar(carId: String, car: Car): Result<Car> {
        return try {
            val response = apiService.updateCar(carId, car)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro na API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCar(carId: String): Result<Unit> {
        return try {
            val response = apiService.deleteCar(carId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro na API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
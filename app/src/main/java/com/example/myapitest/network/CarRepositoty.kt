package com.example.myapitest.network

import com.example.myapitest.models.Car

class CarRepository {
    private val apiService = ApiClient.carService

    suspend fun getCars(): ResultWrapper<List<Car>> {
        return safeApiCall {
            val response = apiService.getCars()
            response.body() ?: emptyList()
        }
    }

    suspend fun getCar(carId: String): ResultWrapper<Car> {
        return safeApiCall {
            val response = apiService.getCar(carId)
            response.body() ?: throw Exception("Car not found")
        }
    }

    suspend fun createCar(car: Car): ResultWrapper<Car> {
        return try {
            val response = ApiClient.carService.createCar(car)
            if (response.isSuccessful) {
                ResultWrapper.Success(car)
            } else {
                ResultWrapper.GenericError(response.code(), "Erro ao criar carro: ${response.code()}")
            }
        } catch (e: Exception) {
            ResultWrapper.NetworkError
        }
    }

    suspend fun createCars(cars: List<Car>): ResultWrapper<List<Car>> {
        return safeApiCall {
            val response = apiService.createCars(cars)
            response.body() ?: emptyList()
        }
    }

    suspend fun updateCar(carId: String, car: Car): ResultWrapper<Car> {
        return safeApiCall {
            // Usando o objeto Car diretamente ao invés de mapOf
            val response = apiService.updateCar(carId, car)
            response.body() ?: throw Exception("Failed to update car")
        }
    }

    suspend fun deleteCar(carId: String): ResultWrapper<Unit> {
        return safeApiCall {
            apiService.deleteCar(carId)
            Unit
        }
    }

    // Função auxiliar para reduzir repetição de código
    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): ResultWrapper<T> {
        return try {
            val result = apiCall()
            ResultWrapper.Success(result)
        } catch (e: retrofit2.HttpException) {
            ResultWrapper.GenericError(e.code(), e.message())
        } catch (e: java.io.IOException) {
            ResultWrapper.NetworkError
        } catch (e: Exception) {
            ResultWrapper.GenericError(null, e.message ?: "Unknown error")
        }
    }
}
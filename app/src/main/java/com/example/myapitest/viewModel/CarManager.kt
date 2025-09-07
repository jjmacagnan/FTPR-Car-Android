package com.example.myapitest.managers

import com.example.myapitest.models.Car
import com.example.myapitest.network.CarRepository
import com.example.myapitest.network.ResultWrapper
import com.example.myapitest.utils.UiState
import com.example.myapitest.utils.ImageUploadHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object CarManager {

    private val repository = CarRepository()

    private val _carsState = MutableStateFlow<UiState<List<Car>>>(UiState.Idle)
    val carsState: StateFlow<UiState<List<Car>>> get() = _carsState

    private val _operationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val operationState: StateFlow<UiState<String>> get() = _operationState

    private val carsList = mutableListOf<Car>()

    suspend fun fetchCars() {
        _carsState.value = UiState.Loading
        when (val result = repository.getCars()) {
            is ResultWrapper.Success -> {
                carsList.clear()
                carsList.addAll(result.value)
                _carsState.value = UiState.Success(result.value)
            }
            is ResultWrapper.GenericError -> _carsState.value =
                UiState.Error(result.error ?: "Erro genérico")
            is ResultWrapper.NetworkError -> _carsState.value =
                UiState.Error("Erro de rede")
        }
    }

    suspend fun addCar(car: Car) {
        _operationState.value = UiState.Loading
        when (val result = repository.createCar(car)) {
            is ResultWrapper.Success -> {
                carsList.add(car)
                _carsState.value = UiState.Success(carsList.toList())
                _operationState.value = UiState.Success("Carro adicionado com sucesso!")
            }
            is ResultWrapper.GenericError -> _operationState.value =
                UiState.Error(result.error ?: "Erro ao adicionar carro")
            is ResultWrapper.NetworkError -> _operationState.value =
                UiState.Error("Erro de rede ao adicionar carro")
        }
    }

    fun updateCar(updatedCar: Car) {
        val index = carsList.indexOfFirst { it.id == updatedCar.id }
        if (index != -1) {
            carsList[index] = updatedCar
            _carsState.value = UiState.Success(carsList.toList())
        }
    }

    suspend fun deleteCar(carId: String) {
        _operationState.value = UiState.Loading
        when (val result = repository.deleteCar(carId)) {
            is ResultWrapper.Success -> {
                val carToRemove = carsList.find { it.id == carId }

                try {
                    carToRemove?.imageUrl?.let { url ->
                        val success = suspendCancellableCoroutine<Boolean> { cont ->
                            ImageUploadHelper.deleteImage(url) { ok ->
                                cont.resume(ok)
                            }
                        }

                        if (!success) {
                            _operationState.value =
                                UiState.Error("Carro removido, mas erro ao excluir imagem")
                            return
                        }
                    }

                    carsList.removeAll { it.id == carId }
                    _carsState.value = UiState.Success(carsList.toList())
                    _operationState.value = UiState.Success("Carro e imagem deletados")
                } catch (e: Exception) {
                    _operationState.value = UiState.Error("Erro ao excluir imagem: ${e.message}")
                }
            }
            is ResultWrapper.GenericError -> _operationState.value =
                UiState.Error(result.error ?: "Erro genérico")
            is ResultWrapper.NetworkError -> _operationState.value =
                UiState.Error("Erro de rede")
        }
    }

    fun clearOperationState() {
        _operationState.value = UiState.Idle
    }
}
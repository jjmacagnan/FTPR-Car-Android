package com.example.myapitest.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapitest.models.Car
import com.example.myapitest.network.CarRepository
import com.example.myapitest.network.ResultWrapper
import com.example.myapitest.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CarViewModel(private val repository: CarRepository) : ViewModel() {

    private val _carsState = MutableStateFlow<UiState<List<Car>>>(UiState.Idle)
    val carsState: StateFlow<UiState<List<Car>>> get() = _carsState

    private val _operationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val operationState: StateFlow<UiState<String>> get() = _operationState

    // Lista interna que mantém referência para atualizações
    private val carsList = mutableListOf<Car>()

    fun fetchCars() {
        _carsState.value = UiState.Loading
        viewModelScope.launch {
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
    }

    fun updateCar(updatedCar: Car) {
        val index = carsList.indexOfFirst { it.id == updatedCar.id }
        if (index != -1) {
            carsList[index] = updatedCar
            _carsState.value = UiState.Success(carsList.toList()) // Atualiza lista
        }
    }

    fun deleteCar(carId: String) {
        _operationState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = repository.deleteCar(carId)) {
                is ResultWrapper.Success -> {
                    carsList.removeAll { it.id == carId }
                    _carsState.value = UiState.Success(carsList.toList())
                    _operationState.value = UiState.Success("Carro deletado")
                }
                is ResultWrapper.GenericError -> _operationState.value =
                    UiState.Error(result.error ?: "Erro genérico")
                is ResultWrapper.NetworkError -> _operationState.value =
                    UiState.Error("Erro de rede")
            }
        }
    }

    // Função que reseta o estado de operações
    fun clearOperationState() {
        _operationState.value = UiState.Idle
    }
}

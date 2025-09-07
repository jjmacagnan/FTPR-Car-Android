package com.example.myapitest.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapitest.managers.CarManager
import com.example.myapitest.models.Car
import kotlinx.coroutines.launch

class CarViewModel : ViewModel() {

    // Expõe os StateFlows do CarManager
    val carsState = CarManager.carsState
    val operationState = CarManager.operationState

    fun fetchCars() {
        viewModelScope.launch {
            CarManager.fetchCars()
        }
    }

    fun addCar(car: Car) {
        viewModelScope.launch {
            CarManager.addCar(car)
        }
    }

    fun updateCar(updatedCar: Car) {
        CarManager.updateCar(updatedCar)
    }

    fun deleteCar(carId: String) {
        viewModelScope.launch {
            CarManager.deleteCar(carId)
        }
    }

    fun clearOperationState() {
        CarManager.clearOperationState()
    }
}
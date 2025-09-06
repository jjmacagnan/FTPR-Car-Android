package com.example.myapitest.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapitest.R
import com.example.myapitest.activities.base.BaseCarActivity
import com.example.myapitest.databinding.ActivityEditCarBinding
import com.example.myapitest.models.Car
import com.example.myapitest.network.ApiClient
import kotlinx.coroutines.launch

class EditCarActivity : BaseCarActivity() {

    private lateinit var binding: ActivityEditCarBinding
    private lateinit var car: Car
    private var currentImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityEditCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        car = intent.getParcelableExtra("car")!!
        currentImageUrl = car.imageUrl
        selectedLatitude = car.place.lat
        selectedLongitude = car.place.long

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Editar ${car.name}"

        super.onCreate(savedInstanceState)
        populateFields()
    }

    override fun setupSpecificUI() {
        binding.btnSelectImage.setOnClickListener { showImagePickerDialog() }
        binding.btnSelectLocation.setOnClickListener { openLocationPicker() }
        binding.btnSaveChanges.setOnClickListener { handleSaveOperation() }
        binding.btnCancel.setOnClickListener { finish() }

        setupLicenceMask(binding.etLicence)
    }

    private fun populateFields() {
        binding.apply {
            etName.setText(car.name)
            etYear.setText(car.year)
            etLicence.setText(car.licence)
            etLatitude.setText(car.place.lat.toString())
            etLongitude.setText(car.place.long.toString())

            Glide.with(this@EditCarActivity)
                .load(car.imageUrl)
                .placeholder(R.drawable.ic_car_placeholder)
                .error(R.drawable.ic_car_placeholder)
                .into(ivCarImage)
        }
    }

    override fun getNameField(): String = binding.etName.text.toString().trim()
    override fun getYearField(): String = binding.etYear.text.toString().trim()
    override fun getLicenceField(): String = binding.etLicence.text.toString().trim()

    override fun setImageUri(uri: Uri) {
        binding.ivCarImage.setImageURI(uri)
    }

    override fun updateSaveButton(enabled: Boolean, text: String) {
        binding.btnSaveChanges.isEnabled = enabled
        binding.btnSaveChanges.text = text
    }

    override fun resetSaveButton() {
        updateSaveButton(true, "Salvar Alterações")
    }

    override fun setLocationFields(latitude: Double, longitude: Double) {
        binding.etLatitude.setText(latitude.toString())
        binding.etLongitude.setText(longitude.toString())
    }

    override fun showFieldError(field: String, message: String) {
        when (field) {
            "name" -> binding.etName.error = message
            "year" -> binding.etYear.error = message
            "licence" -> binding.etLicence.error = message
        }
    }

    override fun getCurrentImageUrl(): String {
        return currentImageUrl ?: car.imageUrl
    }

    override fun performSaveOperation(imageUrl: String) {
        val updatedCar = car.copy(
            name = getNameField(),
            year = getYearField(),
            licence = getLicenceField(),
            imageUrl = imageUrl,
            place = createPlace(
                selectedLatitude ?: car.place.lat,
                selectedLongitude ?: car.place.long
            )
        )

        updateCarViaApi(updatedCar)
    }

    private fun updateCarViaApi(updatedCar: Car) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.carService.updateCar(updatedCar.id, updatedCar)
                if (response.isSuccessful) {
                    showToast("Carro atualizado com sucesso!")
                    viewModel.updateCar(updatedCar)

                    val resultIntent = Intent().apply {
                        putExtra("updated_car", updatedCar)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    showToast("Erro ao atualizar carro: ${response.code()}")
                    resetSaveButton()
                }
            } catch (e: Exception) {
                showToast("Erro de conexão: ${e.message}")
                resetSaveButton()
            }
        }
    }
}
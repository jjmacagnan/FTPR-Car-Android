package com.example.myapitest.activities

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import com.example.myapitest.R
import com.example.myapitest.activities.base.BaseCarActivity
import com.example.myapitest.databinding.ActivityAddCarBinding
import com.example.myapitest.models.Car
import java.util.UUID

class AddCarActivity : BaseCarActivity() {

    private lateinit var binding: ActivityAddCarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Adicionar Carro"

        super.onCreate(savedInstanceState)
    }

    override fun setupSpecificUI() {
        binding.btnSelectImage.setOnClickListener { showImagePickerDialog() }
        binding.btnSelectLocation.setOnClickListener { openLocationPicker() }
        binding.btnSaveCar.setOnClickListener { handleSaveOperation() }
        binding.ivCarImage.setImageResource(R.drawable.ic_car_placeholder)

        setupLicenceMask(binding.etLicence)
    }

    override fun getNameField(): String = binding.etName.text.toString().trim()
    override fun getYearField(): String = binding.etYear.text.toString().trim()
    override fun getLicenceField(): String = binding.etLicence.text.toString().trim()

    override fun setImageUri(uri: Uri) {
        binding.ivCarImage.setImageURI(uri)
    }

    override fun updateSaveButton(enabled: Boolean, text: String) {
        binding.btnSaveCar.isEnabled = enabled
        binding.btnSaveCar.text = text
    }

    override fun resetSaveButton() {
        updateSaveButton(true, "Salvar Carro")
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
        // For AddCarActivity, there's no current image URL, so we return empty
        // The validation will catch this and require image selection
        return ""
    }

    override fun performSaveOperation(imageUrl: String) {
        if (imageUrl.isEmpty()) {
            showToast("Selecione uma imagem")
            resetSaveButton()
            return
        }

        val car = Car(
            id = UUID.randomUUID().toString(),
            name = getNameField(),
            year = getYearField(),
            licence = getLicenceField(),
            imageUrl = imageUrl,
            place = createPlace()
        )

        viewModel.addCar(car)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}
package com.example.myapitest.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapitest.R
import com.example.myapitest.databinding.ActivityEditCarBinding
import com.example.myapitest.models.Car
import com.example.myapitest.models.Place
import com.example.myapitest.network.ApiClient
import com.example.myapitest.network.CarRepository
import com.example.myapitest.utils.ImageUploadHelper
import com.example.myapitest.viewModel.CarViewModel
import kotlinx.coroutines.launch

class EditCarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCarBinding
    private lateinit var car: Car
    private var selectedImageUri: Uri? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var currentImageUrl: String? = null

    // ViewModel compartilhado
    private val viewModel: CarViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(CarViewModel::class.java)) {
                    return CarViewModel(CarRepository()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivCarImage.setImageURI(it)
        }
    }

    private val locationPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                selectedLatitude = data.getDoubleExtra("latitude", 0.0)
                selectedLongitude = data.getDoubleExtra("longitude", 0.0)
                binding.etLatitude.setText(selectedLatitude.toString())
                binding.etLongitude.setText(selectedLongitude.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        car = intent.getParcelableExtra("car")!!
        setupUI()
        populateFields()
    }

    private fun setupUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Editar ${car.name}"

        binding.btnSelectImage.setOnClickListener { imagePickerLauncher.launch("image/*") }
        binding.btnSelectLocation.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            locationPickerLauncher.launch(intent)
        }
        binding.btnSaveChanges.setOnClickListener { saveChanges() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun populateFields() {
        binding.apply {
            etName.setText(car.name)
            etYear.setText(car.year)
            etLicence.setText(car.licence)
            etLatitude.setText(car.place.lat.toString())
            etLongitude.setText(car.place.long.toString())

            selectedLatitude = car.place.lat
            selectedLongitude = car.place.long
            currentImageUrl = car.imageUrl

            Glide.with(this@EditCarActivity)
                .load(car.imageUrl)
                .placeholder(R.drawable.placeholder_car)
                .error(R.drawable.placeholder_car)
                .into(ivCarImage)
        }
    }

    private fun saveChanges() {
        if (!validateFields()) return

        binding.btnSaveChanges.isEnabled = false
        binding.btnSaveChanges.text = "Salvando..."

        if (selectedImageUri != null) {
            uploadImageAndSaveCar(selectedImageUri!!)
        } else {
            updateCarWithCurrentImage()
        }
    }

    private fun uploadImageAndSaveCar(imageUri: Uri) {
        ImageUploadHelper.uploadImage(imageUri) { downloadUrl, error ->
            if (downloadUrl != null) {
                updateCarObject(downloadUrl)
            } else {
                showToast("Erro ao fazer upload da imagem: ${error?.message}")
                resetSaveButton()
            }
        }
    }

    private fun updateCarWithCurrentImage() {
        updateCarObject(currentImageUrl ?: car.imageUrl)
    }

    private fun updateCarObject(imageUrl: String) {
        val updatedCar = car.copy(
            name = binding.etName.text.toString().trim(),
            year = binding.etYear.text.toString().trim(),
            licence = binding.etLicence.text.toString().trim(),
            imageUrl = imageUrl,
            place = Place(
                lat = selectedLatitude ?: car.place.lat,
                long = selectedLongitude ?: car.place.long
            )
        )

        updateCarInAPI(updatedCar)
    }

    private fun updateCarInAPI(updatedCar: Car) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.carService.updateCar(updatedCar.id, updatedCar)
                if (response.isSuccessful) {
                    showToast("Carro atualizado com sucesso!")

                    // Atualiza lista do MainActivity
                    viewModel.updateCar(updatedCar)

                    // Envia de volta para CarDetailActivity
                    val resultIntent = Intent()
                    resultIntent.putExtra("updated_car", updatedCar)
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

    private fun validateFields(): Boolean {
        val name = binding.etName.text.toString().trim()
        val year = binding.etYear.text.toString().trim()
        val licence = binding.etLicence.text.toString().trim()

        return when {
            name.isEmpty() -> {
                binding.etName.error = "Campo obrigatório"
                false
            }
            year.isEmpty() -> {
                binding.etYear.error = "Campo obrigatório"
                false
            }
            licence.isEmpty() -> {
                binding.etLicence.error = "Campo obrigatório"
                false
            }
            selectedLatitude == null || selectedLongitude == null -> {
                showToast("Localização é obrigatória")
                false
            }
            else -> true
        }
    }

    private fun resetSaveButton() {
        binding.btnSaveChanges.isEnabled = true
        binding.btnSaveChanges.text = "Salvar Alterações"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
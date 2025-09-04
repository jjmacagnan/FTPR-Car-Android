package com.example.myapitest.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapitest.R
import com.example.myapitest.databinding.ActivityEditCarBinding
import com.example.myapitest.models.Car
import com.example.myapitest.models.Location
import com.example.myapitest.network.ApiClient
import com.example.myapitest.utils.ImageUploadHelper
import kotlinx.coroutines.launch

class EditCarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditCarBinding
    private lateinit var car: Car
    private var selectedImageUri: Uri? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var currentImageUrl: String? = null

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

        car = intent.getSerializableExtra("car") as Car

        setupUI()
        populateFields()
    }

    private fun setupUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Editar ${car.brand} ${car.model}"

        binding.btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnSelectLocation.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            locationPickerLauncher.launch(intent)
        }

        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun populateFields() {
        binding.apply {
            etBrand.setText(car.brand)
            etModel.setText(car.model)
            etYear.setText(car.year.toString())
            etColor.setText(car.color)
            etPrice.setText(car.price.toString())
            etLatitude.setText(car.location.latitude.toString())
            etLongitude.setText(car.location.longitude.toString())

            // Definir valores atuais
            selectedLatitude = car.location.latitude
            selectedLongitude = car.location.longitude
            currentImageUrl = car.imageUrl

            // Carregar imagem atual
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
            // Upload nova imagem
            uploadImageAndSaveCar(selectedImageUri!!)
        } else {
            // Usar imagem atual
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
            brand = binding.etBrand.text.toString().trim(),
            model = binding.etModel.text.toString().trim(),
            year = binding.etYear.text.toString().trim().toInt(),
            color = binding.etColor.text.toString().trim(),
            price = binding.etPrice.text.toString().trim().toDouble(),
            imageUrl = imageUrl,
            location = Location(
                latitude = selectedLatitude ?: car.location.latitude,
                longitude = selectedLongitude ?: car.location.longitude,
                address = car.location.address
            )
        )

        updateCarInAPI(updatedCar)
    }

    private fun updateCarInAPI(updatedCar: Car) {
        lifecycleScope.launch {
            try {
                car.id?.let { carId ->
                    val response = ApiClient.carService.updateCar(carId, updatedCar)
                    if (response.isSuccessful) {
                        showToast("Carro atualizado com sucesso!")
                        finish()
                    } else {
                        showToast("Erro ao atualizar carro")
                        resetSaveButton()
                    }
                }
            } catch (e: Exception) {
                showToast("Erro de conexão: ${e.message}")
                resetSaveButton()
            }
        }
    }

    private fun validateFields(): Boolean {
        val brand = binding.etBrand.text.toString().trim()
        val model = binding.etModel.text.toString().trim()
        val year = binding.etYear.text.toString().trim()
        val color = binding.etColor.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()

        when {
            brand.isEmpty() -> {
                binding.etBrand.error = "Campo obrigatório"
                return false
            }
            model.isEmpty() -> {
                binding.etModel.error = "Campo obrigatório"
                return false
            }
            year.isEmpty() -> {
                binding.etYear.error = "Campo obrigatório"
                return false
            }
            color.isEmpty() -> {
                binding.etColor.error = "Campo obrigatório"
                return false
            }
            price.isEmpty() -> {
                binding.etPrice.error = "Campo obrigatório"
                return false
            }
            selectedLatitude == null || selectedLongitude == null -> {
                showToast("Localização é obrigatória")
                return false
            }
        }

        return true
    }

    private fun resetSaveButton() {
        binding.btnSaveChanges.isEnabled = true
        binding.btnSaveChanges.text = "Salvar Alterações"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
package com.example.myapitest.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapitest.R
import com.example.myapitest.databinding.ActivityAddCarBinding
import com.example.myapitest.models.Car
import com.example.myapitest.models.Location
import com.example.myapitest.network.ApiClient
import com.example.myapitest.utils.ImageUploadHelper
import kotlinx.coroutines.launch

class AddCarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCarBinding
    private var selectedImageUri: Uri? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

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
        binding = ActivityAddCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnSelectLocation.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            locationPickerLauncher.launch(intent)
        }

        binding.btnSaveCar.setOnClickListener {
            saveCar()
        }

        // Configurar toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Adicionar Carro"

        // DEFINIR PLACEHOLDER INICIAL
        binding.ivCarImage.setImageResource(R.drawable.placeholder_car)
    }

    private fun saveCar() {
        if (!validateFields()) return

        binding.btnSaveCar.isEnabled = false
        binding.btnSaveCar.text = "Salvando..."

        selectedImageUri?.let { imageUri ->
            uploadImageAndSaveCar(imageUri)
        } ?: run {
            showToast("Selecione uma imagem")
            resetSaveButton()
        }
    }

    private fun uploadImageAndSaveCar(imageUri: Uri) {
        ImageUploadHelper.uploadImage(imageUri) { downloadUrl, error ->
            if (downloadUrl != null) {
                createCarObject(downloadUrl)
            } else {
                showToast("Erro ao fazer upload da imagem: ${error?.message}")
                resetSaveButton()
            }
        }
    }

    private fun createCarObject(imageUrl: String) {
        val car = Car(
            brand = binding.etBrand.text.toString().trim(),
            model = binding.etModel.text.toString().trim(),
            year = binding.etYear.text.toString().trim().toInt(),
            color = binding.etColor.text.toString().trim(),
            price = binding.etPrice.text.toString().trim().toDouble(),
            imageUrl = imageUrl,
            location = Location(
                latitude = selectedLatitude ?: 0.0,
                longitude = selectedLongitude ?: 0.0,
                address = "Endereço não especificado"
            )
        )

        saveCarToAPI(car)
    }

    private fun saveCarToAPI(car: Car) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.carService.createCar(car)
                if (response.isSuccessful) {
                    showToast("Carro salvo com sucesso!")
                    finish()
                } else {
                    showToast("Erro ao salvar carro")
                    resetSaveButton()
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
                showToast("Selecione uma localização")
                return false
            }
        }

        return true
    }

    private fun resetSaveButton() {
        binding.btnSaveCar.isEnabled = true
        binding.btnSaveCar.text = "Salvar Carro"
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
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
import com.example.myapitest.R
import com.example.myapitest.databinding.ActivityAddCarBinding
import com.example.myapitest.models.Car
import com.example.myapitest.models.Place
import com.example.myapitest.utils.ImageUploadHelper
import com.example.myapitest.utils.UiState
import com.example.myapitest.viewModel.CarViewModel
import kotlinx.coroutines.launch
import java.util.UUID

class AddCarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCarBinding
    private var selectedImageUri: Uri? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    // Usa o ViewModel que conecta ao CarManager singleton
    private val viewModel: CarViewModel by viewModels()

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

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Adicionar Carro"

        setupUI()
        observeOperationState()
    }

    private fun setupUI() {
        binding.btnSelectImage.setOnClickListener { imagePickerLauncher.launch("image/*") }

        binding.btnSelectLocation.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            locationPickerLauncher.launch(intent)
        }

        binding.btnSaveCar.setOnClickListener { saveCar() }

        // Placeholder inicial
        binding.ivCarImage.setImageResource(R.drawable.ic_car_placeholder)
    }

    private fun observeOperationState() {
        lifecycleScope.launch {
            viewModel.operationState.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                        binding.btnSaveCar.isEnabled = true
                        binding.btnSaveCar.text = "Salvar Carro"
                    }
                    is UiState.Loading -> {
                        binding.btnSaveCar.isEnabled = false
                        binding.btnSaveCar.text = "Salvando..."
                    }
                    is UiState.Success -> {
                        showToast(state.data)
                        // IMPORTANTE: MainActivity será automaticamente atualizada
                        // porque observa o mesmo CarManager singleton
                        finish()
                    }
                    is UiState.Error -> {
                        showToast("Erro: ${state.message}")
                        resetSaveButton()
                    }
                }
            }
        }
    }

    private fun saveCar() {
        if (!validateFields()) return

        binding.btnSaveCar.isEnabled = false
        binding.btnSaveCar.text = "Salvando..."

        selectedImageUri?.let { uploadImageAndSaveCar(it) }
            ?: run {
                showToast("Selecione uma imagem")
                resetSaveButton()
            }
    }

    private fun uploadImageAndSaveCar(imageUri: Uri) {
        ImageUploadHelper.uploadImage(selectedImageUri!!) { downloadUrl, error ->
            if (downloadUrl != null) {
                createAndSaveCarObject(downloadUrl)
            } else {
                showToast("Erro no upload: ${error?.message}")
                resetSaveButton()
            }
        }
    }

    private fun createAndSaveCarObject(imageUrl: String) {
        val car = Car(
            id = UUID.randomUUID().toString(),
            name = binding.etName.text.toString().trim(),
            year = binding.etYear.text.toString().trim(),
            licence = binding.etLicence.text.toString().trim(),
            imageUrl = imageUrl,
            place = Place(
                lat = selectedLatitude ?: 0.0,
                long = selectedLongitude ?: 0.0
            )
        )

        // MUDANÇA: Usa ViewModel que conecta ao CarManager singleton
        // Isso garante que MainActivity será atualizada automaticamente
        viewModel.addCar(car)
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
                showToast("Selecione uma localização")
                false
            }
            else -> true
        }
    }

    private fun resetSaveButton() {
        binding.btnSaveCar.isEnabled = true
        binding.btnSaveCar.text = "Salvar Carro"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}
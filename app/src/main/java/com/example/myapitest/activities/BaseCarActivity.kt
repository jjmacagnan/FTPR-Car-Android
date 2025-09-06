package com.example.myapitest.activities.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapitest.R
import com.example.myapitest.models.Place
import com.example.myapitest.utils.ImageSelectionHelper
import com.example.myapitest.utils.ImageUploadHelper
import com.example.myapitest.utils.UiState
import com.example.myapitest.utils.ValidationUtils
import com.example.myapitest.viewModel.CarViewModel
import com.example.myapitest.activities.MapActivity
import kotlinx.coroutines.launch

abstract class BaseCarActivity : AppCompatActivity() {

    protected var selectedImageUri: Uri? = null
    protected var selectedLatitude: Double? = null
    protected var selectedLongitude: Double? = null

    protected val viewModel: CarViewModel by viewModels()
    private lateinit var imageSelectionHelper: ImageSelectionHelper

    // Abstract methods that must be implemented by subclasses
    abstract fun setupSpecificUI()
    abstract fun getNameField(): String
    abstract fun getYearField(): String
    abstract fun getLicenceField(): String
    abstract fun setImageUri(uri: Uri)
    abstract fun updateSaveButton(enabled: Boolean, text: String)
    abstract fun performSaveOperation(imageUrl: String)
    abstract fun setLocationFields(latitude: Double, longitude: Double)
    abstract fun showFieldError(field: String, message: String)
    abstract fun resetSaveButton()
    abstract fun getCurrentImageUrl(): String

    // Activity Result Launcher for location
    private val locationPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                selectedLatitude = data.getDoubleExtra("latitude", 0.0)
                selectedLongitude = data.getDoubleExtra("longitude", 0.0)
                setLocationFields(selectedLatitude!!, selectedLongitude!!)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_FTPRCar)
        super.onCreate(savedInstanceState)
        setupImageSelectionHelper()
        setupSpecificUI()
        observeOperationState()
    }

    private fun setupImageSelectionHelper() {
        imageSelectionHelper = ImageSelectionHelper.create(
            activity = this,
            onImageSelected = { uri ->
                selectedImageUri = uri
                setImageUri(uri)
            },
            onError = { message ->
                showToast(message)
            }
        )
    }

    // Common UI methods
    protected fun showImagePickerDialog() {
        imageSelectionHelper.showImagePickerDialog()
    }

    protected fun openLocationPicker() {
        val intent = Intent(this, MapActivity::class.java)
        locationPickerLauncher.launch(intent)
    }

    // License plate mask setup using ValidationUtils
    protected fun setupLicenceMask(editText: android.widget.EditText) {
        editText.addTextChangedListener(ValidationUtils.createPlateTextWatcher())
    }

    // Validation using ValidationUtils
    protected fun validateFields(): Boolean {
        val name = getNameField()
        val year = getYearField()
        val licence = getLicenceField()
        val hasLocation = selectedLatitude != null && selectedLongitude != null

        val validationResult = ValidationUtils.validateCarFields(name, year, licence, hasLocation)

        if (!validationResult.isValid) {
            // Show field errors
            validationResult.fieldErrors.forEach { (field, message) ->
                when (field) {
                    "location" -> showToast(message)
                    else -> showFieldError(field, message)
                }
            }
        }

        return validationResult.isValid
    }

    // Common save operation
    protected fun handleSaveOperation() {
        if (!validateFields()) return

        updateSaveButton(false, "Salvando...")

        selectedImageUri?.let { uploadImageAndSave(it) }
            ?: performSaveOperation(getCurrentImageUrl())
    }

    private fun uploadImageAndSave(imageUri: Uri) {
        ImageUploadHelper.uploadImage(imageUri) { downloadUrl, error ->
            if (downloadUrl != null) {
                performSaveOperation(downloadUrl)
            } else {
                showToast("Erro no upload: ${error?.message}")
                resetSaveButton()
            }
        }
    }

    // Operation state observer
    private fun observeOperationState() {
        lifecycleScope.launch {
            viewModel.operationState.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                        resetSaveButton()
                    }
                    is UiState.Loading -> {
                        updateSaveButton(false, "Salvando...")
                    }
                    is UiState.Success -> {
                        showToast(state.data)
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

    // Helper methods
    protected fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Create Place object
    protected fun createPlace(lat: Double? = selectedLatitude, long: Double? = selectedLongitude): Place {
        return Place(
            lat = lat ?: 0.0,
            long = long ?: 0.0
        )
    }
}
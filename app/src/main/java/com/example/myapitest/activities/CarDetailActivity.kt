package com.example.myapitest.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapitest.R
import com.example.myapitest.databinding.ActivityCarDetailBinding
import com.example.myapitest.models.Car
import com.example.myapitest.network.CarRepository
import com.example.myapitest.utils.UiState
import com.example.myapitest.viewModel.CarViewModel
import kotlinx.coroutines.launch

class CarDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarDetailBinding
    private lateinit var car: Car

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

    // Launcher para editar carro
    private val editCarLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val updatedCar = result.data?.getParcelableExtra<Car>("updated_car")
            updatedCar?.let {
                car = it
                displayCarDetails()
                viewModel.updateCar(it) // Atualiza MainActivity automaticamente
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        car = intent.getParcelableExtra("car")!!
        setupUI()
        displayCarDetails()
        observeOperationState()
    }

    private fun setupUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = car.name

        binding.btnViewOnMap.setOnClickListener { openMap() }
        binding.btnEdit.setOnClickListener { editCar() }
        binding.btnDelete.setOnClickListener { showDeleteConfirmation() }
    }

    private fun displayCarDetails() {
        binding.apply {
            tvCarName.text = car.name
            tvCarYear.text = car.year
            tvLicence.text = car.licence
            tvLocation.text = "Lat: ${car.place.lat}, Lng: ${car.place.long}"

            Glide.with(this@CarDetailActivity)
                .load(car.imageUrl)
                .placeholder(R.drawable.placeholder_car)
                .error(R.drawable.placeholder_car_simple)
                .into(ivCarImage)
        }
    }

    private fun openMap() {
        val intent = Intent(this, MapViewActivity::class.java)
        intent.putExtra("latitude", car.place.lat)
        intent.putExtra("longitude", car.place.long)
        intent.putExtra("title", car.name)
        startActivity(intent)
    }

    private fun editCar() {
        val intent = Intent(this, EditCarActivity::class.java)
        intent.putExtra("car", car)
        editCarLauncher.launch(intent)
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar exclusão")
            .setMessage("Tem certeza que deseja excluir este carro?")
            .setPositiveButton("Excluir") { _, _ -> deleteCar() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteCar() {
        binding.btnDelete.isEnabled = false
        viewModel.deleteCar(car.id)
    }

    private fun observeOperationState() {
        lifecycleScope.launch {
            viewModel.operationState.collect { state ->
                when (state) {
                    is UiState.Idle -> binding.btnDelete.isEnabled = true
                    is UiState.Loading -> Toast.makeText(this@CarDetailActivity, "Processando...", Toast.LENGTH_SHORT).show()
                    is UiState.Success -> {
                        Toast.makeText(this@CarDetailActivity, state.data, Toast.LENGTH_SHORT).show()
                        finish() // Fecha a activity e volta para MainActivity
                    }
                    is UiState.Error -> {
                        Toast.makeText(this@CarDetailActivity, "Erro: ${state.message}", Toast.LENGTH_SHORT).show()
                        binding.btnDelete.isEnabled = true
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
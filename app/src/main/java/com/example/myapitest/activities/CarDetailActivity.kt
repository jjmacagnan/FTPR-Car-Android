package com.example.myapitest.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapitest.R
import com.example.myapitest.databinding.ActivityCarDetailBinding
import com.example.myapitest.models.Car
import com.example.myapitest.network.ApiClient
import kotlinx.coroutines.launch

class CarDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarDetailBinding
    private lateinit var car: Car

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        car = intent.getSerializableExtra("car") as Car

        setupUI()
        displayCarDetails()
    }

    private fun setupUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "${car.brand} ${car.model}"

        binding.btnViewOnMap.setOnClickListener {
            openMap()
        }

        binding.btnEdit.setOnClickListener {
            editCar()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun displayCarDetails() {
        binding.apply {
            tvCarBrand.text = car.brand
            tvCarModel.text = car.model
            tvCarYear.text = car.year.toString()
            tvCarColor.text = car.color
            tvCarPrice.text = "R$ ${String.format("%.2f", car.price)}"
            tvLocation.text = "Lat: ${car.location.latitude}, Lng: ${car.location.longitude}"

            // Carregar imagem - USANDO PLACEHOLDER CORRIGIDO
            Glide.with(this@CarDetailActivity)
                .load(car.imageUrl)
                .placeholder(R.drawable.placeholder_car) // ALTERADO
                .error(R.drawable.placeholder_car_simple) // ALTERADO
                .into(ivCarImage)
        }
    }

    private fun openMap() {
        val intent = Intent(this, MapViewActivity::class.java)
        intent.putExtra("latitude", car.location.latitude)
        intent.putExtra("longitude", car.location.longitude)
        intent.putExtra("title", "${car.brand} ${car.model}")
        startActivity(intent)
    }

    private fun editCar() {
        val intent = Intent(this, EditCarActivity::class.java)
        intent.putExtra("car", car)
        startActivity(intent)
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar exclusão")
            .setMessage("Tem certeza que deseja excluir este carro?")
            .setPositiveButton("Excluir") { _, _ ->
                deleteCar()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteCar() {
        lifecycleScope.launch {
            try {
                car.id?.let { carId ->
                    val response = ApiClient.carService.deleteCar(carId)
                    if (response.isSuccessful) {
                        showToast("Carro excluído com sucesso!")
                        finish()
                    } else {
                        showToast("Erro ao excluir carro")
                    }
                }
            } catch (e: Exception) {
                showToast("Erro de conexão: ${e.message}")
            }
        }
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
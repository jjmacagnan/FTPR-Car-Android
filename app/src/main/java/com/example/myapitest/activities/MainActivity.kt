package com.example.myapitest.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.R
import com.example.myapitest.adapters.CarAdapter
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.models.Car
import com.example.myapitest.network.ApiClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var carAdapter: CarAdapter
    private val cars = mutableListOf<Car>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupUI()
        setupRecyclerView()
        loadCars()
    }

    private fun setupUI() {
        binding.fabAddCar.setOnClickListener {
            startActivity(Intent(this, AddCarActivity::class.java))
        }

        setSupportActionBar(binding.toolbar)
    }

    private fun setupRecyclerView() {
        carAdapter = CarAdapter(cars) { car ->
            // Clique no item do carro
            val intent = Intent(this, CarDetailActivity::class.java)
            intent.putExtra("car", car)
            startActivity(intent)
        }

        binding.recyclerViewCars.apply {
            adapter = carAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun loadCars() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.carService.getCars()
                if (response.isSuccessful) {
                    response.body()?.let { carList ->
                        cars.clear()
                        cars.addAll(carList)
                        carAdapter.notifyDataSetChanged()
                    }
                } else {
                    showToast("Erro ao carregar carros")
                }
            } catch (e: Exception) {
                showToast("Erro de conexão: ${e.message}")
            }
        }
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
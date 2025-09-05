package com.example.myapitest.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.R
import com.example.myapitest.adapters.CarAdapter
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.models.Car
import com.example.myapitest.utils.UiState
import com.example.myapitest.viewModel.CarViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var carAdapter: CarAdapter
    private lateinit var auth: FirebaseAuth

    // Agora usa ViewModel simplificado (sem Factory necessário)
    private val viewModel: CarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeUiStates()

        viewModel.fetchCars()
    }

    override fun onResume() {
        super.onResume()
        viewModel.clearOperationState()
        checkUserAuthentication()

        // MUDANÇA IMPORTANTE: Sempre observa o estado quando volta para MainActivity
        // Como agora usamos CarManager singleton, as mudanças serão automáticas
    }

    private fun checkUserAuthentication() {
        if (auth.currentUser == null) navigateToLogin()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
        auth.currentUser?.let { user ->
            supportActionBar?.subtitle = user.phoneNumber ?: "Usuário logado"
        }
    }

    private fun setupRecyclerView() {
        carAdapter = CarAdapter { car ->
            val intent = Intent(this, CarDetailActivity::class.java)
            intent.putExtra("car", car)
            startActivity(intent)
        }

        binding.recyclerViewCars.apply {
            adapter = carAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupFab() {
        binding.fabAddCar.setOnClickListener {
            startActivity(Intent(this, AddCarActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                refreshCars()
                true
            }
            R.id.action_logout -> {
                showLogoutConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Logout")
            .setMessage("Tem certeza que deseja sair da sua conta?")
            .setPositiveButton("Sair") { _, _ -> performLogout() }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun performLogout() {
        showToast("Fazendo logout...")
        auth.signOut()
        navigateToLogin()
        showToast("Logout realizado com sucesso!")
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun observeUiStates() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                // IMPORTANTE: Observa o estado global do CarManager
                launch {
                    viewModel.carsState.collect { state -> handleCarsState(state) }
                }
                launch {
                    viewModel.operationState.collect { state -> handleOperationState(state) }
                }
            }
        }
    }

    private fun handleCarsState(state: UiState<List<Car>>) {
        when (state) {
            is UiState.Idle -> hideLoading()
            is UiState.Loading -> showLoading()
            is UiState.Success -> {
                hideLoading()
                if (state.data.isEmpty()) showEmptyState() else {
                    hideEmptyState()
                    carAdapter.submitList(state.data)
                }
            }
            is UiState.Error -> {
                hideLoading()
                showEmptyState()
                showToast("Erro ao carregar carros: ${state.message}")
            }
        }
    }

    private fun handleOperationState(state: UiState<String>) {
        when (state) {
            is UiState.Idle -> {}
            is UiState.Loading -> showToast("Processando...")
            is UiState.Success -> showToast(state.data)
            is UiState.Error -> showToast("Erro na operação: ${state.message}")
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewCars.visibility = View.GONE
        binding.textViewEmpty.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerViewCars.visibility = View.VISIBLE
    }

    private fun showEmptyState() {
        binding.recyclerViewCars.visibility = View.GONE
        binding.textViewEmpty.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        binding.recyclerViewCars.visibility = View.VISIBLE
        binding.textViewEmpty.visibility = View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun refreshCars() {
        showToast("Atualizando lista de carros...")
        viewModel.fetchCars()
    }
}
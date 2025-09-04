package com.example.myapitest.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.R
import com.example.myapitest.databinding.ActivityMapViewBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.view.View


class MapViewActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapViewBinding
    private lateinit var googleMap: GoogleMap
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        title = intent.getStringExtra("title") ?: "Localização"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = title

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupUI()
    }

    private fun setupUI() {
        binding.fabCenterLocation.setOnClickListener {
            centerMapOnLocation()
        }

        // Mostrar card com informações
        binding.cardLocationInfo.visibility = View.VISIBLE
        binding.tvLocationTitle.text = title
        binding.tvLocationCoordinates.text = "Lat: $latitude, Lng: $longitude"
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(latitude, longitude))
                .title(title)
                .snippet("Lat: $latitude, Lng: $longitude")
        )
        centerMapOnLocation()

        // Configurar tipo de mapa
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Habilitar controles
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = false
    }

    private fun centerMapOnLocation() {
        if (::googleMap.isInitialized) {
            val location = LatLng(latitude, longitude)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
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

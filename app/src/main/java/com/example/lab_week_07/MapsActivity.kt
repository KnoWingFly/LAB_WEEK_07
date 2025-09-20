package com.example.lab_week_07

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.lab_week_07.databinding.ActivityMapsBinding
import com.google.android.gms. location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()){
                    isGranted -> if(isGranted){
                getLastLocation()
            }else{
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
            }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        when{
            hasLocationPermission() -> getLastLocation()
            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
            else -> requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
    }

    private fun updateMapLocation(location: LatLng){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 7f))
    }

    private fun addMarkerAtLocation(location: LatLng, title: String){
        mMap.addMarker(MarkerOptions().title(title).position(location))
    }

    private fun getLastLocation(){
        if(hasLocationPermission()){
            try{
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            val userLocation = LatLng(location.latitude, location.longitude)
                            updateMapLocation(userLocation)
                            addMarkerAtLocation(userLocation, "Your Location")
                        }
                    }
            } catch (e: SecurityException){
                Log.e("MapsActivity", "SecurityException: ${e.message}")
            }
        } else {
            requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
    }

    private fun showPermissionRationale(positiveAction: () -> Unit){
        AlertDialog.Builder(this)
            .setTitle("Location Permission")
            .setMessage("This app will not work without knowing your current location")
            .setPositiveButton(android.R.string.ok) {_, _ -> positiveAction()}
            .setNegativeButton(android.R.string.cancel) {dialog, _ -> dialog.dismiss()}
            .create().show()
    }

    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

}
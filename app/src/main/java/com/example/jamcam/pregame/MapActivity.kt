package com.example.jamcam.pregame

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.jamcam.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveStartedListener,
    GoogleMap.OnCameraIdleListener {

    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    private val DEFAULT_ZOOM = 15f

    private var mMap: GoogleMap? = null
    private lateinit var mapView: MapView
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var tvCurrentAddress: TextView
    private lateinit var btnCancel: Button
    private lateinit var btnConfirm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.map)
        tvCurrentAddress = findViewById(R.id.tvAddress)
        btnCancel = findViewById(R.id.btnMapCancel)
        btnConfirm = findViewById(R.id.btnMapConfirm)

        btnCancel.setOnClickListener {
            quit(null)
        }
        btnConfirm.setOnClickListener {
            quit(tvCurrentAddress.text.toString())
        }

        getCurrentLocation()
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@MapActivity)

        try {
            val location = fusedLocationProviderClient!!.getLastLocation()
            location.addOnCompleteListener { p0 ->
                if (p0.isSuccessful) {
                    val currentLocation = p0.result
                    if (currentLocation != null) {
                        moveCamera(
                            LatLng(currentLocation.latitude, currentLocation.longitude),
                            DEFAULT_ZOOM
                        )
                    }
                } else {
                    Toast.makeText(
                        this@MapActivity,
                        "Current location not found.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (se: Exception) {
            Log.e("TAG", "Security Exception")
        }
    }

    private fun moveCamera(latLng: LatLng, defaultZoom: Float) {
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, defaultZoom))
    }

    private fun setAddress(address: Address) {
        if (address.getAddressLine(0) != null) {
            tvCurrentAddress.text = address.getAddressLine(0)
        }
        if (address.getAddressLine(1) != null) {
            tvCurrentAddress.text = tvCurrentAddress.text.toString() + address.getAddressLine(1)
        }
    }

    private fun quit(result: String?) {
        val returnIntent = Intent()
        if (result != null && result != "No address") {
            returnIntent.putExtra("address", result)
        }
        setResult(Activity.RESULT_OK, returnIntent)
        println(result)
        finish()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapView.onResume()
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap!!.isMyLocationEnabled = true
        mMap!!.setOnCameraMoveListener(this)
        mMap!!.setOnCameraMoveStartedListener(this)
        mMap!!.setOnCameraIdleListener(this)

    }

    public override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onLocationChanged(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        setAddress(addresses!![0])
    }

    override fun onCameraMove() {
    }

    override fun onCameraMoveStarted(p0: Int) {
    }

    override fun onCameraIdle() {
        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(
                mMap!!.cameraPosition.target.latitude,
                mMap!!.cameraPosition.target.longitude,
                1
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (addresses != null) {
            if (addresses.isNotEmpty()) {
                setAddress(addresses[0])
            }
        }
    }
}
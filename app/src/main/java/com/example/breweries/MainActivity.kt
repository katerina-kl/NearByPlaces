package com.example.breweries

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.breweries.adapters.BreweriesAdapter
import com.example.breweries.databinding.ActivityMainBinding
import com.example.breweries.retrofit.RetrofitInstance
import com.google.gson.Gson
import okhttp3.*
import retrofit2.HttpException
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var breweriesAdapter: BreweriesAdapter
    private var currentLocation: Location? = null
    private var locationByGps: Location? = null
    private var locationByNetwork: Location? = null
    private lateinit var locationManager: LocationManager

    companion object {
        var deviceLatitude: Double = 0.0
        var deviceLongitude: Double = 0.0
        lateinit var context: Context
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this

        isLocationPermissionGranted()
        getLocation()
        setRecyclerView()
        getAllBreweries()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String?): Boolean {
                binding.searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(text: String?): Boolean {
                isLocationPermissionGranted() //if the user has not accepted the permission asks again, to be able to search on the search bar

                breweriesAdapter.breweries.toMutableList()
                    .clear() // the list clears every time the user types
                val searchText = text!!.toLowerCase(Locale.getDefault())
                if (searchText.isNotEmpty()) {
                    breweriesAdapter.breweries.forEach {

                        if (it.city.toLowerCase(Locale.getDefault()).contains(searchText)) {
                            breweriesAdapter.breweries.toMutableList()
                                .add(it) // it adds all the objects ,that contain the city user is typing, to the list
                        }
                    }
                    getBreweriesByCity(text) //does the api request for objects searched by city
                    breweriesAdapter.notifyDataSetChanged()

                } else {
                    //it shows all objects when the search view in empty
                    breweriesAdapter.breweries.toMutableList().clear()
                    breweriesAdapter.notifyDataSetChanged()
                    getAllBreweries()

                }
                return false
            }
        })

    }

    private fun isLocationPermissionGranted(): Boolean {
        //checks if the user has accepted the permission for the device location
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
            false
        } else {
            true
        }
    }

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    1
                )
                return
            }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                0F,
                gpsLocationListener
            )//updates the location every 5000 ms (gps)
        }

        if (hasNetwork) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                0F,
                networkLocationListener
            )//updates the location every 5000 ms (network)

        }

        val lastKnownLocationByGps =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        lastKnownLocationByGps?.let {
            locationByGps = lastKnownLocationByGps
        }

        val lastKnownLocationByNetwork =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        lastKnownLocationByNetwork?.let {
            locationByNetwork = lastKnownLocationByNetwork
        }

        if (locationByNetwork != null) {
            //initialize the current location
            currentLocation = locationByNetwork
            deviceLatitude = currentLocation!!.latitude
            deviceLongitude = currentLocation!!.longitude
            if (locationByGps != null) {
                if (locationByGps!!.accuracy > locationByNetwork!!.accuracy) {
                    currentLocation = locationByGps
                    deviceLatitude = currentLocation!!.latitude
                    deviceLongitude = currentLocation!!.longitude
                }
            }
        }
    }

    private val gpsLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationByGps = location
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private val networkLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationByNetwork = location
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun getAllBreweries() {
        lifecycleScope.launchWhenCreated {
            val response = try {
                RetrofitInstance.api.getBreweries()
            } catch (e: IOException) {
                return@launchWhenCreated
            } catch (e: HttpException) {
                return@launchWhenCreated
            }
            if (response.isSuccessful && response.body() != null) {
                breweriesAdapter.breweries = response.body()!!
            } else {
                Log.e(TAG, "" + response.message())
            }
        }
    }

    private fun getBreweriesByCity(city: String) {
        lifecycleScope.launchWhenCreated {
            val response = try {
                RetrofitInstance.api.getBreweriesByCity(city)
            } catch (e: IOException) {
                return@launchWhenCreated
            } catch (e: HttpException) {
                return@launchWhenCreated
            }
            if (response.isSuccessful && response.body() != null) {
                breweriesAdapter.breweries = response.body()!!
            } else {
                Log.e(TAG, "" + response.message())
            }
        }
    }

    private fun setRecyclerView() = binding.recycleView.apply {
        breweriesAdapter = BreweriesAdapter()
        adapter = breweriesAdapter
        layoutManager = LinearLayoutManager(this@MainActivity)
    }

}
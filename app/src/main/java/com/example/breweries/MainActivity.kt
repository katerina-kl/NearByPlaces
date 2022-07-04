package com.example.breweries

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.breweries.adapters.BreweriesAdapter
import com.example.breweries.data.BreweryObject
import com.example.breweries.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var breweriesAdapter: BreweriesAdapter
    private var currentLocation: Location? = null
    private var locationByGps: Location? = null
    private var locationByNetwork: Location? = null
    private lateinit var locationManager: LocationManager
    private var isConnected: Boolean = true

    companion object {
        var deviceLatitude: Double = 0.0
        var deviceLongitude: Double = 0.0
        lateinit var context: Context
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    private val networkCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ConnectivityManager.NetworkCallback() {
        // network is available for use
        override fun onAvailable(network: Network) {
            isConnected=true
            Toast.makeText(context, "connected", Toast.LENGTH_SHORT).show()
            super.onAvailable(network)
        }

        // Network capabilities have changed for the network
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        }

        // lost network connection
        override fun onLost(network: Network) {
            Toast.makeText(context, "not connected", Toast.LENGTH_SHORT).show()
            isConnected=false
            super.onLost(network)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this


        isLocationPermissionGranted()
        getLocation()
        setRecyclerView()

        val connectivityManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        } else {
            TODO("VERSION.SDK_INT < M")
        }
        connectivityManager.requestNetwork(networkRequest, networkCallback)
        if (isConnected){
            getAllBreweries()
            Toast.makeText(context, "get all", Toast.LENGTH_SHORT).show()
        }


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
                    if (isConnected) {
                        getBreweriesByCity(text) //does the api request for objects searched by city
                        breweriesAdapter.notifyDataSetChanged()
                    }

                } else {
                    //it shows all objects when the search view in empty
                    if (isConnected) {
                        breweriesAdapter.breweries.toMutableList().clear()
                        breweriesAdapter.notifyDataSetChanged()
                        getAllBreweries()
                    }

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
        CoroutineScope(Dispatchers.IO).launch {
            val rss = NetworkUtility.request("/breweries")
            withContext(Dispatchers.Main) {
                // call to UI thread and parse response
                handleJson(rss)

            }
        }
    }

    private fun handleJson(jsonString: String?) {
        val jsonArrayList = JSONArray(jsonString)
        val list = ArrayList<BreweryObject>()
        var i = 0
        while (i < jsonArrayList.length()) {
            val jsonObject = jsonArrayList.getJSONObject(i)
            list.add(
                BreweryObject(
                    jsonObject.getString("id"),
                    jsonObject.getString("name"),
                    jsonObject.getString("brewery_type"),
                    jsonObject.getString("street"),
                    jsonObject.getString("address_2"),
                    jsonObject.getString("address_3"),
                    jsonObject.getString("city"),
                    jsonObject.getString("state"),
                    jsonObject.getString("county_province"),
                    jsonObject.getString("postal_code"),
                    jsonObject.getString("country"),
                    jsonObject.getString("longitude"),
                    jsonObject.getString("latitude"),
                    jsonObject.getString("phone"),
                    jsonObject.getString("website_url"),
                    jsonObject.getString("updated_at"),
                    jsonObject.getString("created_at")
                )
            )

            i++
        }
        breweriesAdapter.breweries = list
    }

    private fun getBreweriesByCity(city: String) {

        CoroutineScope(Dispatchers.IO).launch {
            val rss = NetworkUtility.request("/breweries?by_city=" + city)
            withContext(Dispatchers.Main) {
                // call to UI thread and parse response
                handleJson(rss)

            }
        }

    }

    private fun setRecyclerView() = binding.recycleView.apply {
        breweriesAdapter = BreweriesAdapter()
        adapter = breweriesAdapter
        layoutManager = LinearLayoutManager(this@MainActivity)
    }

}
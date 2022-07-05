package com.example.breweries

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.breweries.LocationPermission.Companion.LOCATION_REQUEST_CODE
import com.example.breweries.adapters.BreweriesAdapter
import com.example.breweries.data.BreweryObject
import com.example.breweries.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.*


class MainActivity : AppCompatActivity(), androidx.appcompat.widget.SearchView.OnQueryTextListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var breweriesAdapter: BreweriesAdapter
    private var currentLocation: Location? = null
    private var locationByGps: Location? = null
    private var locationByNetwork: Location? = null
    private lateinit var database: BreweryDBHelper
    private lateinit var locationPermission: LocationPermission
    private lateinit var locationManager: LocationManager

    companion object {
        var deviceLatitude: Double = 0.0
        var deviceLongitude: Double = 0.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = BreweryDBHelper(this)
        locationPermission= LocationPermission()
        locationPermission.setupPermissions(this)

        //getLocation()
        setRecyclerView()

        if (NetworkUtility.isOnline(this)) {
            getAllBreweries(database)
        } else {
            breweriesAdapter.breweries.toMutableList().clear()
            val data = database.readAllBreweries()

            breweriesAdapter.breweries = data.toList()
            breweriesAdapter.notifyDataSetChanged()
        }

        binding.searchView.setOnQueryTextListener(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.i("kati", "onRequestPermissionsResult: code "+requestCode)

        when (requestCode) {
            LOCATION_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    locationPermission.showDialog(this)
                    Log.i("kati", "onRequestPermissionsResult: ")
                }
            }
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
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
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

    private fun getAllBreweries(database: BreweryDBHelper) {
        CoroutineScope(Dispatchers.IO).launch {
            val rss = NetworkUtility.request("/breweries")
            withContext(Dispatchers.Main) {
                // call to UI thread and parse response
                handleJson(rss, database)
            }
        }
    }

    private fun handleJson(jsonString: String?, database: BreweryDBHelper) {
        val jsonArrayList = JSONArray(jsonString)
        val list = ArrayList<BreweryObject>()
        var breweryObject: BreweryObject
        var i = 0
        while (i < jsonArrayList.length()) {
            val jsonObject = jsonArrayList.getJSONObject(i)
            breweryObject = BreweryObject(
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
            list.add(breweryObject)
            database.insertBrewery(breweryObject)
            i++
        }
        breweriesAdapter.breweries = list
    }

    private fun getBreweriesByCity(city: String, database: BreweryDBHelper) {

        CoroutineScope(Dispatchers.IO).launch {
            val rss = NetworkUtility.request("/breweries?by_city=$city")
            withContext(Dispatchers.Main) {
                // call to UI thread and parse response
                handleJson(rss, database)
            }
        }
    }

    private fun setRecyclerView() = binding.recycleView.apply {
        breweriesAdapter = BreweriesAdapter()
        adapter = breweriesAdapter
        layoutManager = LinearLayoutManager(this@MainActivity)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(text: String?): Boolean {

        breweriesAdapter.breweries.toMutableList().clear() // the list clears every time the user types
        val searchText = text!!.toLowerCase(Locale.getDefault())
        if (searchText.isNotEmpty()) {
            if (NetworkUtility.isOnline(applicationContext)) {
                getBreweriesByCity(
                    text,
                    database
                ) //does the api request for objects searched by city
            } else {
                val data = database.readBreweriesByCity(text)
                breweriesAdapter.breweries = data.toList()
            }
            breweriesAdapter.notifyDataSetChanged()

        } else {
            //it shows all objects when the search view in empty
            breweriesAdapter.breweries.toMutableList().clear()
            if (NetworkUtility.isOnline(applicationContext)) {
                getAllBreweries(database)
            } else {
                val data = database.readAllBreweries()
                breweriesAdapter.breweries = data.toList()
            }
            breweriesAdapter.notifyDataSetChanged()
        }
        return false
    }
}
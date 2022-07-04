package com.example.breweries

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.breweries.adapters.BreweriesAdapter
import com.example.breweries.data.BreweryObject
import com.example.breweries.databinding.ActivityMainBinding
import com.example.breweries.retrofit.RetrofitInstance
import com.google.gson.Gson
import okhttp3.*
import org.json.JSONArray
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


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
//        lifecycleScope.launchWhenCreated {
//            val response = try {
//                RetrofitInstance.api.getBreweries()
//            } catch (e: IOException) {
//                return@launchWhenCreated
//            } catch (e: HttpException) {
//                return@launchWhenCreated
//            }
//            if (response.isSuccessful && response.body() != null) {
//                breweriesAdapter.breweries = response.body()!!
//            } else {
//                Log.e(TAG, "" + response.message())
//            }
//        }

        AsyncTaskHandleJson().execute("https://api.openbrewerydb.org/breweries")


    }
    inner class AsyncTaskHandleJson : AsyncTask<String,String,String>(){
        override fun doInBackground(vararg url: String?): String {
            var text :String
            val connection = URL(url[0]).openConnection() as HttpURLConnection
            try {
                connection.connect()
                text = connection.inputStream.use {
                    it.reader().use {
                        reader -> reader.readText()
                    }
                }
            }finally {
                connection.disconnect()
            }
            return text
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            handleJson(result)
        }

    }
    private fun handleJson(jsonString:String?){
        val jsonArrayList = JSONArray(jsonString)
        val list = ArrayList<BreweryObject>()
        var i=0
        while (i <jsonArrayList.length()){
            val jsonObject = jsonArrayList.getJSONObject(i)
            list.add(BreweryObject(jsonObject.getString("id"),jsonObject.getString("name"),jsonObject.getString("brewery_type"),jsonObject.getString("street"),jsonObject.getString("address_2")
            ,jsonObject.getString("address_3"),jsonObject.getString("city"),jsonObject.getString("state"),jsonObject.getString("county_province"),jsonObject.getString("postal_code"),jsonObject.getString("country")
            ,jsonObject.getString("longitude"),jsonObject.getString("latitude"),jsonObject.getString("phone"),jsonObject.getString("website_url"),jsonObject.getString("updated_at"),jsonObject.getString("created_at")
            ))

            i++
        }
        breweriesAdapter.breweries = list


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
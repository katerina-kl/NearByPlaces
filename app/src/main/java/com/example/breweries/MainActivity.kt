package com.example.breweries

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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

    private lateinit var database: BreweryDBHelper
    private lateinit var locationPermission: LocationPermission


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = BreweryDBHelper(this)
        locationPermission = LocationPermission()
        locationPermission.setupPermissions(this)
        if (locationPermission.permissionIsGranted(this)){
            setupUi()
        }

    }
    fun setupUi(){
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

        when (requestCode) {
            LOCATION_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    locationPermission.showDialog(this)
                } else {
                    locationPermission.getLocation(this)
                    setupUi()
                }
            }
        }

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

        breweriesAdapter.breweries.toMutableList()
            .clear() // the list clears every time the user types
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
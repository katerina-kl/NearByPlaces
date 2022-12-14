package com.example.breweries

import android.content.pm.PackageManager
import android.os.Bundle
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

    }

    override fun onResume() {
        if (locationPermission.permissionIsGranted(this)){
            //only if location permission is granted it will show recyclerview with breweries and request location of device
            setupUi()
            locationPermission.getLocation(this)
        }
        super.onResume()
    }

    private fun setupUi(){
        setRecyclerView()

        if (NetworkUtility.isOnline(this)) {
            //make api call to get a list of breweries if is connected to the internet
            getAllBreweries(database)
        } else {
            //get data stored in database and added to the list
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
                    //is permission is not granted , shows dialog to request permission
                    locationPermission.showDialog(this,this.resources.getString(R.string.dialog_title),this.resources.getString(R.string.dialog_subtitle))
                } else {
                    //is permissions are granted , get the device's location and set up the home page ui
                    locationPermission.getLocation(this)
                    setupUi()
                }
            }
        }

    }


    private fun getAllBreweries(database: BreweryDBHelper) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = NetworkUtility.request("/breweries")
            withContext(Dispatchers.Main) {
                // call to UI thread and parse response
                handleJson(response, database)
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
            //insert object to database
            database.insertBrewery(breweryObject)
            i++
        }
        breweriesAdapter.breweries = list
    }

    private fun getBreweriesByCity(city: String, database: BreweryDBHelper) {

        CoroutineScope(Dispatchers.IO).launch {
            val response = NetworkUtility.request("/breweries?by_city=$city")
            withContext(Dispatchers.Main) {
                // call to UI thread and parse response
                handleJson(response, database)
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

        if (!locationPermission.isLocationEnabled(this)){
            locationPermission.getLocation(this)
        }
        breweriesAdapter.breweries.toMutableList()
            .clear() // the list clears every time the user types
        val searchText = text!!.toLowerCase(Locale.getDefault())
        if (searchText.isNotEmpty()) {
            if (NetworkUtility.isOnline(applicationContext)) {
                getBreweriesByCity(
                    text,
                    database
                ) //does the api request for objects searched by city when online
            } else {
                //reads data from database and add them to the list
                val data = database.readBreweriesByCity(text)
                breweriesAdapter.breweries = data.toList()
            }
            breweriesAdapter.notifyDataSetChanged()

        } else {
            //it shows all objects when the search view in empty
            breweriesAdapter.breweries.toMutableList().clear()
            if (NetworkUtility.isOnline(applicationContext)) {
                //does the api request for all brewery objects when online
                getAllBreweries(database)
            } else {
                //reads data from database and add them to the list
                val data = database.readAllBreweries()
                breweriesAdapter.breweries = data.toList()
            }
            breweriesAdapter.notifyDataSetChanged()
        }
        return false
    }
}
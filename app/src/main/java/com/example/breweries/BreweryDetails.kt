package com.example.breweries

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.breweries.databinding.ActivityBreweryDetailsBinding
import com.example.breweries.data.BreweryObject
import com.google.gson.Gson

class BreweryDetails : AppCompatActivity() {
    private lateinit var binding: ActivityBreweryDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBreweryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gson = Gson()
        val breweryObject = gson.fromJson<BreweryObject>(intent.getStringExtra("brewery_object"), BreweryObject::class.java)
        //just getting data from the previous activity
        binding.name.text= breweryObject.name
        binding.street.text=breweryObject.street
        binding.city.text=breweryObject.city
        binding.state.text=breweryObject.state
        binding.distance.text= intent.getStringExtra("distance")

    }
}
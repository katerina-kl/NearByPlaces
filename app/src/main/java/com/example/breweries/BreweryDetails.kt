package com.example.breweries

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.example.breweries.data.BreweryObject
import com.example.breweries.databinding.ActivityBreweryDetailsBinding
import com.google.gson.Gson

class BreweryDetails : AppCompatActivity() {
    private lateinit var binding: ActivityBreweryDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBreweryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gson = Gson()
        val breweryObject = gson.fromJson<BreweryObject>(
            intent.getStringExtra("brewery_object"),
            BreweryObject::class.java
        )
        //just getting data from the previous activity
        if (breweryObject.name != "null") {
            binding.name.visibility = VISIBLE
            binding.textViewName.visibility = VISIBLE
            binding.name.text = breweryObject.name

        } else {
            binding.name.visibility = GONE
            binding.textViewName.visibility = GONE
        }
        if (breweryObject.street != "null") {
            binding.street.visibility = VISIBLE
            binding.textViewStreet.visibility = VISIBLE
            binding.street.text = breweryObject.street

        } else {
            binding.street.visibility = GONE
            binding.textViewStreet.visibility = GONE
        }
        if (breweryObject.city != "null") {
            binding.city.visibility = VISIBLE
            binding.textViewCity.visibility = VISIBLE
            binding.city.text = breweryObject.city

        } else {
            binding.city.visibility = GONE
            binding.textViewCity.visibility = GONE
        }
        if (breweryObject.state != "null") {
            binding.state.visibility = VISIBLE
            binding.textViewState.visibility = VISIBLE
            binding.state.text = breweryObject.state

        } else {
            binding.state.visibility = GONE
            binding.textViewState.visibility = GONE
        }
        if (intent.getStringExtra("distance")!!.isNotEmpty()) {
            binding.distance.visibility = VISIBLE
            binding.textViewDistance.visibility = VISIBLE
            binding.distance.text = intent.getStringExtra("distance")

        } else {
            binding.distance.visibility = GONE
            binding.textViewDistance.visibility = GONE
        }


    }
}
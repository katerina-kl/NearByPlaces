package com.example.breweries.adapters

import android.content.Intent
import android.location.Location
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.breweries.BreweryDetails
import com.example.breweries.LocationPermission.Companion.deviceLatitude
import com.example.breweries.LocationPermission.Companion.deviceLongitude
import com.example.breweries.data.BreweryObject
import com.example.breweries.databinding.BreweryLayoutBinding
import com.google.gson.Gson

class BreweriesAdapter : RecyclerView.Adapter<BreweriesAdapter.BreweriesViewHolder>() {

    inner class BreweriesViewHolder(val binding: BreweryLayoutBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    private val diffCallback = object : DiffUtil.ItemCallback<BreweryObject>() {
        override fun areContentsTheSame(oldItem: BreweryObject, newItem: BreweryObject): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: BreweryObject, newItem: BreweryObject): Boolean {
            return oldItem.id == newItem.id
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)
    var breweries: List<BreweryObject>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreweriesViewHolder {
        return BreweriesViewHolder(
            BreweryLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BreweriesViewHolder, position: Int) {
        holder.binding.apply {
            val brewery = breweries[position]

            name.text = brewery.name
            if (brewery.street != "null") {
                street.visibility = VISIBLE
                street.text = brewery.street
            } else
                street.visibility = GONE

            city.text = brewery.city + " ,"
            state.text = brewery.state
            if (deviceLatitude != 0.0
                && deviceLongitude != 0.0
                && brewery.latitude != "null"
                && brewery.longitude != "null"
            ) {

                var distanceToMiles = getDistance(
                    deviceLatitude,
                    deviceLongitude,
                    brewery.latitude.toDouble(),
                    brewery.longitude.toDouble()
                ).toDouble() * 0.000621 // with * 0.000621 i converted it from meters to miles

                distance.visibility = VISIBLE
                distance.text = distanceToMiles.toInt()
                    .toString() + " miles away" //and here i added toInt to round the number so it would be more pleasant for the eye
            } else {
                distance.visibility =
                    GONE //i added this because some fields in some objects were null
            }
            holder.itemView.setOnClickListener {
                val gson = Gson()
                val intent = Intent(it.context, BreweryDetails::class.java)
                //passes brewery object and distance text as calculated here to detail activity
                intent.putExtra("brewery_object", gson.toJson(brewery))
                intent.putExtra("distance", distance.text)
                it.context.startActivity(intent)
            }
        }
    }

    private fun getDistance(
        startLat: Double,
        startLang: Double,
        endLat: Double,
        endLang: Double
    ): Float {
        //calculates the distance from the device location to brewery's location
        val locStart = Location("")
        locStart.latitude = startLat
        locStart.longitude = startLang
        val locEnd = Location("")
        locEnd.latitude = endLat
        locEnd.longitude = endLang
        return locStart.distanceTo(locEnd)
    }

    override fun getItemCount(): Int {
        return breweries.size
    }
}
package com.example.breweries

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationPermission : AppCompatActivity() {

    companion object {
        val LOCATION_REQUEST_CODE = 1
        var deviceLatitude: Double = 0.0
        var deviceLongitude: Double = 0.0
    }
    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    fun setupPermissions(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            makeRequest(context)
        }
    }

    fun permissionIsGranted(context: Context): Boolean {
        return !(ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED)
    }

    private fun makeRequest(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_REQUEST_CODE
        )
    }

    fun showDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog_layout)
        val title = dialog.findViewById(R.id.title) as TextView
        title.text = context.resources.getString(R.string.dialog_title)
        val subtitle = dialog.findViewById(R.id.subtitle) as TextView
        subtitle.text = context.resources.getString(R.string.dialog_subtitle)
        val yesBtn = dialog.findViewById(R.id.button) as Button
        yesBtn.text = context.resources.getString(R.string.dialog_button)
        yesBtn.setOnClickListener {
            dialog.dismiss()
            makeRequest(context)
        }
        dialog.show()
    }

    fun getLocation(context: Context) {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        if (isLocationEnabled(context)){
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                makeRequest(context)
                return
            }
            fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                val location :Location? =it.result
                if (location == null){
                    Toast.makeText(context, "null location", Toast.LENGTH_SHORT).show()
                }else{
                    deviceLatitude =location.latitude
                    deviceLongitude =location.longitude
                    Toast.makeText(context, "success location"+ deviceLatitude + "  " + deviceLongitude, Toast.LENGTH_SHORT).show()
                }

            }
        }else{
            Toast.makeText(context, "please enable location", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }
    }

    fun isLocationEnabled(context: Context) :Boolean{
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

}
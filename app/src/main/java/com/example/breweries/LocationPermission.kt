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
import android.view.View.GONE
import android.view.View.VISIBLE
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
        //check if the permissions are granted
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            makeRequest(context)
        }
    }

    fun permissionIsGranted(context: Context): Boolean {
        return !(ActivityCompat.checkSelfPermission(
            context,
           Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED)
    }

    private fun makeRequest(context: Context) {
        //make request for location permission if not granted
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_REQUEST_CODE
        )
    }

    fun showDialog(context: Context,titleText:String,subtitleText:String) {
        //show dialog when permissions are not granted
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog_layout)
        val title = dialog.findViewById(R.id.title) as TextView
        title.text = titleText
        val subtitle = dialog.findViewById(R.id.subtitle) as TextView
        subtitle.text = subtitleText
        if (subtitleText == "") {
            subtitle.visibility = GONE
        } else {
            subtitle.visibility = VISIBLE
        }
        val yesBtn = dialog.findViewById(R.id.button) as Button
        yesBtn.text = context.resources.getString(R.string.dialog_button)
        yesBtn.setOnClickListener {
            dialog.dismiss()
            if (subtitleText == "") {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            }else{
                makeRequest(context)
            }
        }
        dialog.show()
    }

    fun getLocation(context: Context) {
       //get location of the device
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        if (permissionIsGranted(context)) {
            if (isLocationEnabled(context)) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    //make a request if the permission is not granted
                    makeRequest(context)
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                    //get the last location of device
                    val location: Location? = it.result
                    if (location == null) {
                    } else {
                        //sets the latitude && longitude
                        deviceLatitude = location.latitude
                        deviceLongitude = location.longitude
                    }

                }
            } else {
                //if the location is not enabled it shows the dialog
                showDialog(context,context.resources.getString(R.string.dialog_location_title),"")
            }
        }else{
            //if the permissions are not granted it makes a request again
            makeRequest(context)
        }
    }

    fun isLocationEnabled(context: Context) :Boolean{
        //returns true if GPS_PROVIDER or NETWORK_PROVIDER is enabled, false otherwise
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

}
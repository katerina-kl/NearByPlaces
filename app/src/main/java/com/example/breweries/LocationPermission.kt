package com.example.breweries

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class LocationPermission : AppCompatActivity() {

    companion object{
        val LOCATION_REQUEST_CODE = 1
    }

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

}
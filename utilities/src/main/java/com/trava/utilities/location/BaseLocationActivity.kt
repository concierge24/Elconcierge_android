package com.trava.utilities.location

import android.location.Location
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener

class BaseLocationActivity : AppCompatActivity(), OnSuccessListener<Location> {

    private lateinit var locationProvider: LocationProvider

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        locationProvider = LocationProvider.CurrentLocationBuilder(this).build()
        locationProvider.getLastKnownLocation(this)
    }

    override fun onResume() {
        super.onResume()
        locationProvider.getLastKnownLocation(this)
    }

    override fun onSuccess(p0: Location?) {

    }

}
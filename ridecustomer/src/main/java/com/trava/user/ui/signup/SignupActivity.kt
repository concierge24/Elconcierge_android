package com.trava.user.ui.signup

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.trava.user.R
import com.trava.user.ui.signup.login.LoginFragment
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction.setStatusBarColor
import com.trava.utilities.LocaleManager
import com.trava.utilities.location.LocationProvider
import java.util.*

class SignupActivity : AppCompatActivity() {

    private var locationProvider: LocationProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        supportFragmentManager.beginTransaction().add(R.id.container, LoginFragment()).commit()

        locationProvider = LocationProvider.LocationUpdatesBuilder(this).apply {
            interval = 500
            fastestInterval = 500
        }.build()
        locationProvider?.startLocationUpdates(locationUpdatesListener)

        val statusColor= Color.parseColor(ConfigPOJO.primary_color)
        setStatusBarColor(this, statusColor)
    }

    private val locationUpdatesListener = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            locationProvider?.stopLocationUpdates(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        locationProvider?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationProvider?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onBackPressed() {
        when {
            supportFragmentManager.backStackEntryCount > 0 -> supportFragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            else -> super.onBackPressed()
        }

    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }
}

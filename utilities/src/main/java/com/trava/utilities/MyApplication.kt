package com.trava.utilities

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.service.voice.VoiceInteractionService
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import androidx.slice.SliceManager
import com.crashlytics.android.Crashlytics
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import io.fabric.sdk.android.Fabric



class MyApplication : MultiDexApplication() {

    companion object {
        lateinit var instance: MyApplication
        lateinit var placesClient : PlacesClient
    }


    private val tag = "B_APP"

    override fun onCreate() {
        super.onCreate()
        instance = this
//       Places.initialize(applicationContext,"AIzaSyAEdac49YV93MRBmt4HWgVr4xqdw1GzOBI")
//       Places.initialize(applicationContext,"AIzaSyDY4z2mxbk9otYBTCQgvnsPMDoEmHFX3po")
       Places.initialize(applicationContext,"AIzaSyDNjbIaiPB41uhvhD0mb9Xdi2tA7n0AFlo")
//        Places.initialize(applicationContext,"AIzaSyDDx2MU39DY617n41XTo7L9f3Sp5i6bo7w")
        placesClient = Places.createClient(applicationContext)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        SharedPrefs.with(applicationContext)
        Fabric.with(this, Crashlytics())
        //grantSlicePermissions()
    }
    private val SLICE_AUTHORITY = "..."

    private fun grantSlicePermissions() {
        val context = getApplicationContext()
        val sliceProviderUri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(SLICE_AUTHORITY)
                .build()
        val assistantPackage = getAssistantPackage(context)
        if (assistantPackage == null)
        {
            return
        }
        SliceManager.getInstance(context)
                .grantSlicePermission(assistantPackage, sliceProviderUri)
    }
    private fun getAssistantPackage(context:Context): String? {
        val packageManager = context.getPackageManager()
        val resolveInfoList = packageManager.queryIntentServices(
                Intent(VoiceInteractionService.SERVICE_INTERFACE), 0)
        if (resolveInfoList.isEmpty())
        {
            return null
        }
        return resolveInfoList.get(0).serviceInfo.packageName
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.setLocale(base))
        Logger.e(tag, "attachBaseContext")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleManager.setLocale(this)
        Logger.e(tag, "onConfigurationChanged: " + newConfig.locale.getLanguage())
    }
}
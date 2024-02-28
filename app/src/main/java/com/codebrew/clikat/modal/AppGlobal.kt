package com.codebrew.clikat.modal

import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.location.LocationManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import co.paystack.android.PaystackSdk
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppToasty
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.di.component.DaggerAppComponent
import com.codebrew.clikat.modal.database.SearchCatListModel
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.utils.LocaleManager
import com.codebrew.clikat.utils.configurations.DrawablesConfig
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.newrelic.agent.android.NewRelic
import com.segment.analytics.Analytics
import com.trava.utilities.SharedPrefs
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject

//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;
/*
 * Created by Ankit Jindal on 18/4/16.
 */
class AppGlobal : MultiDexApplication(), HasAndroidInjector {

    @JvmField
    @Inject
    var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Any>? = null

    @Inject
    lateinit var prefHelper: PreferenceHelper

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        MultiDex.install(this)
        //ZopimChat.init("48yGhCMMohSibuqyfDSSiEcx8VSsCT54");
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        regular = Typeface.createFromAsset(assets, "fonts/proximanovaregular.otf")
        semi_bold = Typeface.createFromAsset(assets, "fonts/proximanovasemibold.otf")
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        FirebaseApp.initializeApp(this)

        Places.initialize(applicationContext,getString(R.string.google_map))

        //Realm
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .initialData { realm: Realm -> realm.createObject(SearchCatListModel::class.java) }
                .build()
        Realm.setDefaultConfiguration(realmConfig)
        //---------------------------------------------//

        NewRelic.withApplicationToken("AA4997859411278222d32dfcebe9be9a050bd91501-NRMA"
        ).start(this)

        //PayStack
        PaystackSdk.initialize(applicationContext)
        //-----------------------------------------

        Prefs.with(this)
        SharedPrefs.with(applicationContext)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        //Dagger
        DaggerAppComponent.builder()
                .application(this)
                .build()
                .inject(this)
        //--------------------------//
        DrawablesConfig.NAV_HOME = Drawable.createFromPath("")

        intializeEmoji()

        AppToasty.int(applicationContext)

        setDarkTheme()

    }

    private fun setDarkTheme() {

        val darkModeEnabled = prefHelper.getKeyValue(PrefenceConstants.DARK_MODE_ENABLE, PrefenceConstants.TYPE_BOOLEAN) as Boolean

        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

    }

    fun intializeEmoji() {
        val fontRequest = FontRequest(
                "com.example.fontprovider",
                "com.example",
                "emoji compat Font Query",
                R.array.com_google_android_gms_fonts_certs
        )
        val config = FontRequestEmojiCompatConfig(this, fontRequest)
        EmojiCompat.init(config)
    }

    override fun attachBaseContext(base: Context?) {
        localeManager = LocaleManager(base)
        super.attachBaseContext(localeManager?.setLocale(base ?: baseContext))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        localeManager?.setLocale(this)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return activityDispatchingAndroidInjector!!
    }


    companion object {
        @JvmField
        var regular: Typeface? = null

        @JvmField
        var semi_bold: Typeface? = null

        @JvmField
        var name = ""

        @JvmField
        var context: Context? = null

        var localeManager: LocaleManager? = null
    }
}
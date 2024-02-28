package com.trava.user

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.assist.AssistContent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import com.google.gson.Gson
import com.newrelic.agent.android.NewRelic
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.MainMenuActivity
import com.trava.user.ui.home.VideoPlayerScreen
import com.trava.user.ui.home.wallet.UserWalletActivity
import com.trava.user.ui.signup.RegisterOptionsActivity
import com.trava.user.ui.signup.SignupActivity
import com.trava.user.ui.signup.moby.landing.LandingScreen
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.walkthrough.WalkthroughActivity
import com.trava.user.webservices.models.applabels.AppForunSetting
import com.trava.user.webservices.models.applabels.Data
import com.trava.user.webservices.models.appsettings.SettingItems
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.*
import com.trava.utilities.Constants.DELIVERY20
import com.trava.utilities.Constants.SUMMER_APP_BASE
import com.trava.utilities.constants.*
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_URL
import com.trava.utilities.webservices.models.AppDetail
import com.trava.utilities.webservices.models.Service
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.security.MessageDigest
import java.util.*

class MainActivity : AppCompatActivity(), MainActivityContract.View {
    private var handler = Handler()
    private var dialogIndeterminate: DialogIndeterminate? = null
    private val presenter = MainActivityPresenter()

    override fun onApiSuccess(price: JSONObject) {
        ConfigPOJO.OMAN_CURRENCY=price.getDouble("AED_OMR")
    }

    override fun onAppLablesSuccess(appLables: Data?) {
        if(!appLables?.app_forun_settings.isNullOrEmpty()){
            ConfigPOJO.appLablesList = appLables?.app_forun_settings ?: ArrayList<AppForunSetting>()
        }

        intent?.handleIntent()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter.attachView(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        when (BASE_URL) {
            "https://apitest.royorides.io/v1/" -> {
                NewRelic.withApplicationToken("AA2784064fe47d14112a015cecd72314ea28b36e53-NRMA").start(this.application)
            }
            "https://api.royorides.io/v1/" -> {
                NewRelic.withApplicationToken("AA472205e1732047cab3352b14e53535a1c70e0fc5-NRMA").start(this.application)
            }
            else -> {
                NewRelic.withApplicationToken("AA4598d9df1a12cb71a9f6d7b479fa8e300e7e724e-NRMA").start(this.application)
            }
        }

        createNotificationChannel()

        var loc = LocaleManager.getLanguage(this)
        val locale = Locale(loc)

        if(locale != null){
            SharedPrefs.get().save(LANGUAGE_CODE, getLanguageId(locale.toString()).toString())
        }else{
            SharedPrefs.get().save(LANGUAGE_CODE, getLanguageId("en"))
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val mChannel = NotificationChannel("1", getString(R.string.requests), NotificationManager.IMPORTANCE_HIGH)
            mChannel.setShowBadge(true)
            mChannel.lightColor = Color.GRAY
            mChannel.enableLights(true)
            mChannel.enableVibration(true)
            mChannel.description = getString(R.string.request_channel_description)
            val attributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            val sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.custom_tone)
            mChannel.setSound(sound, attributes)
            mNotificationManager.createNotificationChannel(mChannel)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

    }

    override fun onResume() {
        super.onResume()

        var loc = LocaleManager.getLanguage(this)
        val locale = Locale(loc)

        if(locale != null){
            SharedPrefs.get().save(LANGUAGE_CODE, getLanguageId(locale.toString()).toString())
        }else{
            SharedPrefs.get().save(LANGUAGE_CODE, getLanguageId("en"))
        }

        if (CheckNetworkConnection.isOnline(this)) {
            if (getString(R.string.app_name) == "Wasila") {
//                presenter.getCurrencyPrice()
            }
            presenter.appSetting()
        } else {
            CheckNetworkConnection.showNetworkError(container)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateSSHKey(context: Context) {
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.getEncoder().encode(md.digest()))
                Log.i("AppLog", "key:$hashKey")
            }
        } catch (e: Exception) {
            Log.e("AppLog", "error:", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
        handler.removeCallbacks(runnable)
    }

    private val runnable = Runnable {
        val proifle = SharedPrefs.with(this@MainActivity).getObject(PROFILE, AppDetail::class.java)
        if (proifle != null) {
            if (getString(R.string.app_name) == "Wasila") {
                if (proifle.user?.phone_code=="+968")
                {
                    ConfigPOJO.currency = "OMR"
                    ConfigPOJO.OMAN_PHONECODE = proifle.user?.phone_code?:""
                }
            }
            SharedPrefs.with(this).save(ACCESS_TOKEN_KEY, intent.getStringExtra("accessToken"))
            ACCESS_TOKEN = intent.getStringExtra("accessToken") ?: ""

            if (Constants.SECRET_DB_KEY == "8969983867fe9a873b1b39d290ffa25c") {
                startActivity(Intent(this@MainActivity, MainMenuActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
            } else {
                startActivity(Intent(this@MainActivity, HomeActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
            }
        } else {
            if (ConfigPOJO.TEMPLATE_CODE == SUMMER_APP_BASE || ConfigPOJO.TEMPLATE_CODE == DELIVERY20) {
                startActivity(Intent(this@MainActivity,
                        SignupActivity::class.java))
            } else if (ConfigPOJO.TEMPLATE_CODE == Constants.MOBY) {
                startActivity(Intent(this@MainActivity,
                        LandingScreen::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
            } else {
                if(ConfigPOJO.settingsResponse?.key_value?.is_enable_business_user ?: "" == "true"){
                    startActivity(Intent(this@MainActivity,
                            RegisterOptionsActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                }else {
                    if (ConfigPOJO.is_facebookLogin == "true") {
                        startActivity(Intent(this@MainActivity,
                                LandingScreen::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    } else {
                        startActivity(Intent(this@MainActivity,
                                WalkthroughActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    }
                }
            }
        }
        finish()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun onProvideAssistContent(outContent: AssistContent?) {
        super.onProvideAssistContent(outContent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // This is just an example, more accurate information should be provided
            outContent?.structuredData = JSONObject()
                    .put("@type", "RideObservation")
                    .put("name", "My last ride")
                    .put("url", "https://royorides.com/app")
                    .toString()
        }
    }

    override fun onApiSuccess(response: SettingItems?) {
        setSettingData(response)
        presenter.getAppLables()
    }

    private fun  setSettingData(response: SettingItems?)
    {
        ConfigPOJO.SECRET_API_KEY = if (response?.key_value?.andriod_google_api ?: "".isNotEmpty() == true) {
            response?.key_value?.andriod_google_api ?: ""
        } else {
            ConfigPOJO.SECRET_API_KEY
        }

        Places.initialize(applicationContext, ConfigPOJO.SECRET_API_KEY)
        if (response?.key_value?.app_template != "") {
            ConfigPOJO.TEMPLATE_CODE = response?.key_value?.app_template?.toInt() ?: 0
        } else {
            ConfigPOJO.TEMPLATE_CODE = 0
        }

        if (response?.key_value?.currency_code == null) {
            ConfigPOJO.currency = "₹"
        } else {
            ConfigPOJO.currency = response?.key_value?.currency_code!!.toString()
        }

        if(response?.key_value?.is_cash_on_Delivery == "true"){
            ConfigPOJO.is_cash_on_Delivery = response?.key_value?.is_cash_on_Delivery
        }
        if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            ConfigPOJO.primary_color = response?.key_value?.Primary_colour.toString().trim()
            ConfigPOJO.secondary_color = response?.key_value?.secondary_colour.toString().trim()
            ConfigPOJO.headerColor = response?.key_value?.header_colour.toString().trim()
            ConfigPOJO.header_txt_colour = response?.key_value?.heder_txt_colour.toString().trim()
            ConfigPOJO.Btn_Text_Colour = ConfigPOJO.Btn_Text_Colour.toString().trim()
            ConfigPOJO.Btn_Colour = response?.key_value?.Primary_colour.toString().trim()
        } else {
            ConfigPOJO.primary_color = response?.key_value?.Primary_colour.toString().trim()
            ConfigPOJO.secondary_color = response?.key_value?.secondary_colour.toString().trim()
            ConfigPOJO.headerColor = response?.key_value?.header_colour.toString().trim()
            ConfigPOJO.header_txt_colour = response?.key_value?.heder_txt_colour.toString().trim()
            ConfigPOJO.Btn_Text_Colour = response?.key_value?.Secondary_Btn_Text_colour.toString().trim()
            ConfigPOJO.Btn_Colour = response?.key_value?.Secondary_Btn_Colour.toString().trim()
            ConfigPOJO.white_color="#ffffff"
        }

        ConfigPOJO.settingsResponse = response
        ConfigPOJO.dynamicBar = response?.dynamicbar

        if (response?.key_value?.is_contactus.equals("true", false)) {
            if (response?.supports != null && response?.supports.isNotEmpty()) {
                ConfigPOJO.support_email = response?.supports!![0].email!!
                ConfigPOJO.support_number = response?.supports!![0].phone!!
            }
        }

        if(response?.key_value?.is_merchant.equals("true",false)){
            ConfigPOJO.is_merchant = response?.key_value?.is_merchant?:""
        }

        if (response?.key_value?.is_omco.equals("true", false)) {
            ConfigPOJO.is_omco = response?.key_value?.is_omco ?: ""
        }

        if (response?.key_value?.is_darkMap.equals("true", false)) {
            ConfigPOJO.is_darkMap = response?.key_value?.is_darkMap ?: ""
        }

        if (response?.key_value?.is_childrenTravelling.equals("true", false)) {
            ConfigPOJO.is_childrenTraveling = response?.key_value?.is_childrenTravelling ?: ""
        }

        if (response?.key_value?.is_gift.equals("true", false)) {
            ConfigPOJO.is_gift = response?.key_value?.is_gift ?: ""
        }

        if (response?.key_value?.is_play_video_after_splash.equals("true", false)) {
            ConfigPOJO.is_play_video_after_splash = response?.key_value?.is_play_video_after_splash ?: ""
            ConfigPOJO.play_video_url = response?.key_value?.play_video_url ?: ""
            ConfigPOJO.play_video_after_splash_images = response?.key_value?.play_video_after_splash_images ?: ""
        }
        ConfigPOJO.admin_base_url = response?.key_value?.admin_base_url ?: ""

        if (response?.key_value?.is_card_payment_enabled.equals("true", false)) {
            ConfigPOJO.is_card_payment_enabled = response?.key_value?.is_card_payment_enabled ?: ""
        }

        if (response?.key_value?.is_google_login.equals("true", false)) {
            ConfigPOJO.is_google_login = response?.key_value?.is_google_login ?: ""
        }

        if (response?.key_value?.is_gender_selection_enabled.equals("true", false)) {
            ConfigPOJO.is_gender_selection_enabled = response?.key_value?.is_gender_selection_enabled ?: ""
        }

        if (response?.key_value?.is_facebookLogin.equals("true", false)) {
            ConfigPOJO.is_facebookLogin = response?.key_value?.is_facebookLogin ?: ""
        }

        if (response?.key_value?.is_countrycheck.equals("true", false)) {
            ConfigPOJO.is_countrycheck = response?.key_value?.is_countrycheck ?: ""
        }

        if (response?.key_value?.is_asap.equals("true", false)) {
            ConfigPOJO.is_asap = response?.key_value?.is_asap ?: ""
        }

        if (response?.key_value?.is_cash_payment_enabled.equals("true", false)) {
            ConfigPOJO.is_cash_payment_enabled = response?.key_value?.is_cash_payment_enabled ?: ""
        }

        if (response?.key_value?.is_water_platform.equals("true", false)) {
            ConfigPOJO.is_water_platform = response?.key_value?.is_water_platform ?:""
        }

        ConfigPOJO.multiple_request = response?.key_value?.multiple_request ?: "1"

        if(ConfigPOJO.is_asap == "true"  && ConfigPOJO.currency == "INR"){
            ConfigPOJO.currency = "₹"
        }

        if(ConfigPOJO.is_asap =="true"){
            ConfigPOJO.search_count = response?.key_value?.search_count  ?: "0"
            ConfigPOJO.distance_search_increment = response?.key_value?.distance_search_increment  ?: "0"
            ConfigPOJO.distance_search_start = response?.key_value?.distance_search_start  ?: "0"
        }

        ConfigPOJO.defaultCoutryCode = response?.key_value?.default_country_code!!.toString().trim().replace("\n", "")
        ConfigPOJO.ISO_CODE = response?.key_value?.iso_code!!.toString().trim().replace("\n", "")
        if (response?.key_value?.minimum_wallet_balance != null)
            ConfigPOJO.minimum_wallet_balance = response?.key_value?.minimum_wallet_balance!!.toString().trim().replace("\n", "")
        if (!response?.key_value?.gateway_unique_id.isNullOrEmpty()) {
            if (response?.key_value?.gateway_unique_id.equals("stripe")) {
                ConfigPOJO.gateway_unique_id = response?.key_value?.gateway_unique_id!!.toString()
                if (response?.key_value?.stripe_public_key != null) {
                    ConfigPOJO.STRIPE_PUBLIC_KEY = response?.key_value?.stripe_public_key!!.toString()
                    ConfigPOJO.STRIPE_SECRET_KEY = response?.key_value?.stripe_secret_key!!.toString()
                }

            } else if (response?.key_value?.gateway_unique_id.equals("conekta")) {
                ConfigPOJO.gateway_unique_id = response?.key_value?.gateway_unique_id!!.toString()
                ConfigPOJO.conekta_api_key = response?.key_value?.conekta_api_key!!.toString()
                ConfigPOJO.conekta_api_version = response?.key_value?.conekta_api_version!!.toString()
                ConfigPOJO.conekta_locale = response?.key_value?.conekta_locale!!.toString()
            }else if(response?.key_value?.gateway_unique_id.equals("razorpay")){
                ConfigPOJO.gateway_unique_id= response?.key_value?.gateway_unique_id
                ConfigPOJO.razorpay_secret_key = response?.key_value?.razorpay_secret_key
                ConfigPOJO.razorpay_private_key = response?.key_value?.razorpay_private_key
            }
            else if(response?.key_value?.gateway_unique_id.toLowerCase(Locale.ENGLISH).equals("thawani"))
            {
                ConfigPOJO.gateway_unique_id= response?.key_value?.gateway_unique_id.toLowerCase(Locale.ENGLISH)
                ConfigPOJO.THAWANI_API_KEY = response?.key_value?.razorpay_secret_key
                ConfigPOJO.THAWANI_PUBLIC_KEY = response?.key_value?.razorpay_private_key
            }
            ConfigPOJO.gateway_unique_id = response?.key_value?.gateway_unique_id!!.toString()
        }

        if (response.services != null) {
            var categoriesList: ArrayList<Service>? = ArrayList()
            for (i in response.services?.indices!!) {
                if (response.services?.get(i)?.category_id == intent.getIntExtra("category_id", 0)) {
                    categoriesList?.add(response.services?.get(i)!!)
                }
            }

            SharedPrefs.with(this).save(SERVICES, Gson().toJson(categoriesList))
        }

        intent?.handleIntent()
    }

    private fun Intent.handleIntent() {
        when (action) {
            // When the action is triggered by a deep-link, Intent.Action_VIEW will be used
            Intent.ACTION_VIEW -> handleDeepLink(data)
            // Otherwise start the app as you would normally do.
            else -> updateDefaultView()
        }
    }



    private fun updateDefaultView() {
        if (intent.extras != null) {
            var data = ""
            if (intent.extras!!.getString("data") != null) {
                val type = JSONObject(intent.extras!!.getString("data"))
                data = type.getString("type")
            }

            if (data == "wallet" || intent.extras!!.get("type") == "wallet") {
                startActivity(Intent(this@MainActivity,
                        UserWalletActivity::class.java)
                        .putExtra("from", "Notification")
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
            } else if (intent.getStringExtra("type") == "chat" || intent.extras!!.get("type") == "chat") {
                startActivity(Intent(this@MainActivity,
                        HomeActivity::class.java)
                        .putExtra("order_id", intent.getStringExtra("order_id"))
                        .putExtra("from_chat", "Notification")
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
            } else {
                if (!intent.getBooleanExtra("hold", true)) {
                    handler.postDelayed(runnable, 0)
                } else {
                    handler.postDelayed(runnable, 500)
                }
            }
        } else {
            if (!intent.getBooleanExtra("hold", true)) {
                handler.postDelayed(runnable, 0)
            } else {
                handler.postDelayed(runnable, 500)
            }
        }
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun apiFailure() {

    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {

        }
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    private fun handleDeepLink(data: Uri?) {
        // path is normally used to indicate which view should be displayed
        // i.e https://fit-actions.firebaseapp.com/start?exerciseType="Running" -> path = "start"
        var actionHandled = true
        when (data?.path) {
            DeepLink.BOOK_RIDE -> {
                val destinationData = data.getQueryParameter(DeepLink.Params.ACTIVITY_TYPE).orEmpty()
                        ?: ""
                var intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("destination", destinationData)
                intent.putExtra("for", "book_ride")
                startActivity(intent)
            }

            DeepLink.OPEN_BOOKINGS -> {
                var intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("destination", "bookings")
                intent.putExtra("for", "bookings")
                startActivity(intent)
            }

            DeepLink.OPEN_ROYO_RIDES -> {
                updateDefaultView()
            }

            else -> {
                Log.e("Intent_handled", "Yes Handled")
                updateDefaultView()
            }
        }
    }
}
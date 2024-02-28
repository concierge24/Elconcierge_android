package com.trava.user.ui.signup.moby.landing

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.trava.user.R
import com.trava.user.databinding.ActivityLandingScreenBinding
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.WebViewActivity
import com.trava.user.ui.signup.moby.resgister_screens.RegisterActivity
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.walkthrough.WelcomeScreenAdapter
import com.trava.user.webservices.moby.response.UserExistResult
import com.trava.utilities.*
import com.trava.utilities.constants.*
import com.trava.utilities.location.LocationProvider
import com.trava.utilities.webservices.models.LoginModel
import kotlinx.android.synthetic.main.activity_landing_screen.*
import kotlinx.android.synthetic.main.fragment_login.*
import org.json.JSONException
import java.util.*
import kotlin.collections.HashMap


class LandingScreen : AppCompatActivity(), UserExistanceConract.View, SocialLoginInteractor.View {
    var binding: ActivityLandingScreenBinding? = null
    var callbackManager: CallbackManager? = null
    private val EMAIL = "email"
    private var count = 0;

    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    var userExistPresenter = UserExistPresenter()
    var socialLoginPresenter = SocialLoginPresenter()
    private lateinit var dialogIndeterminate: DialogIndeterminate
    var socialId: String? = null
    var loginType: String? = null
    private lateinit var locationProvider: LocationProvider
    var isAlready = false
    var tokenList = ArrayList<String>()
    var screenType = "Login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_landing_screen)
        userExistPresenter?.attachView(this) // attach presenter
        socialLoginPresenter?.attachView(this) // attach presenter
        dialogIndeterminate = DialogIndeterminate(this)
        binding!!.color = ConfigPOJO.Companion
        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)
        dots_indicator.selectedDotColor = Color.parseColor(ConfigPOJO.secondary_color)

        binding?.loginButton?.setReadPermissions(listOf(EMAIL))

        // facebook auth initialize
        callbackManager = CallbackManager.Factory.create()

        // google auth initialize
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken("515752350463-nsnvkh6nik8sr4ssee58khq1gm7i42hl.apps.googleusercontent.com")
                .requestIdToken("233030763639-evncur765fnubufas5oml10vusi4t23p.apps.googleusercontent.com")
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        googleLogout()
        setupAdapter()
        setListener()

        if(ConfigPOJO.TEMPLATE_CODE == Constants.TRAVE_APP_BASE  && ConfigPOJO.is_facebookLogin == "true"){
            binding!!.googleBtn.visibility = View.GONE
            binding!!.instBtn.visibility = View.GONE
            binding!!.title.text = if(ConfigPOJO.settingsResponse?.key_value?.is_app_forum_setting=="true")
                AppUtils.getLabelText("login_title")
            else
                resources.getString(R.string.login_to)+" "+resources.getString(R.string.app_name)

            if (Constants.SECRET_DB_KEY == "97b883a388f65760a09e6571e3d16825") {
                binding!!.googleBtn.visibility = View.VISIBLE
            }
        }

        if (Constants.SECRET_DB_KEY =="e03beef2da96672a58fddb43e7468881" || Constants.SECRET_DB_KEY =="0035690c91fbcffda0bb1df570e8cb98")
        {
            binding!!.fbBtn.visibility = View.GONE
        }

        if(ConfigPOJO.is_google_login == "true"){
            binding!!.googleBtn.visibility = View.VISIBLE
        }

        locationProvider = LocationProvider.CurrentLocationBuilder(this, this).build()
        locationProvider.getLastKnownLocation(OnSuccessListener {
            SharedPrefs.with(this).save(Constants.LATITUDE, it?.latitude ?: 0.0)
            SharedPrefs.with(this).save(Constants.LONGITUDE, it?.longitude ?: 0.0)
        })

        binding?.helpIv?.visibility = View.GONE

        if(Constants.SECRET_DB_KEY =="d190fc6d825399e5276673ee8467d415af6dc774ffe7005ea59b3d5b6a2f0314"  || Constants.SECRET_DB_KEY =="d190fc6d825399e5276673ee8467d415cebf52a7d89e2a2f532b4d8d1268fbc7") {
            binding?.cooprateLoginButton?.visibility = View.VISIBLE
            binding?.termsRl?.visibility = View.GONE
            binding?.dropDownIv?.visibility = View.GONE
        }
    }

    fun googleLogout() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this) {
                    // Update your UI here
                }
    }

    private fun setListener() {
        binding?.cooprateLoginButton?.visibility = View.GONE
        binding?.tvLoginSignup?.setOnClickListener {
//            if (binding?.tvLoginSignup?.text.toString().toLowerCase().contains("login")) {
//                binding?.text?.setText(resources.getString(R.string.don_t_have_an_account))
//                binding?.tvLoginSignup?.setText(resources.getString(R.string.sign_up))
//                binding?.dropDownIv?.visibility = GONE
//                binding?.termsRl?.visibility = GONE
//                binding?.title?.setText(resources.getString(R.string.login_to)+" "+resources.getString(R.string.app_name))
//                binding?.phoneEmailTv?.setText(resources.getString(R.string.login_with_phone_or_email))
//            } else {
//                binding?.text?.setText(resources.getString(R.string.already_have_an_account))
//                binding?.tvLoginSignup?.setText(resources.getString(R.string.login))
//                binding?.dropDownIv?.visibility = VISIBLE
//                binding?.termsRl?.visibility = VISIBLE
//                binding?.title?.setText(resources.getString(R.string.sign_up_for)+" "+resources.getString(R.string.app_name))
//                binding?.phoneEmailTv?.setText(resources.getString(R.string.sign_up_with_phone_or_email))
//            }AppUtils.getLabelText("login_title")

            if(screenType == "Login"){
                screenType = "Signup"
                binding?.text?.setText(resources.getString(R.string.already_have_an_account))
                binding?.tvLoginSignup?.setText(resources.getString(R.string.login))
                binding?.dropDownIv?.visibility = VISIBLE
                binding?.termsRl?.visibility = VISIBLE
                binding?.title?.setText(resources.getString(R.string.sign_up_for)+" "+resources.getString(R.string.app_name))
                binding?.phoneEmailTv?.setText(resources.getString(R.string.sign_up_with_phone_or_email))
                if(Constants.SECRET_DB_KEY =="d190fc6d825399e5276673ee8467d415af6dc774ffe7005ea59b3d5b6a2f0314" || Constants.SECRET_DB_KEY =="d190fc6d825399e5276673ee8467d415cebf52a7d89e2a2f532b4d8d1268fbc7") {
                    binding?.cooprateLoginButton?.visibility = View.GONE
                    binding?.termsRl?.visibility = View.GONE
                    binding?.dropDownIv?.visibility = View.GONE
                }
            }else{
                screenType = "Login"
                binding?.text?.setText(resources.getString(R.string.don_t_have_an_account))
                binding?.tvLoginSignup?.setText(resources.getString(R.string.sign_up))
                binding?.dropDownIv?.visibility = GONE
                binding?.termsRl?.visibility = GONE
                binding?.title?.text = if(ConfigPOJO.settingsResponse?.key_value?.is_app_forum_setting=="true")
                    AppUtils.getLabelText("login_title")
                else
                    resources.getString(R.string.login_to)+" "+resources.getString(R.string.app_name)

                binding?.phoneEmailTv?.setText(resources.getString(R.string.login_with_phone_or_email))
                if(Constants.SECRET_DB_KEY =="d190fc6d825399e5276673ee8467d415af6dc774ffe7005ea59b3d5b6a2f0314"|| Constants.SECRET_DB_KEY =="d190fc6d825399e5276673ee8467d415cebf52a7d89e2a2f532b4d8d1268fbc7"){
                    binding?.cooprateLoginButton?.visibility = View.VISIBLE
                    binding?.termsRl?.visibility = View.GONE
                    binding?.dropDownIv?.visibility = View.GONE
                }
            }
        }

        binding?.termsTv?.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra(TITLE, getString(R.string.terms_and_conditions_caps))
            if(ConfigPOJO.is_omco == "true"){
                intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}/terms_conditions/user/")
            }else {
                intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}/terms_conditions/user/")
            }
            startActivity(intent)
        }

        binding?.privacyTv?.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra(TITLE, getString(R.string.privacy_policy))
            if(ConfigPOJO.is_omco == "true"){
                intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}/privacy_policy/user/")
            }else {
                intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}/privacy_policy/user/")
            }
            startActivity(intent)
        }

        binding?.closeIv?.setOnClickListener {
            finishAffinity()
        }

        binding?.fbBtn?.setOnClickListener {
            binding!!.loginButton.performClick()
        }

        binding?.loginButton?.setOnClickListener {
            binding?.loginButton?.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {

                    Log.d("MainActivity", "Facebook token: " + loginResult!!.accessToken.token)
                    val accessToken = AccessToken.getCurrentAccessToken()

                    getUserProfile(accessToken)
                }

                override fun onCancel() { // App code
                }

                override fun onError(exception: FacebookException) { // App code
                }
            })
        }

        binding?.googleBtn?.setOnClickListener {
            googleSigninSignup()
        }

        binding?.phoneEmailBtn?.setOnClickListener {
            if (screenType == "Login") {
                openRegisterScreen("login")
            } else {
                openRegisterScreen("register")
            }
        }

        binding?.cooprateLoginButton?.setOnClickListener {
            openRegisterScreen("cooprate")
        }

        binding?.instBtn?.setOnClickListener {
            if (screenType == "Login") {
                openRegisterScreen("inst_login")
            } else {
                openRegisterScreen("inst_signup")
            }
        }
    }

    private fun googleSigninSignup() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(
                signInIntent, RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resulrCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resulrCode, data)
        callbackManager!!.onActivityResult(requestCode, resulrCode, data)
                if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

//    override fun onActivityResult(
//            requestCode: Int,
//            resultCode: Int,
//            data: Intent?) {
//        callbackManager?.onActivityResult(requestCode, resultCode, data)
//        super.onActivityResult(requestCode, resultCode, data)
//        callbackManager?.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == RC_SIGN_IN) {
//            val task =
//                    GoogleSignIn.getSignedInAccountFromIntent(data)
//            handleSignInResult(task)
//        }
//
//    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(
                    ApiException::class.java
            )
            // Signed in successfully
            val googleId = account?.id ?: ""
            Log.i("Google ID", googleId)

            val googleFirstName = account?.givenName ?: ""
            Log.i("Google First Name", googleFirstName)

            val googleLastName = account?.familyName ?: ""
            Log.i("Google Last Name", googleLastName)

            val googleEmail = account?.email ?: ""
            Log.i("Google Email", googleEmail)

            val googleProfilePicURL = account?.photoUrl.toString()
            Log.i("Google Profile Pic URL", googleProfilePicURL)

            val googleIdToken = account?.idToken ?: ""
            Log.i("Google ID Token", googleIdToken)
            socialId=googleId
            loginType="Gmail"
            verifyUserExistance(socialId!!, loginType!!)
            /*startActivity(Intent(this, RegisterActivity::class.java)
                    .putExtra("screenType", "phone")
                    .putExtra("social_key", googleId)
                    .putExtra("signup_as", "Gmail")
            )*/


        } catch (e: ApiException) {
            // Sign in was unsuccessful
            Log.e(
                    "failed code=", e.statusCode.toString()
            )
        }
    }

    private fun openRegisterScreen(screetype: String) {
        startActivity(Intent(this, RegisterActivity::class.java).putExtra("screenType", screetype))
    }

    private fun setupAdapter() {
        val adapter = WelcomeScreenAdapter(supportFragmentManager)
        viewPager.adapter = adapter
        dots_indicator.setViewPager(viewPager)
    }

    private fun getUserProfile(currentAccessToken: AccessToken?) {
        val request = GraphRequest.newMeRequest(
                currentAccessToken) { `object`, response ->
            Log.d("TAG", `object`.toString())
            try {
                if (isAlready) {
//                val first_name = `object`.getString("first_name")
//                val last_name = `object`.getString("last_name")
//                val email = `object`.getString("email")
                    socialId = `object`?.getString("id")
                    Log.d("TOKEN>>", socialId?:"")
//                val image_url = "https://graph.facebook.com/$id/picture?type=normal"
                    loginType = "Facebook"

                    verifyUserExistance(socialId!!, loginType!!)
                    LoginManager.getInstance().logOut();

                } else {
                    isAlready = true
                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val parameters = Bundle()
        parameters.putString("fields", "first_name,last_name,email,id")
        request.parameters = parameters
        request.executeAsync()
    }

    private fun verifyUserExistance(socialKey: String, loginType: String) {
        if (CheckNetworkConnection.isOnline(this)) {
            userExistPresenter?.checkUserExist(socialKey, loginType)
            isAlready = false
        } else {
            CheckNetworkConnection.showNetworkError(rootView)
        }

    }

    override fun onApiSuccess(response: UserExistResult?) {

        if (response?.getAppDetail()?.getUserExists()!!) {
            //hit social login api

            locationProvider.getLastKnownLocation(OnSuccessListener {
                val map = HashMap<String, String>()

                SharedPrefs.with(this).save(Constants.LATITUDE, it?.latitude ?: 0.0)
                SharedPrefs.with(this).save(Constants.LONGITUDE, it?.longitude
                        ?: 0.0)
                map["timezone"] = TimeZone.getDefault().id
                map["latitude"] = it?.latitude?.toString() ?: "0.0"
                map["longitude"] = it?.longitude?.toString() ?: "0.0"
                map["login_as"] = loginType.toString()
                map["device_type"] = "Android"
                map["social_key"] = socialId.toString()

                if (CheckNetworkConnection.isOnline(this)) {
                    socialLoginPresenter.socialLogin(map)
                } else {
                    CheckNetworkConnection.showNetworkError(binding?.root!!)
                }
            })
        } else {
            startActivity(Intent(this, RegisterActivity::class.java)
                    .putExtra("screenType", "phone")
                    .putExtra("social_key", socialId)
                    .putExtra("signup_as", loginType)
            )
        }
    }

    override fun onSocialLoginApiSuccess(response: LoginModel?, map: Map<String, String>) {
        ACCESS_TOKEN = response?.AppDetail?.access_token ?: ""

        SharedPrefs.with(this).save(PROFILE, response?.AppDetail)
        SharedPrefs.with(this).save(SERVICES, Gson().toJson(response?.services))
        finishAffinity()
        startActivity(Intent(this, HomeActivity::class.java))

        Log.d("TOKEN>>", response?.AppDetail?.access_token?:"")

        SharedPrefs.with(this).save(ACCESS_TOKEN_KEY, response?.AppDetail?.access_token)

    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate.show(isLoading)
    }

    override fun apiFailure() {
        binding?.root?.showSnack(getString(R.string.sww_error))
    }

    override fun handleApiError(code: Int?, error: String?) {
        binding?.root?.showSnack(error.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        userExistPresenter?.detachView()
        socialLoginPresenter?.detachView()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }
}

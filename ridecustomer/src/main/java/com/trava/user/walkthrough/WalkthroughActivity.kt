package com.trava.user.walkthrough

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.trava.user.R
import com.trava.user.ui.home.WebViewActivity
import com.trava.user.ui.signup.SignupActivity
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.utilities.Constants
import com.trava.utilities.Constants.CORSA
import com.trava.utilities.Constants.GOMOVE_BASE
import com.trava.utilities.LocaleManager
import com.trava.utilities.constants.TITLE
import com.trava.utilities.constants.URL_TO_LOAD
import com.trava.utilities.showSnack
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_POLICIES
import kotlinx.android.synthetic.main.activity_walkthrough.*
import org.json.JSONException
import java.util.*

class WalkthroughActivity : AppCompatActivity() {
    var callbackManager: CallbackManager? = null
    var isAlready = false
    var socialId: String? = null
    var loginType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walkthrough)

        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)
        dots_indicator.selectedDotColor = Color.parseColor(ConfigPOJO.primary_color)

        if (ConfigPOJO.settingsResponse?.key_value?.iso_code?.length == 2) {
            countryCodePicker.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.toLowerCase())
        } else {
            countryCodePicker.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.substring(0, ConfigPOJO.settingsResponse?.key_value?.iso_code?.length?.minus(1)
                    ?: 0)?.toLowerCase())
        }

        cvCountry.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        tvPhone.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        ll_facebook.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        ll_phone.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)

        callbackManager = CallbackManager.Factory.create()

        if (ConfigPOJO.is_facebookLogin == "true") {
            rlBottomfacebook.visibility = View.VISIBLE
            rlBottom.visibility = View.GONE
        } else if (ConfigPOJO.is_water_platform == "true") {
            rlBottom1.visibility = View.GONE
            tvLanguage.visibility = View.VISIBLE
            rlBottom.visibility = View.GONE
            rlBottomfacebook.visibility = View.GONE
            countryCodePicker.showFlag(true)
            tvLanguage.setTextColor(Color.parseColor(ConfigPOJO.primary_color))
            tvLanguage.background = StaticFunction.changeBorderTextColor(ConfigPOJO.transparent, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        } else {
            rlBottom.visibility = View.VISIBLE
            rlBottomfacebook.visibility = View.GONE
        }

        setupAdapter()
        setListener()
        if (getString(R.string.app_name) == "Wasila") {
            ch_box.visibility = View.GONE
        }

        if (ConfigPOJO.TEMPLATE_CODE==GOMOVE_BASE) {
            ll_terms.visibility = View.GONE
        }

        if (ConfigPOJO.TEMPLATE_CODE!=GOMOVE_BASE)
        {
            tv_privacy.setLinkTextColor(Color.parseColor(ConfigPOJO.secondary_color))
            tv_privacy.makeLinks(Pair(getString(R.string.terms_and_conditions), View.OnClickListener {
                val intent = Intent(this, WebViewActivity::class.java)
                intent.putExtra(TITLE, getString(R.string.terms_and_conditions_caps))
                if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
                    intent.putExtra(URL_TO_LOAD, "${BASE_POLICIES}legal/terms/")// gomove
                }else if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
                    intent.putExtra(URL_TO_LOAD, "${BASE_POLICIES}/terms_conditions/user/")// gomove
                }else if(ConfigPOJO.is_merchant == "true"){
                    intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}/terms_conditions/user/")
                } else {
                    intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}/terms_conditions/user/")
                }
                startActivity(intent)
            }),Pair(getString(R.string.privacy_policy), View.OnClickListener {
                val intent = Intent(this, WebViewActivity::class.java)
                intent.putExtra(TITLE, getString(R.string.privacy))
                if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
                    intent.putExtra(URL_TO_LOAD, "${BASE_POLICIES}legal/privacy/")// gomove
                }else if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
                    intent.putExtra(URL_TO_LOAD, "${BASE_POLICIES}/privacy_policy/user/")// gomove
                }else if(ConfigPOJO.is_merchant == "true"){
                    intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}/privacy_policy/user/")
                } else {
                    intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}/privacy_policy/user/")
                }
                startActivity(intent)
            }))
        }
    }

    fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
        val spannableString = SpannableString(this.text)
        for (link in links) {
            val clickableSpan = object : ClickableSpan() {

                override fun updateDrawState(textPaint: TextPaint) {
                    // use this to change the link color
                    textPaint.color = textPaint.linkColor
                    // toggle below value to enable/disable
                    // the underline shown below the clickable text
                    textPaint.isUnderlineText = true
                }

                override fun onClick(view: View) {
                    Selection.setSelection((view as TextView).text as Spannable, 0)
                    view.invalidate()
                    link.second.onClick(view)
                }
            }
            val startIndexOfLink = this.text.toString().indexOf(link.first)
            spannableString.setSpan(clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        this.movementMethod = LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    private fun setListener() {
        rlPhoneLayout.setOnClickListener {
            if (getString(R.string.app_name) == "Wasila" || ConfigPOJO.TEMPLATE_CODE==GOMOVE_BASE || ConfigPOJO.TEMPLATE_CODE== CORSA) {
                startActivity(Intent(this, SignupActivity::class.java))
                overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
            } else {
                if (ch_box.isChecked) {
                    startActivity(Intent(this, SignupActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
                } else {
                    root_view.showSnack(getString(R.string.terms_policy))
                }
            }
        }

        ll_facebook.setOnClickListener {
            login_button.performClick()
        }

        login_button.setOnClickListener {
            login_button.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
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

        ll_phone.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
        }
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
                    Log.d("TOKEN>>", socialId ?: "")
//                val image_url = "https://graph.facebook.com/$id/picture?type=normal"
                    loginType = "Facebook"


                    // verifyUserExistance(socialId!!, loginType!!)
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

    override fun onActivityResult(requestCode: Int, resulrCode: Int, data: Intent?) {
        callbackManager!!.onActivityResult(requestCode, resulrCode, data)
        super.onActivityResult(requestCode, resulrCode, data)
        callbackManager!!.onActivityResult(requestCode, resulrCode, data)
    }

    private fun setupAdapter() {
        val adapter = WalkthroughAdapter(supportFragmentManager)
        viewPager.adapter = adapter
        dots_indicator.setViewPager(viewPager)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }
}
package com.trava.user.ui.signup.login

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnSuccessListener
import com.trava.user.MainActivity
import com.trava.user.R
import com.trava.user.databinding.FragmentLoginBinding
import com.trava.user.databinding.FragmentLoginSummerBinding
import com.trava.user.databinding.FragmentLoginWaterBinding
import com.trava.user.ui.home.WebViewActivity
import com.trava.user.ui.signup.verifytop.OtpFragment
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.SendOtp
import com.trava.user.webservices.models.appsettings.LanguageSets
import com.trava.utilities.*
import com.trava.utilities.Constants.DELIVERY20
import com.trava.utilities.Constants.EAGLE
import com.trava.utilities.Constants.GOMOVE_BASE
import com.trava.utilities.Constants.MOVER
import com.trava.utilities.Constants.SECRET_DB_KEY
import com.trava.utilities.Constants.SUMMER_APP_BASE
import com.trava.utilities.Constants.TRAVE_APP_BASE
import com.trava.utilities.constants.*
import com.trava.utilities.location.LocationProvider
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_POLICIES
import kotlinx.android.synthetic.main.fragment_login.cb_select
import kotlinx.android.synthetic.main.fragment_login.countryCodePicker
import kotlinx.android.synthetic.main.fragment_login.etPhone
import kotlinx.android.synthetic.main.fragment_login.ivBack
import kotlinx.android.synthetic.main.fragment_login.iv_coutry
import kotlinx.android.synthetic.main.fragment_login.rootView
import kotlinx.android.synthetic.main.fragment_login.tvProceed
import kotlinx.android.synthetic.main.fragment_login_summer.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class LoginFragment : Fragment(), View.OnClickListener, LoginContract.View {

    private lateinit var locationProvider: LocationProvider
    lateinit var loginFragmentLoginBinding: FragmentLoginBinding
    lateinit var loginFragmentLoginSummerBinding: FragmentLoginSummerBinding
    lateinit var loginFragmentLoginWaterBinding: FragmentLoginWaterBinding
    private val presenter = LoginPresenter()
    private lateinit var dialogIndeterminate: DialogIndeterminate
    private lateinit var cvCountry: LinearLayout
    private var validNumber = false
    var layoutView: View? = null

    private var languagesAdapter: ArrayAdapter<String>? = null
    private var languages = arrayListOf<String>()
    private var LanguageSets = ArrayList<LanguageSets>()
    private var selectedLanguageID = 0
    private var selectedLanguagePopsition = 0
    private var selectedLanguageName = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val ccpPrefs = context.getSharedPreferences("CCP_PREF_FILE", Context.MODE_PRIVATE)
        ccpPrefs?.edit()?.putString("signupCCP", "")?.apply()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        loginFragmentLoginBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);
        loginFragmentLoginSummerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_login_summer, container, false);
        loginFragmentLoginWaterBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_login_water, container, false);
        loginFragmentLoginBinding.color = ConfigPOJO.Companion
        loginFragmentLoginSummerBinding.color = ConfigPOJO.Companion
        loginFragmentLoginWaterBinding.color = ConfigPOJO.Companion

        if (ConfigPOJO.TEMPLATE_CODE == TRAVE_APP_BASE || ConfigPOJO.TEMPLATE_CODE == GOMOVE_BASE || ConfigPOJO.TEMPLATE_CODE == MOVER || ConfigPOJO.TEMPLATE_CODE == EAGLE || ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            if (ConfigPOJO.is_water_platform == "true") {
                layoutView = loginFragmentLoginWaterBinding.root
            } else {
                layoutView = loginFragmentLoginBinding.root
            }
        } else if (ConfigPOJO.TEMPLATE_CODE == SUMMER_APP_BASE || ConfigPOJO.TEMPLATE_CODE == DELIVERY20) {
            layoutView = loginFragmentLoginSummerBinding.root
        }

        if (ConfigPOJO.TEMPLATE_CODE == GOMOVE_BASE) {
            loginFragmentLoginBinding.tvTerms.text = getString(R.string.i_agree_to_the) + " " + getString(R.string.terms_and_conditions)
            loginFragmentLoginBinding.llTerms.visibility = View.VISIBLE
            setSpan()
        }

        if (ConfigPOJO.TEMPLATE_CODE == SUMMER_APP_BASE || ConfigPOJO.TEMPLATE_CODE == DELIVERY20) {
            loginFragmentLoginSummerBinding.tvEnterYourNumber.text = getString(R.string.login_to) + "" + getString(R.string.app_name)
            loginFragmentLoginSummerBinding.llCheckTerm.visibility = View.VISIBLE
            setSpanForAnother()
        }

        if (ConfigPOJO.is_water_platform == "true") {
            loginFragmentLoginBinding.countryCodePicker.showFlag(true)
            loginFragmentLoginSummerBinding.countryCodePicker.showFlag(true)
        }
        return layoutView
    }

    private fun setSpanForAnother() {
        val ss = SpannableString("By checking this box. I agree to Terms and conditions")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                val intent = Intent(activity, WebViewActivity::class.java)
                intent.putExtra(TITLE, getString(R.string.terms_and_conditions_caps))
                intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}terms_conditions/user/")
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        ss.setSpan(clickableSpan, 33, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        loginFragmentLoginSummerBinding.tvTermss.text = ss
        loginFragmentLoginSummerBinding.tvTermss.movementMethod = LinkMovementMethod.getInstance()
        loginFragmentLoginSummerBinding.tvTermss.setLinkTextColor(Color.parseColor(ConfigPOJO.secondary_color))
    }

    private fun setSpan() {
        val span = SpannableString(loginFragmentLoginBinding.tvTerms.text)
        span.setSpan(ForegroundColorSpan(ContextCompat.getColor(loginFragmentLoginBinding.tvTerms.context, R.color.black)),
                loginFragmentLoginBinding.tvTerms.text.indexOf(getString(R.string.terms_and_conditions)), loginFragmentLoginBinding.tvTerms.text.length
                , Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        span.setSpan(UnderlineSpan(), loginFragmentLoginBinding.tvTerms.text.indexOf(getString(R.string.terms_and_conditions)),
                loginFragmentLoginBinding.tvTerms.text.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        loginFragmentLoginBinding.tvTerms.text = span
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this) // attach presenter
        dialogIndeterminate = DialogIndeterminate(activity)
        cvCountry = view.findViewById(R.id.cvCountry)

        if (ConfigPOJO.settingsResponse?.key_value?.iso_code?.length == 2) {
            countryCodePicker.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.toLowerCase())
        } else {
            countryCodePicker.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.substring(0, ConfigPOJO.settingsResponse?.key_value?.iso_code?.length?.minus(1)
                    ?: 0)?.toLowerCase())
        }

        countryCodePicker.setNumberAutoFormattingEnabled(false)
        countryCodePicker.registerCarrierNumberEditText(etPhone) // registers the EditText with the country code picker
        countryCodePicker.setPhoneNumberValidityChangeListener { isValidNumber ->
            validNumber = isValidNumber
        }
        iv_coutry.setOnClickListener {
            countryCodePicker.show()
            countryCodePicker.setNumberAutoFormattingEnabled(false)
            countryCodePicker.registerCarrierNumberEditText(etPhone)
        }
        setListeners()
        locationProvider = LocationProvider.CurrentLocationBuilder(activity, this).build()
        locationProvider.getLastKnownLocation(OnSuccessListener {
            SharedPrefs.with(activity).save(Constants.LATITUDE, it?.latitude ?: 0.0)
            SharedPrefs.with(activity).save(Constants.LONGITUDE, it?.longitude ?: 0.0)
        })

        if (SECRET_DB_KEY == "eecf5a33e8575b1a860cc17dd778ea6f" ||
                SECRET_DB_KEY == "456049b71e28127ccd109b3fa9392fdb") {
            etPhone.setText("0")
            Selection.setSelection(etPhone.text, etPhone.text.length)
            etPhone.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    // TODO Auto-generated method stub
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    // TODO Auto-generated method stub
                }

                override fun afterTextChanged(s: Editable) {
                    if (!s.toString().startsWith("0")) {
                        etPhone.setText("0")
                        Selection.setSelection(etPhone.text, etPhone.text.length)
                    }
                }
            })
        }

        if (ConfigPOJO.TEMPLATE_CODE == SUMMER_APP_BASE || ConfigPOJO.TEMPLATE_CODE == DELIVERY20) {
            val spannable = SpannableString("By checking this box, I agree to terms and conditions")
            spannable.setSpan(ForegroundColorSpan(Color.parseColor(ConfigPOJO.secondary_color)), 33, spannable.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            loginFragmentLoginBinding.tvTerms.text = spannable
            tvProceed_fab.setOnClickListener(this)
            tvProceed_fab.background.mutate().setTint(Color.parseColor(ConfigPOJO.primary_color))
        } else {
            loginFragmentLoginBinding.etPhone.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            loginFragmentLoginBinding.cvCountry.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            loginFragmentLoginWaterBinding.etPhone.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            loginFragmentLoginWaterBinding.cvCountry.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        }

        if (ConfigPOJO.settingsResponse?.key_value?.is_language_selection_on_splash == "true") {
            presenter.getLanguages()
        } else {
            rl_language.visibility = View.GONE
        }
    }

    private fun setListeners() {
        ivBack.setOnClickListener(this)
        tvProceed.setOnClickListener(this)
        loginFragmentLoginBinding.tvTerms.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                activity?.onBackPressed()
            }
            R.id.tvTerms -> {
                val intent = Intent(activity, WebViewActivity::class.java)
                intent.putExtra(TITLE, getString(R.string.terms_and_conditions_caps))
                if (ConfigPOJO.TEMPLATE_CODE == GOMOVE_BASE) {
                    intent.putExtra(URL_TO_LOAD, "${BASE_POLICIES}/legal/terms/")// gomove
                }else if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
                    intent.putExtra(URL_TO_LOAD, "${BASE_POLICIES}/terms_conditions/user/")// gomove
                } else {
                    intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}terms_conditions/user/")
                }
                startActivity(intent)
            }

            R.id.tvProceed, R.id.tvProceed_fab -> {
                if (isValidationOk()) {
                    if (ConfigPOJO.TEMPLATE_CODE == SUMMER_APP_BASE || ConfigPOJO.TEMPLATE_CODE == DELIVERY20) {
                        if (!cb_select.isChecked) {
                            tvProceed?.showSnack(getString(R.string.by_creating_account_you_agree))
                            return
                        }
                    }
                    if (ConfigPOJO.TEMPLATE_CODE == GOMOVE_BASE) {
                        if (!loginFragmentLoginBinding.cbSelect.isChecked) {
                            tvProceed?.showSnack(getString(R.string.by_creating_account_you_agree))
                            return
                        }
                    }

                    /*if (!validNumber)
                    {
                        tvProceed?.showSnack("Please enter valid phone number")
                        return
                    }*/

                    locationProvider.getLastKnownLocation(OnSuccessListener {
                        SharedPrefs.with(activity).save(Constants.LATITUDE, it?.latitude ?: 0.0)
                        SharedPrefs.with(activity).save(Constants.LONGITUDE, it?.longitude ?: 0.0)
                        val map = HashMap<String, String>()
                        map["phone_code"] = countryCodePicker.selectedCountryCodeWithPlus
                        map["phone_number"] = etPhone.text.toString().replace(Regex("[^0-9]"), "")
                        map["timezone"] = TimeZone.getDefault().id
                        map["latitude"] = it?.latitude?.toString() ?: "0.0"
                        map["longitude"] = it?.longitude?.toString() ?: "0.0"
                        map["signup_as"] = "PhoneNo"
                        map["device_type"] = "Android"
                        if (CheckNetworkConnection.isOnline(activity)) {
                            presenter.sendOtpApiCall(map)
                        } else {
                            CheckNetworkConnection.showNetworkError(rootView)
                        }
                    })
                }
            }
        }
    }

    /*Checks for the validations*/
    private fun isValidationOk(): Boolean {
        return when {
            etPhone.text.toString().trim().isEmpty() -> {
                tvProceed?.showSnack(getString(R.string.phone_validation_message))// Invalid phone number
                false
            }
            etPhone.text.toString().length<5 -> {
                tvProceed?.showSnack(getString(R.string.phone_valid_validation_message))// Invalid phone number
                false
            }
            else -> true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationProvider.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        locationProvider.onActivityResult(requestCode, resultCode, data)
    }

    /* Api success callback for send OTP api*/
    override fun onApiSuccess(response: SendOtp?, map: Map<String, String>) {
        SharedPrefs.with(activity).save(ACCESS_TOKEN_KEY, response?.access_token)
        Log.d("TOKEN>>", response?.access_token ?: "")
        ACCESS_TOKEN = response?.access_token ?: ""
        val fragment = OtpFragment()
        val bundle = Bundle()
        bundle.putString("phone_number", countryCodePicker.selectedCountryCodeWithPlus + "-" + etPhone.text.toString().replace(Regex("[^0-9]"), ""))
        bundle.putString("otp", response?.otp.toString())
        bundle.putString("access_token", response?.access_token)
        bundle.putSerializable("map", map as HashMap)
        fragment.arguments = bundle
        fragmentManager?.let { it1 ->
            Utils.addFragment(it1, fragment, R.id.container, OtpFragment::class.java.simpleName)
        }
    }

    override fun onSettingsApiSuccess() {
        if (LocaleManager.getLanguage(activity) != selectedLanguageName) {
            SharedPrefs.get().save(LANGUAGE_CODE, selectedLanguageID.toString())
            setNewLocale(selectedLanguageName, true)
        }
    }

    override fun onSettingsApiSuccess(list: ArrayList<LanguageSets>?) {
        LanguageSets.clear()
        LanguageSets.addAll(list!!)
        setLanguageAdapter()
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate.show(isLoading)
    }

    override fun apiFailure() {
        tvProceed.showSnack(getString(R.string.sww_error))
    }

    override fun handleApiError(code: Int?, error: String?) {
        tvProceed.showSnack(error.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    private fun setLanguageAdapter() {
        languages.clear()
        if (LanguageSets.size > 0) {
            for (i in LanguageSets.indices) {
                languages.add(LanguageSets.get(i).language_name)
                if (LanguageSets.get(i).language_code?.toLowerCase() == LocaleManager.getLanguage(activity!!)) {
                    selectedLanguagePopsition = i
                    selectedLanguageID = LanguageSets.get(i).language_id
                }
            }
        }

        languagesAdapter = ArrayAdapter(activity!!, R.layout.layout_spinner_languages, languages)
        languagesAdapter?.setDropDownViewResource(R.layout.item_languages)
        spinner.adapter = languagesAdapter
        spinner.setSelection(selectedLanguagePopsition)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                (parent?.getChildAt(0) as TextView).setTextColor(Color.parseColor(ConfigPOJO.primary_color))

                selectedLanguageID = LanguageSets.get(position).language_id
                selectedLanguageName = LanguageSets.get(position).language_code.toLowerCase()

                if (LocaleManager.getLanguage(activity) != selectedLanguageName) {
                    SharedPrefs.get().save(PREFS_LANGUAGE_ID, position + 1)
                    SharedPrefs.get().save(LANGUAGE_CHANGED, true)

                    if (LocaleManager.getLanguage(activity) != selectedLanguageName) {
                        SharedPrefs.get().save(LANGUAGE_CODE, selectedLanguageID.toString())
                        setNewLocale(selectedLanguageName, true)
                    }

//                    presenter.updateSettingsApiCall(selectedLanguageID.toString(), "1")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun setNewLocale(language: String, restartProcess: Boolean): Boolean {
        LocaleManager.setNewLocale(activity, language)
        val intent = Intent(activity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("hold", false)
        startActivity(intent)
        activity?.finish()
        return true
    }
}
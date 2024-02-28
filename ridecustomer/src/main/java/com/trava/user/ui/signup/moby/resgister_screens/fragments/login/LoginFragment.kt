package com.trava.user.ui.signup.moby.resgister_screens.fragments.login


import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.facebook.login.LoginManager
import com.google.android.gms.tasks.OnSuccessListener
import com.google.gson.Gson
import com.trava.user.MainActivity

import com.trava.user.R
import com.trava.user.databinding.FragmentLoginScreenLayoutBinding
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.signup.login.LoginContract
import com.trava.user.ui.signup.login.LoginPresenter
import com.trava.user.ui.signup.verifytop.OtpFragment
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.SendOtp
import com.trava.user.webservices.models.appsettings.LanguageSets
import com.trava.utilities.*
import com.trava.utilities.constants.*
import com.trava.utilities.location.LocationProvider
import com.trava.utilities.webservices.models.LoginModel
import kotlinx.android.synthetic.main.fragment_login_screen_layout.*
import kotlinx.android.synthetic.main.fragment_login_screen_layout.countryCodePicker
import kotlinx.android.synthetic.main.fragment_login_screen_layout.etPhone
import kotlinx.android.synthetic.main.fragment_login_screen_layout.rl_language
import kotlinx.android.synthetic.main.fragment_login_screen_layout.spinner
import kotlinx.android.synthetic.main.fragment_login_summer.*
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() ,View.OnClickListener, LoginContract.View,EmailLoginContractor.View {

    var binding : FragmentLoginScreenLayoutBinding ?= null
    private val presenter = LoginPresenter()
    private val emailLoginPresenter = EmailLoginPresenter()
    private lateinit var dialogIndeterminate: DialogIndeterminate
    private lateinit var cvCountry: LinearLayout
    private lateinit var locationProvider: LocationProvider

    private var languagesAdapter: ArrayAdapter<String>? = null
    private var languages = arrayListOf<String>()
    private var LanguageSets = ArrayList<LanguageSets>()
    private var selectedLanguageID = 0
    private var selectedLanguagePopsition = 0
    private var selectedLanguageName = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_login_screen_layout, container, false)

        binding?.color = ConfigPOJO.Companion

        return  binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this) // attach presenter
        emailLoginPresenter.attachView(this) // attach presenter
        dialogIndeterminate = DialogIndeterminate(activity)
        locationProvider = LocationProvider.CurrentLocationBuilder(activity, this).build()
        locationProvider.getLastKnownLocation(OnSuccessListener {
            SharedPrefs.with(activity).save(Constants.LATITUDE, it?.latitude ?: 0.0)
            SharedPrefs.with(activity).save(Constants.LONGITUDE, it?.longitude ?: 0.0)
        })
        binding?.tvProceedFab?.background?.mutate()?.setTint(Color.parseColor(ConfigPOJO.primary_color))


//        binding?.tvProceedFab.

        countryCodePicker.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.substring(0, ConfigPOJO.settingsResponse?.key_value?.iso_code?.length?.minus(1)?:0)?.toLowerCase())

        if (ConfigPOJO.settingsResponse?.key_value?.iso_code?.length == 2) {
            countryCodePicker.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.toLowerCase())
        } else {
            countryCodePicker.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.substring(0, ConfigPOJO.settingsResponse?.key_value?.iso_code?.length?.minus(1)
                    ?: 0)?.toLowerCase())
        }
        countryCodePicker.setNumberAutoFormattingEnabled(false)
        countryCodePicker.registerCarrierNumberEditText(etPhone) // registers the EditText with the country code picker

        setListeners()

        if(Constants.SECRET_DB_KEY == "6ae915e1698d88c4e7dda839852fa5fd"){
            binding?.rlForgot?.visibility = View.GONE
        }

        binding?.tvTitle?.text = getString(R.string.login_to)+" "+getString(R.string.app_name)
//        binding!!.switchScreenTv.performClick()
        binding?.rlForgot?.visibility = View.GONE

        if(arguments!= null){
            if(arguments?.getString("from") == "cooprate"){
                binding?.logTv?.visibility = View.GONE
                binding?.switchScreenTv?.visibility = View.GONE
                binding!!.switchScreenTv.performClick()
            }
        }

        if (ConfigPOJO.settingsResponse?.key_value?.is_language_selection_on_splash == "true") {
            presenter.getLanguages()
        } else {
            rl_language.visibility = View.INVISIBLE
        }
    }

    private fun setListeners() {
        binding!!.ivBack.setOnClickListener(this)
        binding!!.switchScreenTv.setOnClickListener(this)
        binding!!.tvProceedFab.setOnClickListener(this)
        binding!!.forgotTv.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.ivBack -> {
                activity?.onBackPressed()
            }

            R.id.switch_screen_tv -> {
                if(switch_screen_tv.text.toString().toLowerCase().contains("phone")|| switch_screen_tv.text.toString().toLowerCase().contains("teléfono") || switch_screen_tv.text.toString().trim().equals(resources.getString(R.string.phoneNo))){
                    binding?.rlPhoneLayout?.visibility = View.VISIBLE
                    binding?.rlEmail?.visibility = View.GONE
                    switch_screen_tv.setText(resources.getString(R.string.email_id))
                }else{
                    binding?.rlPhoneLayout?.visibility = View.GONE
                    binding?.rlEmail?.visibility = View.VISIBLE
                    switch_screen_tv.setText(resources.getString(R.string.phoneNo))
                }
            }

            R.id.tvProceed_fab -> {
                if(switch_screen_tv.text.toString().toLowerCase().contains("phone")|| switch_screen_tv.text.toString().toLowerCase().contains("teléfono")|| switch_screen_tv.text.toString().trim().equals(resources.getString(R.string.phoneNo))){
                    // email login
                    if(!isValidEmail(binding?.emailEdt?.text.toString())){
                        binding?.root?.showSnack(R.string.email_valid_validation_message)
                    }
                    else if(binding?.passEdt?.text.toString().length<6 || binding?.passEdt?.text.toString().isNullOrEmpty()){
                        binding?.root?.showSnack(getString(R.string.pass_not_empty))
                    }/*else if(binding?.conPassEdt?.text.toString().length<7 || binding?.conPassEdt?.text.toString().isNullOrEmpty()){
                        binding?.root?.showSnack(getString(R.string.pass_not_empty))
                    }else if(!(binding?.passEdt?.text.toString().equals(binding?.conPassEdt?.text.toString()))){
                        binding?.root?.showSnack(getString(R.string.pass_mismatch))
                    }*/else{
                        if (CheckNetworkConnection.isOnline(activity)) {
                            val map = HashMap<String, String>()

                            map["timezone"] = TimeZone.getDefault().id
                            map["latitude"] = "0.0"
                            map["longitude"] = "0.0"
                            map["email"] = binding?.emailEdt?.text.toString()
                            map["password"] = binding?.passEdt?.text.toString()
                            map["device_type"] = "Android"
                            map["login_as"] = "Email"


                            if (CheckNetworkConnection.isOnline(activity)) {
                                emailLoginPresenter.emailLogin(map)
                            } else {
                                CheckNetworkConnection.showNetworkError(binding?.root!!)
                            }

                        } else {
                            CheckNetworkConnection.showNetworkError(binding?.root!!)
                        }
                    }
                }else {
                    if (isValidationOk()) {
                        locationProvider.getLastKnownLocation(OnSuccessListener {
                            SharedPrefs.with(activity).save(Constants.LATITUDE, it?.latitude ?: 0.0)
                            SharedPrefs.with(activity).save(Constants.LONGITUDE, it?.longitude
                                    ?: 0.0)
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
                                CheckNetworkConnection.showNetworkError(binding?.root!!)
                            }
                        })
                    }
                }
            }


        }
    }


    fun isValidEmail(target: CharSequence): Boolean {
        val check: Boolean
        val p: Pattern
        val m: Matcher

        val EMAIL_STRING = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

        p = Pattern.compile(EMAIL_STRING)

        m = p.matcher(target)
        check = m.matches()

        if (!check) {
            return false
        }
        return check
    }

    /*Checks for the validations*/
    private fun isValidationOk(): Boolean {
        return when {
            etPhone.text.toString().trim().isEmpty() -> {
                binding?.root?.showSnack(getString(R.string.phone_validation_message))// Invalid phone number
                false
            }
            else -> true
        }
    }



    /* Api success callback for send OTP api*/
    override fun onApiSuccess(response: SendOtp?, map: Map<String, String>) {
        SharedPrefs.with(activity).save(ACCESS_TOKEN_KEY, response?.access_token)
        Log.d("TOKEN>>2",response?.access_token?:"")
        ACCESS_TOKEN = response?.access_token ?: ""
        val fragment = OtpFragment()
        val bundle = Bundle()
        bundle.putString("phone_number", countryCodePicker.selectedCountryCodeWithPlus + "-" + etPhone.text.toString().replace(Regex("[^0-9]"), ""))
        bundle.putString("otp", response?.otp.toString())
        bundle.putString("access_token", response?.access_token)
        bundle.putSerializable("map", map as HashMap)
        fragment.arguments = bundle
        fragmentManager?.let { it1 ->
            Utils.replaceFragment(it1, fragment, R.id.container, OtpFragment::class.java.simpleName)
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

    override fun onApiSuccess(response: LoginModel?, map: Map<String, String>) {

        //verify otp

            /* User is already registered. Navigation into the home screen*/
            ACCESS_TOKEN = response?.AppDetail?.access_token ?: ""
            SharedPrefs.with(activity).save(ACCESS_TOKEN_KEY, response?.AppDetail?.access_token)
            Log.d("TOKEN>>1",response?.AppDetail?.access_token?:"")

            SharedPrefs.with(activity).save(PROFILE, response?.AppDetail)
            SharedPrefs.with(activity).save(SERVICES, Gson().toJson(response?.services))
//            val data = SharedPrefs.get().getString(IS_CATEGORY_SELECTED,"")
//            if(data.isNotEmpty()){
            activity?.finishAffinity()
            startActivity(Intent(activity, HomeActivity::class.java))

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

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
        emailLoginPresenter.detachView()
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

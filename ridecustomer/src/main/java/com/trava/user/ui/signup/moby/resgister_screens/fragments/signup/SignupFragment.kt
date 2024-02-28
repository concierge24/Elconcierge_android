package com.trava.user.ui.signup.moby.resgister_screens.fragments.signup


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.OnSuccessListener
import com.hbb20.CountryCodePicker

import com.trava.user.R
import com.trava.user.databinding.FragmentPhoneLayoutBinding
import com.trava.user.databinding.FragmentSignupBinding
import com.trava.user.ui.home.WebViewActivity
import com.trava.user.ui.signup.login.LoginContract
import com.trava.user.ui.signup.login.LoginPresenter
import com.trava.user.ui.signup.verifytop.OtpFragment
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.SendOtp
import com.trava.user.webservices.models.appsettings.LanguageSets
import com.trava.utilities.*
import com.trava.utilities.constants.ACCESS_TOKEN
import com.trava.utilities.constants.ACCESS_TOKEN_KEY
import com.trava.utilities.constants.TITLE
import com.trava.utilities.constants.URL_TO_LOAD
import com.trava.utilities.location.LocationProvider
import kotlinx.android.synthetic.main.fragment_signup.*
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */
class SignupFragment : Fragment(), View.OnClickListener, LoginContract.View {
    override fun onSettingsApiSuccess(list: ArrayList<LanguageSets>?) {
        TODO("Not yet implemented")
    }

    override fun onSettingsApiSuccess() {
        TODO("Not yet implemented")
    }

    var binding: FragmentSignupBinding? = null
    var binding2: FragmentPhoneLayoutBinding? = null
    private val presenter = LoginPresenter()
    private lateinit var dialogIndeterminate: DialogIndeterminate
    private lateinit var cvCountry: LinearLayout
    private lateinit var locationProvider: LocationProvider
    var isPhonetab: Boolean = true;
    var socialKey: String? = null
    var signup_as: String? = "Normal"
    var vieww: View? = null
    private var rootView : LinearLayout ?= null
    private var etPhone : EditText?= null
    private var countryCodePicker:CountryCodePicker ?= null

    companion object {
        fun newInstance(social_key: String, signup_as: String): SignupFragment {
            val args = Bundle()
            args.putString("social_key", social_key)
            args.putString("signup_as", signup_as)
            val fragment = SignupFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding2 = DataBindingUtil.inflate(inflater, R.layout.fragment_phone_layout, container, false)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false)


        if (!arguments?.getString("social_key").isNullOrEmpty()) {
            socialKey = arguments?.getString("social_key")
            signup_as = arguments?.getString("signup_as")
            vieww = binding2?.root
        } else {
            vieww = binding?.root
        }

        rootView = vieww?.findViewById(R.id.rootView)
        etPhone = vieww?.findViewById(R.id.etPhone)
        countryCodePicker = vieww?.findViewById(R.id.countryCodePicker)

        binding?.color = ConfigPOJO.Companion
        binding2?.color = ConfigPOJO.Companion


        return  vieww
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this) // attach presenter
        dialogIndeterminate = DialogIndeterminate(activity)
        binding?.countryCodePicker?.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.substring(0, ConfigPOJO.settingsResponse?.key_value?.iso_code?.length?.minus(1)
                ?: 0)?.toLowerCase())
        binding?.countryCodePicker?.setNumberAutoFormattingEnabled(false)
        binding?.countryCodePicker?.registerCarrierNumberEditText(etPhone) // registers the EditText with the country code picker

        binding?.ccountryCodePicker?.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.substring(0, ConfigPOJO.settingsResponse?.key_value?.iso_code?.length?.minus(1)
                ?: 0)?.toLowerCase())
        binding?.ccountryCodePicker?.setNumberAutoFormattingEnabled(false)
        binding?.ccountryCodePicker?.registerCarrierNumberEditText(etPhone) // registers the EditText with the country code picker

        binding2?.countryCodePicker?.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.substring(0, ConfigPOJO.settingsResponse?.key_value?.iso_code?.length?.minus(1)
                ?: 0)?.toLowerCase())
        binding2?.countryCodePicker?.setNumberAutoFormattingEnabled(false)
        binding2?.countryCodePicker?.registerCarrierNumberEditText(etPhone) // registers the EditText with the country code picker

        if (ConfigPOJO.settingsResponse?.key_value?.iso_code?.length == 2) {
            binding?.countryCodePicker?.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.toLowerCase())
        } else {
            binding?.countryCodePicker?.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.substring(0, ConfigPOJO.settingsResponse?.key_value?.iso_code?.length?.minus(1)
                    ?: 0)?.toLowerCase())
        }

        if (ConfigPOJO.settingsResponse?.key_value?.iso_code?.length == 2) {
            binding?.ccountryCodePicker?.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.toLowerCase())
        } else {
            binding?.ccountryCodePicker?.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.substring(0, ConfigPOJO.settingsResponse?.key_value?.iso_code?.length?.minus(1)
                    ?: 0)?.toLowerCase())
        }

        if (ConfigPOJO.settingsResponse?.key_value?.iso_code?.length == 2) {
            binding2?.countryCodePicker?.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.toLowerCase())
        } else {
            binding2?.countryCodePicker?.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.substring(0, ConfigPOJO.settingsResponse?.key_value?.iso_code?.length?.minus(1)
                    ?: 0)?.toLowerCase())
        }

        locationProvider = LocationProvider.CurrentLocationBuilder(activity, this).build()
        locationProvider.getLastKnownLocation(OnSuccessListener {
            SharedPrefs.with(activity).save(Constants.LATITUDE, it?.latitude ?: 0.0)
            SharedPrefs.with(activity).save(Constants.LONGITUDE, it?.longitude ?: 0.0)
        })

        binding?.tvProceedFab?.background?.mutate()?.setTint(Color.parseColor(ConfigPOJO.primary_color))
        binding2?.tvProceedFab?.background?.mutate()?.setTint(Color.parseColor(ConfigPOJO.primary_color))

        if (Constants.SECRET_DB_KEY == "d190fc6d825399e5276673ee8467d415af6dc774ffe7005ea59b3d5b6a2f0314" || Constants.SECRET_DB_KEY =="d190fc6d825399e5276673ee8467d415cebf52a7d89e2a2f532b4d8d1268fbc7") {
            binding?.cbTermsConditions?.visibility = View.VISIBLE
        }
        setListeners()
    }

    private fun setListeners() {
        binding!!.ivBack.setOnClickListener(this)
        binding!!.phoneTab.setOnClickListener(this)
        binding!!.emailTab.setOnClickListener(this)
        binding!!.tvProceedFab.setOnClickListener(this)

        binding2!!.ivBack.setOnClickListener(this)
        binding2!!.tvProceedFab.setOnClickListener(this)

        binding?.termsTv?.setOnClickListener {
            val intent = Intent(activity!!, WebViewActivity::class.java)
            intent.putExtra(TITLE, getString(R.string.terms_and_conditions_caps))
            if (ConfigPOJO.is_omco == "true") {
                intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}/terms_conditions/user/")
            }else {
                intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}/terms_conditions/user/")
            }
            startActivity(intent)
        }

        binding?.privacyTv?.setOnClickListener {
            val intent = Intent(activity!!, WebViewActivity::class.java)
            intent.putExtra(TITLE, getString(R.string.privacy_policy))
            if (ConfigPOJO.is_omco == "true") {
                intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}/privacy_policy/user/")
            }else {
                intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}/privacy_policy/user/")
            }
            startActivity(intent)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                activity?.onBackPressed()
            }

            R.id.phone_tab -> {
                isPhonetab = true
                binding?.phoneTab?.setBackgroundDrawable(resources.getDrawable(R.drawable.new_button_border_bg_drawable))
                binding?.emailTab?.setBackgroundDrawable(resources.getDrawable(R.drawable.grey_tab_background))
                binding?.phoneTab?.setTextColor(resources.getColor(R.color.black))
                binding?.emailTab?.setTextColor(resources.getColor(R.color.grey_5e))
                binding?.rlPhoneLayout?.visibility = VISIBLE
                binding?.rlEmail?.visibility = GONE
            }

            R.id.email_tab -> {
                isPhonetab = false
                binding?.phoneTab?.setBackgroundDrawable(resources.getDrawable(R.drawable.grey_tab_background))
                binding?.emailTab?.setBackgroundDrawable(resources.getDrawable(R.drawable.new_button_border_bg_drawable))
                binding?.emailTab?.setTextColor(resources.getColor(R.color.black))
                binding?.phoneTab?.setTextColor(resources.getColor(R.color.grey_5e))
                binding?.rlPhoneLayout?.visibility = GONE
                binding?.rlEmail?.visibility = VISIBLE
            }

            R.id.tvProceed_fab -> {
                if (!TextUtils.isEmpty(socialKey)) {
                    if (isValidationOk()) {
                        locationProvider.getLastKnownLocation(OnSuccessListener {
                            val map = HashMap<String, String>()
                            if (!TextUtils.isEmpty(socialKey)) {
                                SharedPrefs.with(activity).save(Constants.LATITUDE, it?.latitude
                                        ?: 0.0)
                                SharedPrefs.with(activity).save(Constants.LONGITUDE, it?.longitude
                                        ?: 0.0)
                                map["phone_code"] = binding2?.countryCodePicker?.selectedCountryCodeWithPlus.toString()
                                map["phone_number"] = binding2?.etPhone?.text.toString().replace(Regex("[^0-9]"), "")
                                map["timezone"] = TimeZone.getDefault().id
                                map["latitude"] = it?.latitude?.toString() ?: "0.0"
                                map["longitude"] = it?.longitude?.toString() ?: "0.0"
                                map["signup_as"] = signup_as.toString()
                                map["device_type"] = "Android"
                                map["social_key"] = socialKey.toString()
                            } else {
                                SharedPrefs.with(activity).save(Constants.LATITUDE, it?.latitude
                                        ?: 0.0)
                                SharedPrefs.with(activity).save(Constants.LONGITUDE, it?.longitude
                                        ?: 0.0)
                                map["phone_code"] = binding2?.countryCodePicker?.selectedCountryCodeWithPlus.toString()
                                map["phone_number"] = binding2?.etPhone?.text.toString().replace(Regex("[^0-9]"), "")
                                map["timezone"] = TimeZone.getDefault().id
                                map["latitude"] = it?.latitude?.toString() ?: "0.0"
                                map["longitude"] = it?.longitude?.toString() ?: "0.0"
                                map["signup_as"] = binding2.toString()
                                map["device_type"] = "Android"
                            }

                            if (CheckNetworkConnection.isOnline(activity)) {
                                presenter.sendOtpApiCall(map)
                            } else {
                                CheckNetworkConnection.showNetworkError(rootView!!)
                            }
                        })
                    }
                } else {
                    if (isPhonetab) {
                        if (isValidationOk()) {
                            if (Constants.SECRET_DB_KEY == "d190fc6d825399e5276673ee8467d415af6dc774ffe7005ea59b3d5b6a2f0314" || Constants.SECRET_DB_KEY =="d190fc6d825399e5276673ee8467d415cebf52a7d89e2a2f532b4d8d1268fbc7") {
                                if (binding?.cbTermsConditions?.isChecked == false) {
                                    binding?.cbTermsConditions?.showSnack(getString(R.string.terms_policy))
                                    return
                                }
                            }
                            locationProvider.getLastKnownLocation(OnSuccessListener {
                                val map = HashMap<String, String>()

                                if (!TextUtils.isEmpty(socialKey)) {
                                    COUNTRY_CODE = binding?.countryCodePicker?.selectedCountryCodeWithPlus.toString()
                                    LOGIN_TYPE = "Phone"
                                    SharedPrefs.with(activity).save(Constants.LATITUDE, it?.latitude
                                            ?: 0.0)
                                    SharedPrefs.with(activity).save(Constants.LONGITUDE, it?.longitude
                                            ?: 0.0)
                                    map["phone_code"] = binding?.countryCodePicker?.selectedCountryCodeWithPlus.toString()
                                    map["phone_number"] = binding?.etPhone?.text.toString().replace(Regex("[^0-9]"), "")
                                    map["timezone"] = TimeZone.getDefault().id
                                    map["latitude"] = it?.latitude?.toString() ?: "0.0"
                                    map["longitude"] = it?.longitude?.toString() ?: "0.0"
                                    map["signup_as"] = "PhoneNo"
                                    map["device_type"] = "Android"
                                } else {
                                    COUNTRY_CODE = binding?.countryCodePicker?.selectedCountryCodeWithPlus.toString()
                                    SharedPrefs.with(activity).save(Constants.LATITUDE, it?.latitude
                                            ?: 0.0)
                                    SharedPrefs.with(activity).save(Constants.LONGITUDE, it?.longitude
                                            ?: 0.0)
                                    LOGIN_TYPE = "Phone"
                                    map["phone_code"] = binding?.countryCodePicker?.selectedCountryCodeWithPlus.toString()
                                    map["phone_number"] = binding?.etPhone?.text.toString().replace(Regex("[^0-9]"), "")
                                    map["timezone"] = TimeZone.getDefault().id
                                    map["latitude"] = it?.latitude?.toString() ?: "0.0"
                                    map["longitude"] = it?.longitude?.toString() ?: "0.0"
                                    map["signup_as"] = "Normal"
                                    map["device_type"] = "Android"
                                }

                                if (CheckNetworkConnection.isOnline(activity)) {
                                    presenter.sendOtpApiCall(map)
                                } else {
                                    CheckNetworkConnection.showNetworkError(rootView!!)
                                }
                            })
                        }
                    } else {
                        if (isEmailValidationOk()) {
                            MOBILE_NUMBER = binding?.ettPhone?.text.toString().replace(Regex("[^0-9]"), "")
                            LOGIN_TYPE = "Email"
                            EMAIL_ADDRESS = binding?.emailEdt?.text.toString()
                            COUNTRY_CODE = binding?.ccountryCodePicker?.selectedCountryCodeWithPlus.toString()

                            if (Constants.SECRET_DB_KEY == "d190fc6d825399e5276673ee8467d415af6dc774ffe7005ea59b3d5b6a2f0314" || Constants.SECRET_DB_KEY =="d190fc6d825399e5276673ee8467d415cebf52a7d89e2a2f532b4d8d1268fbc7") {
                                if (binding?.cbTermsConditions?.isChecked == false) {
                                    binding?.cbTermsConditions?.showSnack(getString(R.string.terms_policy))
                                    return
                                }
                            }

                            locationProvider.getLastKnownLocation(OnSuccessListener {
                                val map = HashMap<String, String>()
                                SharedPrefs.with(activity).save(Constants.LATITUDE, it?.latitude
                                        ?: 0.0)
                                SharedPrefs.with(activity).save(Constants.LONGITUDE, it?.longitude
                                        ?: 0.0)
                                map["phone_code"] = binding?.ccountryCodePicker?.selectedCountryCodeWithPlus.toString()
                                map["phone_number"] = binding?.ettPhone?.text.toString().replace(Regex("[^0-9]"), "")
                                map["timezone"] = TimeZone.getDefault().id
                                map["latitude"] = it?.latitude?.toString() ?: "0.0"
                                map["longitude"] = it?.longitude?.toString() ?: "0.0"
                                map["signup_as"] = "Email"
                                map["email"] = binding?.emailEdt?.text.toString()
                                map["password"] = binding?.passEdt?.text.toString()
                                map["device_type"] = "Android"
                                if (CheckNetworkConnection.isOnline(activity)) {
                                    presenter.sendOtpApiCall(map)
                                } else {
                                    CheckNetworkConnection.showNetworkError(rootView!!)
                                }
                            })
                        }
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
            etPhone?.text.toString().trim().isEmpty() -> {
                rootView?.showSnack(getString(R.string.phone_validation_message))// Invalid phone number
                false
            }
            else -> true
        }
    }


    /*Checks for the validations*/
    private fun isEmailValidationOk(): Boolean {
        return when {
            !(isValidEmail(binding?.emailEdt?.text.toString())) -> {
                rootView?.showSnack(R.string.email_valid_validation_message)
                return false
            }
            (binding?.passEdt?.text.toString().length < 6 || binding?.passEdt?.text.toString().isNullOrEmpty()) -> {
                rootView?.showSnack(getString(R.string.pass_not_empty))
                return false
            }
            (binding?.conPassEdt?.text.toString().length < 6 || binding?.conPassEdt?.text.toString().isNullOrEmpty()) -> {
                rootView?.showSnack(getString(R.string.pass_not_empty))
                return false
            }
            (!(binding?.passEdt?.text.toString().equals(binding?.conPassEdt?.text.toString()))) -> {
                rootView?.showSnack(getString(R.string.pass_mismatch))
                return false
            }
            binding?.ettPhone?.text.toString().trim().isEmpty() -> {
                rootView?.showSnack(getString(R.string.phone_validation_message))// Invalid phone number
                false
            }
            else -> true

        }
    }

    /* Api success callback for send OTP api*/
    override fun onApiSuccess(response: SendOtp?, map: Map<String, String>) {
        SharedPrefs.with(activity).save(ACCESS_TOKEN_KEY, response?.access_token)
        Log.d("TOKEN>>", response?.access_token ?: "")
        ACCESS_TOKEN = response?.access_token ?: ""
        val fragment = OtpFragment()
        val bundle = Bundle()
        if (!arguments?.getString("social_key").isNullOrEmpty()) {
            bundle.putString("phone_number", binding2?.countryCodePicker?.selectedCountryCodeWithPlus.toString() + "-" + etPhone?.text.toString().replace(Regex("[^0-9]"), ""))
        }
        else
        {
            if(etPhone?.text.toString().trim().isNotEmpty()){
                bundle.putString("phone_number", countryCodePicker?.selectedCountryCodeWithPlus.toString() + "-" + etPhone?.text.toString().replace(Regex("[^0-9]"), ""))
            }else{
                bundle.putString("phone_number", binding?.ccountryCodePicker?.selectedCountryCodeWithPlus.toString() + "-" + binding?.ettPhone?.text.toString().replace(Regex("[^0-9]"), ""))
            }
        }

        bundle.putString("otp", response?.otp.toString())
        bundle.putString("access_token", response?.access_token)
        bundle.putSerializable("map", map as HashMap)
        fragment.arguments = bundle
        fragmentManager?.let { it1 ->
            Utils.replaceFragment(it1, fragment, R.id.container, OtpFragment::class.java.simpleName)
        }
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate.show(isLoading)
    }

    override fun apiFailure() {
        rootView?.showSnack(getString(R.string.sww_error))
    }

    override fun handleApiError(code: Int?, error: String?) {
        rootView?.showSnack(error.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }
}

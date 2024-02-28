package com.trava.user.ui.signup.verifytop

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.trava.user.R
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.signup.entername.EnterNameFragment
import com.trava.user.webservices.models.SendOtp
import com.trava.utilities.*
import com.trava.utilities.webservices.models.LoginModel
import com.google.gson.Gson
import com.trava.user.MainActivity
import com.trava.user.databinding.FragmentLoginSummerBinding
import com.trava.user.databinding.FragmentOtpBinding
import com.trava.user.databinding.FragmentOtpSummerBinding
import com.trava.user.ui.home.MainMenuActivity
import com.trava.utilities.constants.*
import com.trava.user.utils.ConfigPOJO
import com.trava.utilities.Constants.CORSA
import com.trava.utilities.Constants.SECRET_DB_KEY
import kotlinx.android.synthetic.main.fragment_otp.*
import kotlinx.android.synthetic.main.fragment_otp.ivBack
import kotlinx.android.synthetic.main.fragment_otp.pinView
import kotlinx.android.synthetic.main.fragment_otp.rootView
import kotlinx.android.synthetic.main.fragment_otp.tvResend
import kotlinx.android.synthetic.main.fragment_otp_summer.*
import java.util.*

/**
 * Fragment to let user enters the sent otp and redirects into the app if already registered
 * or redirects to the next sign up process if user is not already registered
 * after successful otp verification.
 * */

class OtpFragment : Fragment(), VerifyOtpContract.View{
    private val presenter = VerifyOtpPresenter()
    private lateinit var dialogIndeterminate: DialogIndeterminate
    private lateinit var tvPhone:TextView
    private lateinit var tvOtpVerify:TextView
    private lateinit var tvResend:TextView
    private lateinit var tvRResend:TextView
    private lateinit var tvVerify:TextView
    private lateinit var rootView:RelativeLayout
    lateinit var otpBinding:FragmentOtpBinding
    lateinit var otpBindingSummer: FragmentOtpSummerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        otpBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_otp, container, false);
        otpBindingSummer = DataBindingUtil.inflate(inflater, R.layout.fragment_otp_summer, container, false);
        otpBinding.color = ConfigPOJO.Companion
        otpBindingSummer.color = ConfigPOJO.Companion

        var view = if (ConfigPOJO.TEMPLATE_CODE == Constants.TRAVE_APP_BASE ||
                ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE ||ConfigPOJO.TEMPLATE_CODE == Constants.EAGLE ||
                ConfigPOJO.TEMPLATE_CODE == Constants.MOVER || ConfigPOJO.TEMPLATE_CODE == CORSA ) {

            if (SECRET_DB_KEY=="f068ea614d6e726084d6660cdfc4dc14242c4f98380b537e89446cd49b0022f7"||
                    SECRET_DB_KEY=="0035690c91fbcffda0bb1df570e8cb98"||
                    SECRET_DB_KEY=="e03beef2da96672a58fddb43e7468881"||
                    SECRET_DB_KEY=="f068ea614d6e726084d6660cdfc4dc14bb88ffcd9d48b45b6cd0b34d5a3554e5"||
                    SECRET_DB_KEY == "6ae915e1698d88c4e7dda839852fa5fd"|| SECRET_DB_KEY == "fdd4d886d691305c703e8274bbd49ec217a41b4a1bc16322f1345c000a1987f5"
                    || SECRET_DB_KEY == "a3f699d8c406daaf828f2d2c891eac15110024532f77f082ad972842c8516d60")
            {
                otpBindingSummer.root
            }
            else
            {
                otpBinding.root
            }
        } else{
            otpBindingSummer.root
        }

        if(ConfigPOJO.TEMPLATE_CODE == Constants.NO_TEMPLATE) {
            startActivity(Intent(activity, MainActivity::class.java))
            activity?.finish()
            Toast.makeText(activity, R.string.session_expired_please_login_again, Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        if(ConfigPOJO.TEMPLATE_CODE == Constants.CORSA){
            ll_option1.visibility = View.VISIBLE
            ll_option2.visibility = View.GONE
        }

        tvPhone=view.findViewById(R.id.tvPhone)
        tvOtpVerify=view.findViewById(R.id.tvOtpVerify)
        tvVerify=view.findViewById(R.id.tvVerify)
        rootView=view.findViewById(R.id.rootView)
        tvResend=view.findViewById(R.id.tvResend)

        dialogIndeterminate = DialogIndeterminate(activity)

        if(SECRET_DB_KEY != "6ae915e1698d88c4e7dda839852fa5fd"){
            tvResend.setTextColor(Color.parseColor(ConfigPOJO.secondary_color))
        }

        otpBinding?.tvRResend.setTextColor(Color.parseColor(ConfigPOJO.secondary_color))
        if (ConfigPOJO.TEMPLATE_CODE== Constants.SUMMER_APP_BASE || ConfigPOJO.TEMPLATE_CODE== Constants.DELIVERY20
                || SECRET_DB_KEY=="0035690c91fbcffda0bb1df570e8cb98"
                || SECRET_DB_KEY=="e03beef2da96672a58fddb43e7468881"
                || SECRET_DB_KEY=="f068ea614d6e726084d6660cdfc4dc14242c4f98380b537e89446cd49b0022f7"
                || SECRET_DB_KEY=="f068ea614d6e726084d6660cdfc4dc14bb88ffcd9d48b45b6cd0b34d5a3554e5"
                || SECRET_DB_KEY == "fdd4d886d691305c703e8274bbd49ec217a41b4a1bc16322f1345c000a1987f5"
                || SECRET_DB_KEY == "a3f699d8c406daaf828f2d2c891eac15110024532f77f082ad972842c8516d60"
                || SECRET_DB_KEY=="f068ea614d6e726084d6660cdfc4dc14bb88ffcd9d48b45b6cd0b34d5a3554e5")
        {
            tvVerify_fab.setOnClickListener {
                if (et_pinView.text.toString().length < 6) {
                    it.showSnack(getString(R.string.otp_validation_message)) // User enters invalid otp
                } else {
                    val map = HashMap<String, String>()
                    map["otp"] = et_pinView.text.toString()
                    if (CheckNetworkConnection.isOnline(activity)) {
                        presenter.verifyOtp(map) //  Api call for otp verification
                    } else {
                        CheckNetworkConnection.showNetworkError(rootView)
                    }
                }
            }
            tvVerify_fab.background.mutate().setTint(Color.parseColor(ConfigPOJO.primary_color))
            et_pinView.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    /* Do nothing*/
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    /* Do nothing*/
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 6) {
                        /* hides the keyboard automatically after successfully entering 4 digits otp */
                        et_pinView.hideKeyboard()
                    }
                }
            })
            if (SECRET_DB_KEY=="f068ea614d6e726084d6660cdfc4dc14242c4f98380b537e89446cd49b0022f7"||
                    SECRET_DB_KEY=="f068ea614d6e726084d6660cdfc4dc14bb88ffcd9d48b45b6cd0b34d5a3554e5")
            {
                didnot_tv.setText(getString(R.string.didnt_recieve_code))
                tvResend.setText(getString(R.string.resend_code))
                tvPhone.text = String.format(Locale.US, "%s %s", getString(R.string.send_code), arguments?.getString("phone_number"))
            }
            else
            {
                tvPhone.text = String.format(Locale.US, "%s %s", getString(R.string.enter_the_otp), arguments?.getString("phone_number"))
            }
        }
        else if(ConfigPOJO.TEMPLATE_CODE== Constants.MOBY)
        {
            tvVerify_fab.setOnClickListener {
                if (et_pinView.text.toString().length < 6) {
                    it.showSnack(getString(R.string.otp_validation_message)) // User enters invalid otp
                } else {
                    val map = HashMap<String, String>()
                    map["otp"] = et_pinView.text.toString()
                    if (CheckNetworkConnection.isOnline(activity)) {
                        presenter.verifyOtp(map) //  Api call for otp verification
                    } else {
                        CheckNetworkConnection.showNetworkError(rootView)
                    }
                }
            }

            tvVerify_fab.background.mutate().setTint(Color.parseColor(ConfigPOJO.primary_color))
            didnot_tv.setText(getString(R.string.didnt_recieve_code))
            tvResend.setText(getString(R.string.resend_code))
            et_pinView.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    /* Do nothing*/
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    /* Do nothing*/
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 6) {
                        /* hides the keyboard automatically after successfully entering 4 digits otp */
                        et_pinView.hideKeyboard()
                    }
                }
            })
            tvPhone.text = String.format(Locale.US, "%s %s", getString(R.string.send_code), arguments?.getString("phone_number"))
            tvOtpVerify.text = String.format(Locale.US, "%s", getString(R.string.verification))
        }else if(ConfigPOJO.TEMPLATE_CODE == Constants.TRAVE_APP_BASE  &&  Constants.SECRET_DB_KEY == "6ae915e1698d88c4e7dda839852fa5fd"){
            tvVerify_fab.setOnClickListener {
                if (et_pinView.text.toString().length < 6) {
                    it.showSnack(getString(R.string.otp_validation_message)) // User enters invalid otp
                } else {
                    val map = HashMap<String, String>()
                    map["otp"] = et_pinView.text.toString()
                    if (CheckNetworkConnection.isOnline(activity)) {
                        presenter.verifyOtp(map) //  Api call for otp verification
                    } else {
                        CheckNetworkConnection.showNetworkError(rootView)
                    }
                }
            }
            tvVerify_fab.background.mutate().setTint(Color.parseColor(ConfigPOJO.primary_color))
            et_pinView.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    /* Do nothing*/
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    /* Do nothing*/
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 6) {
                        /* hides the keyboard automatically after successfully entering 4 digits otp */
                        et_pinView.hideKeyboard()
                    }
                }
            })
            tvPhone.text = String.format(Locale.US, "%s %s", getString(R.string.send_code), arguments?.getString("phone_number"))
            tvOtpVerify.text = String.format(Locale.US, "%s", getString(R.string.verification))
        } else {
            pinView.setLineColor(Color.parseColor(ConfigPOJO.secondary_color))
            pinView.setAnimationEnable(true) // enables animation while entering the otp in the pinview box
            pinView.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    /* Do nothing*/
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    /* Do nothing*/
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 6) {
                        /* hides the keyboard automatically after successfully entering 4 digits otp */
                        pinView.hideKeyboard()
                    }
                }
            })
                tvPhone.text = String.format(Locale.US, "%s %s", getString(R.string.enter_the_otp), arguments?.getString("phone_number"))
        }
        setListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun setListeners() {
        tvResend.setOnClickListener { resendOtpApiCall() }
        otpBinding?.tvRResend.setOnClickListener {
            resendOtpApiCall()
        }
        ivBack.setOnClickListener { fragmentManager?.popBackStackImmediate() }

        tvVerify.setOnClickListener {
            if (pinView.text.toString().length < 6) {
                it.showSnack(getString(R.string.otp_validation_message)) // User enters invalid otp
            } else {
                val map = HashMap<String, String>()
                map["otp"] = pinView.text.toString()
                if (CheckNetworkConnection.isOnline(activity)) {
                    presenter.verifyOtp(map) //  Api call for otp verification
                } else {
                    CheckNetworkConnection.showNetworkError(rootView)
                }
            }
        }
    }

    /* Api call to resend the opt*/
    private fun resendOtpApiCall() {
        if (CheckNetworkConnection.isOnline(activity)) {
            val map = arguments?.getSerializable("map") as HashMap<String, String>
            presenter.sendOtpApiCall(map)
        } else {
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }

    /* Api success for Verification code api */
    override fun onApiSuccess(response: LoginModel?) {
//        SharedPrefs.with(activity).save(SERVICES, Gson().toJson(ConfigPOJO.settingsResponse?.services))
        SharedPrefs.with(activity).save(SERVICES, Gson().toJson(response?.services))
        if (getString(R.string.app_name) == "Wasila") {
            if (response?.AppDetail?.user?.phone_code=="+968")
            {
                ConfigPOJO.currency = "OMR"
                ConfigPOJO.OMAN_PHONECODE = response.AppDetail?.user?.phone_code?:""
            }
        }
        if (response?.AppDetail?.user?.name?.isEmpty() == true) {
            if(ConfigPOJO.TEMPLATE_CODE == Constants.MOBY){
                ACCESS_TOKEN = response?.AppDetail?.access_token ?: ""
                SharedPrefs.with(activity).save(PROFILE, response?.AppDetail)
                SharedPrefs.with(activity).save(SERVICES, Gson().toJson(response?.services))
                activity?.finishAffinity()
                startActivity(Intent(activity, HomeActivity::class.java))
            }else{
                ACCESS_TOKEN = response.AppDetail?.access_token ?: ""
                fragmentManager?.let { it1 -> Utils.replaceFragment(it1, EnterNameFragment(), R.id.container, OtpFragment::class.java.simpleName) }
            }
        } else {
            ACCESS_TOKEN = response?.AppDetail?.access_token ?: ""
            SharedPrefs.with(activity).save(PROFILE, response?.AppDetail)
            activity?.finishAffinity()
            if(Constants.SECRET_DB_KEY == "8969983867fe9a873b1b39d290ffa25c"){
                startActivity(Intent(activity, MainMenuActivity::class.java))
            }else {
                startActivity(Intent(activity, HomeActivity::class.java))
            }
        }
        SharedPrefs.with(activity).save(ACCESS_TOKEN_KEY, response?.AppDetail?.access_token)
    }

    /* Api success for send OTP api*/
    override fun resendOtpSuccss(response: SendOtp?) {
        ACCESS_TOKEN = response?.access_token ?: ""
        SharedPrefs.get().save(ACCESS_TOKEN_KEY, ACCESS_TOKEN)
        rootView.showSnack(getString(R.string.otp_resent_successfully))
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate.show(isLoading)
    }

    override fun apiFailure() {
        rootView.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        rootView.showSnack(error.toString())
    }
}
package com.trava.user.ui.signup.moby.resgister_screens.fragments.institution


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.trava.user.R
import com.trava.user.databinding.FragmentIntitutionLoginBinding
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.signup.moby.resgister_screens.fragments.login.EmailLoginContractor
import com.trava.user.ui.signup.moby.resgister_screens.fragments.login.EmailLoginPresenter
import com.trava.user.utils.ConfigPOJO
import com.trava.utilities.CheckNetworkConnection
import com.trava.utilities.DialogIndeterminate
import com.trava.utilities.SharedPrefs
import com.trava.utilities.constants.ACCESS_TOKEN
import com.trava.utilities.constants.ACCESS_TOKEN_KEY
import com.trava.utilities.constants.PROFILE
import com.trava.utilities.constants.SERVICES
import com.trava.utilities.showSnack
import com.trava.utilities.webservices.models.LoginModel
import kotlinx.android.synthetic.main.fragment_otp_summer.*
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.HashMap


/**
 * A simple [Fragment] subclass.
 */
class IntitutionLogin : Fragment() , EmailLoginContractor.View{

    var binding : FragmentIntitutionLoginBinding ?= null
    private val emailLoginPresenter = EmailLoginPresenter()
    private lateinit var dialogIndeterminate: DialogIndeterminate



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_intitution_login, container, false)
        binding?.color = ConfigPOJO.Companion
        binding?.tvProceedFab?.background?.mutate()?.setTint(Color.parseColor(ConfigPOJO.primary_color))


        return  binding?.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emailLoginPresenter.attachView(this) // attach presenter
        dialogIndeterminate = DialogIndeterminate(activity)

        binding?.ivBack?.setOnClickListener {
            activity?.onBackPressed()
        }

        binding?.tvProceedFab?.setOnClickListener {
            // email login
            if(!isValidEmail(binding?.emailEdt?.text.toString())){
                binding?.root?.showSnack(R.string.email_valid_validation_message)
            }
            else if(binding?.passEdt?.text.toString().length<6 || binding?.passEdt?.text.toString().isNullOrEmpty()){
                binding?.root?.showSnack(getString(R.string.pass_not_empty))
            }else{
                if (CheckNetworkConnection.isOnline(activity)) {
                    val map = HashMap<String, String>()

                    map["timezone"] = TimeZone.getDefault().id
                    map["latitude"] = "0.0"
                    map["longitude"] = "0.0"
                    map["email"] = binding?.emailEdt?.text.toString()
                    map["password"] = binding?.passEdt?.text.toString()
                    map["device_type"] = "Android"
                    map["login_as"] = "PrivateCooperative"


                    if (CheckNetworkConnection.isOnline(activity)) {
                        emailLoginPresenter.emailLogin(map)
                    } else {
                        CheckNetworkConnection.showNetworkError(binding?.root!!)
                    }

                } else {
                    CheckNetworkConnection.showNetworkError(binding?.root!!)
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

    override fun onApiSuccess(response: LoginModel?, map: Map<String, String>) {

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
        emailLoginPresenter.detachView()
    }

}

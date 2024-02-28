package com.trava.user.ui.signup.moby.resgister_screens

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.trava.user.MainActivity
import com.trava.user.R
import com.trava.user.ui.signup.moby.resgister_screens.fragments.institution.IntitutionLogin
import com.trava.user.ui.signup.moby.resgister_screens.fragments.institution.Step1_InstitutionSignup
import com.trava.user.ui.signup.moby.resgister_screens.fragments.login.LoginFragment
import com.trava.user.ui.signup.moby.resgister_screens.fragments.signup.SignupFragment
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.utilities.Constants
import com.trava.utilities.LocaleManager
import java.util.*

class RegisterActivity : AppCompatActivity() {

    var screenType : String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        if(intent.extras!=null){
           screenType = intent.getStringExtra("screenType")
        }

        if(screenType.equals("login")){
            supportFragmentManager.beginTransaction().add(R.id.container, LoginFragment()).commit()
        }else if(screenType.equals("register")){
            supportFragmentManager.beginTransaction().add(R.id.container, SignupFragment()).commit()
        }else if(screenType.equals("phone")){
            val signupFragment = SignupFragment.newInstance(intent.getStringExtra("social_key")?:""
                                        , intent.getStringExtra("signup_as")?:"")
            supportFragmentManager.beginTransaction().add(R.id.container, signupFragment).commit()
        }else if(screenType.equals("inst_login")){
            supportFragmentManager.beginTransaction().add(R.id.container, IntitutionLogin()).commit()
        }else if(screenType.equals("inst_signup")){
            supportFragmentManager.beginTransaction().add(R.id.container, Step1_InstitutionSignup()).commit()
        }else if(screenType.equals("cooprate")){
            var fragment = LoginFragment()
            var bundle = Bundle()
            bundle.putString("from","cooprate")
            fragment.arguments = bundle
            supportFragmentManager.beginTransaction().add(R.id.container, fragment).commit()
        }

        val statusColor= Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)
    }

    override fun onBackPressed() {
        when {
            supportFragmentManager.backStackEntryCount > 0 -> supportFragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            else -> super.onBackPressed()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }
}
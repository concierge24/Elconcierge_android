package com.trava.user.ui.signup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.trava.user.R
import com.trava.user.ui.signup.moby.landing.LandingScreen
import com.trava.user.utils.ConfigPOJO
import com.trava.user.walkthrough.WalkthroughActivity
import com.trava.utilities.Constants
import com.trava.utilities.SharedPrefs
import kotlinx.android.synthetic.main.activity_register_options.*

class RegisterOptionsActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_options)

        setListeners()
    }

    private fun setListeners() {
        llBusiness.setOnClickListener(this)
        llCustomer.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.llBusiness -> {
                SharedPrefs.get().save("user_type","business")
                openNextScreen()
            }
            R.id.llCustomer -> {
                SharedPrefs.get().save("user_type","customer")
                openNextScreen()
            }
        }
    }

    private fun openNextScreen() {
        if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE || ConfigPOJO.TEMPLATE_CODE == Constants.DELIVERY20) {
            startActivity(Intent(this@RegisterOptionsActivity,
                    SignupActivity::class.java))
        } else if (ConfigPOJO.TEMPLATE_CODE == Constants.MOBY) {
            startActivity(Intent(this@RegisterOptionsActivity,
                    LandingScreen::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
        } else {
            if (ConfigPOJO.is_facebookLogin == "true") {
                startActivity(Intent(this@RegisterOptionsActivity,
                        LandingScreen::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
            } else {
                startActivity(Intent(this@RegisterOptionsActivity,
                        WalkthroughActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
            }
        }
    }
}
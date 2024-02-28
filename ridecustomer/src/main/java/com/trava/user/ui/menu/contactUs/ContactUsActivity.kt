package com.trava.user.ui.menu.contactUs

import android.content.Context
import android.os.Bundle

import android.widget.Toast
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.trava.user.R
import com.trava.user.utils.AppUtils
import com.trava.driver.ui.home.contactUs.ContactUsContract
import com.trava.user.databinding.ActivityContactUsBinding
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.utilities.*
import kotlinx.android.synthetic.main.activity_contact_us.*
import java.util.*

class ContactUsActivity : AppCompatActivity(), ContactUsContract.View {

    private var presenter = ContactUsPresenter()
    lateinit var contactUsBinding: ActivityContactUsBinding
    private var dialogIndeterminate: DialogIndeterminate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactUsBinding = DataBindingUtil.setContentView(this, R.layout.activity_contact_us)
        contactUsBinding.color = ConfigPOJO.Companion
        presenter.attachView(this)

        if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE) {
            cvToolbar.setBackgroundColor(Color.parseColor(ConfigPOJO.primary_color))
            ivBack.setImageResource(R.drawable.ic_back_white_snd)
            tv_toolbar_header.setTextColor(Color.parseColor(ConfigPOJO.white_color))
            ll_trava.visibility = View.GONE
            ll_summer.visibility = View.VISIBLE
        } else {
            ll_trava.visibility = View.VISIBLE
            ll_summer.visibility = View.GONE
            tv_toolbar_header.setTextColor(Color.parseColor(ConfigPOJO.black_color))
            ivBack.setImageResource(R.drawable.ic_back_arrow_black)
            cvToolbar.setBackgroundColor(Color.parseColor(ConfigPOJO.white_color))
        }

        tvReachViaPhone.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        tvReachViaMail.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        tvSubmit.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)

        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)

        dialogIndeterminate = DialogIndeterminate(this)
        ivBack.setOnClickListener {
            onBackPressed()
        }

        tvSubmit.setOnClickListener {
            val msg = etMsg.text.toString().trim()
            if (!msg.isEmpty()) {
                presenter.sendMsg(msg)
            } else {
                tvSubmit?.showSnack(getString(R.string.please_enter_message))
            }
        }

        tvReachViaMail.setOnClickListener {
            var intent: Intent? = null
            if (ConfigPOJO.support_email.equals("")) {
                intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "support@trava.lk", null))
            } else {
                intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", ConfigPOJO.support_email, null))
            }
            try {
                startActivity(Intent.createChooser(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), getString(R.string.send_mail_to_buraq)))
            } catch (ex: android.content.ActivityNotFoundException) {
                Toast.makeText(this@ContactUsActivity, getString(R.string.no_email_app_installed), Toast.LENGTH_SHORT).show()
            }
        }

        tvReachViaPhone.setOnClickListener {
            var phone = ""
            if (ConfigPOJO.support_number == "") {
                phone = "+8016608784"
            } else {
                phone = ConfigPOJO.support_number
            }
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
    }


    override fun onApiSuccess() {
        Toast.makeText(this, R.string.we_will_reach_soon, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun apiFailure() {
        tvSubmit?.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(this)
        } else {
            tvSubmit?.showSnack(error.toString())
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

}
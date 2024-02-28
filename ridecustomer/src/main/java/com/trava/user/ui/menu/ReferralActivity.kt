package com.trava.user.ui.menu

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.trava.user.R
import com.trava.user.databinding.ActivityReferralBinding
import com.trava.user.utils.ConfigPOJO
import com.trava.utilities.LocaleManager
import com.trava.utilities.SharedPrefs
import com.trava.utilities.constants.PROFILE
import com.trava.utilities.webservices.models.AppDetail
import kotlinx.android.synthetic.main.activity_referral.*
import java.util.*


class ReferralActivity : AppCompatActivity() {

    var binding : ActivityReferralBinding ?= null
    var refrralCode : String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_referral)
        binding?.color = ConfigPOJO.Companion

        var profile = SharedPrefs.with(this).getObject(PROFILE, AppDetail::class.java)
        refrralCode = profile.user?.referral_code.toString()

        tvShare.setOnClickListener {
            shareLink()
        }

        copy_icon.setOnClickListener {
            copyOperation()
        }
        var res = getResources()
        var text = String.format(res.getString(R.string.refer_your_riders_to_kuai_ride_for_each_riders_successfully_making_their_first_cab_booking_get_special_rewards),
                res.getString(R.string.app_name))
        tvName.text=text
        ref_code_tv.setText(refrralCode.toString())
    }

    private fun copyOperation() {
        val cm: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setText(ref_code_tv.getText())
        Toast.makeText(this, "copied", Toast.LENGTH_SHORT).show()
    }

    private fun shareLink() {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share) + " " + getString(R.string.app_name))
        share.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + com.trava.user.AppVersionConstants.APPLICATION_ID +"&hl=en_US"+ "\n\n"+"referral_code : "+refrralCode)
        startActivity(share)
        finish()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

}
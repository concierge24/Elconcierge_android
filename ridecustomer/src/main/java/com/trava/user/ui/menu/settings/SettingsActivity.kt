package com.trava.user.ui.menu.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.DataBindingUtil
import com.trava.user.MainActivity
import com.trava.user.R
import com.trava.user.databinding.ActivitySettingsBinding
import com.trava.user.ui.home.WebViewActivity
import com.trava.user.ui.menu.ReferralActivity
import com.trava.user.ui.menu.settings.editprofile.EditProfileActivity
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.appsettings.LanguageSets
import com.trava.utilities.*
import com.trava.utilities.Constants.CORSA
import com.trava.utilities.Constants.GOMOVE_BASE
import com.trava.utilities.constants.*
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_POLICIES
import com.trava.utilities.webservices.models.AppDetail
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*
import kotlin.system.exitProcess

class SettingsActivity : AppCompatActivity(), View.OnClickListener, SettingsContract.View {

    private var languagesAdapter: ArrayAdapter<String>? = null
    private var languages = arrayListOf<String>()
    private var LanguageSets = arrayListOf<LanguageSets>()

    private var presenter = SettingsPresenter()
    private var dialogIndeterminate: DialogIndeterminate? = null
    private var selectedLanguageID = 0
    private var selectedLanguagePopsition = 0
    private var selectedLanguageName = ""
    private var changeNotifSetting = false
    var binding: ActivitySettingsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        binding?.color = ConfigPOJO.Companion

        if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE) {
            cvToolbar.setBackgroundColor(Color.parseColor(ConfigPOJO.primary_color))
            ivBack.setImageResource(R.drawable.ic_back_white_snd)
            tv_toolbar_header.setTextColor(Color.parseColor(ConfigPOJO.white_color))
            rr_language.visibility = View.GONE
        } else {
            tv_toolbar_header.setTextColor(Color.parseColor(ConfigPOJO.black_color))
            ivBack.setImageResource(R.drawable.ic_back_arrow_black)
            cvToolbar.setBackgroundColor(Color.parseColor(ConfigPOJO.white_color))
        }

        if (ConfigPOJO.dynamicBar?.is_Refer_and_Earn!! && ConfigPOJO.is_merchant == "true") {
            tvReferAndEarn.visibility = View.VISIBLE
        }

        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)
        iv_drop.setColorFilter(Color.parseColor(ConfigPOJO.primary_color), PorterDuff.Mode.SRC_IN)
        /*val states = arrayOf<IntArray>(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked))
        val thumbColors = intArrayOf(Color.parseColor(ConfigPOJO.primary_color), Color.parseColor(ConfigPOJO.primary_color))
        val trackColors = intArrayOf(Color.parseColor(ConfigPOJO.black_color), Color.parseColor(ConfigPOJO.black_color))
        val switchCompat = findViewById(R.id.switchPush) as Switch
        DrawableCompat.setTintList(DrawableCompat.wrap(switchCompat.getThumbDrawable()), ColorStateList(states, thumbColors))
        DrawableCompat.setTintList(DrawableCompat.wrap(switchCompat.getTrackDrawable()), ColorStateList(states, trackColors))
*/
        presenter.attachView(this)
        setListener()
        setLanguageAdapter()

        dialogIndeterminate = DialogIndeterminate(this)
        val notification = SharedPrefs.with(this).getObject(PROFILE, AppDetail::class.java).notifications
        switchPush.isChecked = notification == "1"
    }

    private fun setListener() {
        ivBack.setOnClickListener { onBackPressed() }

        tvEditProfile.setOnClickListener(this)
        tvTermsConditions.setOnClickListener(this)
        tvPrivacy.setOnClickListener(this)
        tvAboutUs.setOnClickListener(this)
        tvReferAndEarn.setOnClickListener(this)
        switchPush.setOnCheckedChangeListener { buttonView, isChecked ->
            changeNotifSetting=true
            presenter.updateSettingsApiCall("", if (isChecked) "1" else "0")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvEditProfile -> {
                startActivity(Intent(this, EditProfileActivity::class.java))
            }
            R.id.tvTermsConditions -> {
                val intent = Intent(this, WebViewActivity::class.java)
                intent.putExtra(TITLE, getString(R.string.terms_and_conditions_caps))
                if (ConfigPOJO.TEMPLATE_CODE == GOMOVE_BASE) {
                    intent.putExtra(URL_TO_LOAD, "${BASE_POLICIES}/legal/terms/")// gomove
                } else if (ConfigPOJO.TEMPLATE_CODE == CORSA) {
                    intent.putExtra(URL_TO_LOAD, "${BASE_POLICIES}/terms_conditions/user/")// gomove
                } else {
                    intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}/terms_conditions/user/")
                }
                startActivity(intent)
            }
            R.id.tvPrivacy -> {
                val intent = Intent(this, WebViewActivity::class.java)
                intent.putExtra(TITLE, getString(R.string.privacy))
                if (ConfigPOJO.TEMPLATE_CODE == GOMOVE_BASE) {
                    intent.putExtra(URL_TO_LOAD, "${BASE_POLICIES}/legal/privacy/")// gomove
                } else if (ConfigPOJO.TEMPLATE_CODE == CORSA) {
                    intent.putExtra(URL_TO_LOAD, "${BASE_POLICIES}/privacy_policy/user/")// gomove
                } else {
                    intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}/privacy_policy/user/")
                }
                startActivity(intent)
            }
            R.id.tvAboutUs -> {
                val intent = Intent(this, WebViewActivity::class.java)
                intent.putExtra(TITLE, getString(R.string.about_us))
                if (ConfigPOJO.TEMPLATE_CODE == GOMOVE_BASE) {
                    intent.putExtra(URL_TO_LOAD, "${BASE_POLICIES}/business")// gomove
                } else if (ConfigPOJO.TEMPLATE_CODE == CORSA) {
                    intent.putExtra(URL_TO_LOAD, "${BASE_POLICIES}/about_us/user/")// gomove
                } else {
                    intent.putExtra(URL_TO_LOAD, "${ConfigPOJO.admin_base_url}/about_us/user/")
                }
                startActivity(intent)
            }
            R.id.tvReferAndEarn -> {
                startActivity(Intent(this, ReferralActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
        }
    }

    private fun setLanguageAdapter() {
        languages.clear()
        LanguageSets.clear()
        LanguageSets.addAll(ConfigPOJO.settingsResponse?.languages!!)
        if (LanguageSets.size > 0) {
            if (LanguageSets.size==1)
            {
                rr_language.visibility = View.GONE
            }
            for (i in LanguageSets.indices) {
                languages.add(LanguageSets.get(i).language_name)
                if (LanguageSets.get(i).language_code?.toLowerCase() == LocaleManager.getLanguage(this)) {
                    selectedLanguagePopsition = i
                    selectedLanguageID = LanguageSets.get(i).language_id
                }
            }
        }

        languagesAdapter = ArrayAdapter(this, R.layout.layout_spinner_languages, languages)
        languagesAdapter?.setDropDownViewResource(R.layout.item_languages)
        spinnerLanguages.adapter = languagesAdapter
        spinnerLanguages.setSelection(selectedLanguagePopsition)
        spinnerLanguages.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                (parent?.getChildAt(0) as TextView).setTextColor(Color.parseColor(ConfigPOJO.primary_color))

                selectedLanguageID = LanguageSets.get(position).language_id
                selectedLanguageName = LanguageSets.get(position).language_code.toLowerCase()

                if (LocaleManager.getLanguage(this@SettingsActivity) != selectedLanguageName) {
                    SharedPrefs.get().save(PREFS_LANGUAGE_ID, position + 1)
                    SharedPrefs.get().save(LANGUAGE_CHANGED, true)
                    changeNotifSetting=false
                    presenter.updateSettingsApiCall(selectedLanguageID.toString(), if (switchPush.isChecked) "1" else "0")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun setNewLocale(language: String, restartProcess: Boolean): Boolean {
        if (language == "ar") {
            LocaleManager.setNewLocale(this, language)
            val mStartActivity = Intent(this@SettingsActivity, MainActivity::class.java)
            val mPendingIntentId = 123456
            val mPendingIntent: PendingIntent = PendingIntent.getActivity(this@SettingsActivity, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT)
            val mgr: AlarmManager = this@SettingsActivity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
            exitProcess(0)
        } else {
            LocaleManager.setNewLocale(this, language)
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("hold", false)
            startActivity(intent)
            finish()
        }
        return true
    }

    @Override
    override fun recreate() {
        super.recreate()
    }

    override fun onSettingsApiSuccess() {
        if (!changeNotifSetting)
        {
            if (LocaleManager.getLanguage(this@SettingsActivity) != selectedLanguageName) {
                SharedPrefs.get().save(LANGUAGE_CODE, selectedLanguageID.toString())
                setNewLocale(selectedLanguageName, true)
            }
        }
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun apiFailure() {
        rootView.showSWWerror()
        setLanguageAdapter()
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(this)
        } else {
            rootView.showSnack(error ?: "")
            setLanguageAdapter()
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }
}
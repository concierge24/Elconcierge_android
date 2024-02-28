package com.codebrew.clikat.module.change_language


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.extension.launchActivity
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseCardDialog
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.LayoutChangeLanguageBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.bottom_navigation.MainScreenActivity
import com.codebrew.clikat.module.essentialHome.EssentialHomeActivity
import com.codebrew.clikat.module.essentialHome.ServiceListFragment
import com.codebrew.clikat.module.location.LocationActivity
import com.codebrew.clikat.module.setting.SettingNavigator
import com.codebrew.clikat.module.setting.SettingViewModel
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.ClikatConstants
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.configurations.Configurations
import com.trava.utilities.LocaleManager
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.layout_change_language.*
import java.util.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
private const val ARG_PARAM1 = "servieType"

class ChangeLanguage : BaseCardDialog(), SettingNavigator {


    @Inject
    lateinit var dataManager: DataManager


    @Inject
    lateinit var factory: ViewModelProviderFactory
    @Inject
    lateinit var serviceList: ServiceListFragment

    @Inject
    lateinit var appUtils: AppUtils

    @Inject
    lateinit var prefHelper: PreferenceHelper

    private var mViewModel: SettingViewModel? = null
    private var serviceType = false
    private var mBinding: LayoutChangeLanguageBinding? = null

    var settingData: SettingModel.DataBean.SettingData? = null

    var languageId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        mBinding = DataBindingUtil.inflate(inflater, R.layout.layout_change_language, container, false)
        mViewModel = ViewModelProviders.of(this, factory).get(SettingViewModel::class.java)
        mViewModel?.navigator = this
        mBinding?.color = Configurations.colors
        return mBinding?.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        arguments?.let {
            serviceType = it.getBoolean(ARG_PARAM1)
        }

        languageId = prefHelper.getKeyValue(DataNames.SELECTED_LANGUAGE, PrefenceConstants.TYPE_STRING).toString()

        settingData = dataManager.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_first_lang.text = getString(R.string.english)

        settingData?.secondary_language?.let {

            if (it != "0") {
                gp_langauage.visibility = View.VISIBLE
                val locale = Locale(it)
                tv_second_lang.text = if (languageId == ClikatConstants.LANGUAGE_EN  &&  settingData?.is_wagon_app=="1") {
                    locale.getDisplayLanguage(Locale(ClikatConstants.LANGUAGE_AR))
                }else {
                    locale.displayLanguage
                }
            } else {
                gp_langauage.visibility = View.GONE
            }
        }



        tv_first_lang.setOnClickListener {
            selectLanguageId("en")
        }

        tv_second_lang.setOnClickListener {
            settingData?.secondary_language?.let {
                selectLanguageId(it)
            }
        }

        btn_cancel.setOnClickListener {
            dismiss()
        }

    }


    private fun selectLanguageId(langCode: String) {

        if (isNetworkConnected && dataManager.getCurrentUserLoggedIn()) {
            mViewModel?.changeNotiLang(appUtils.getLangCode(langCode), langCode)
        } else {
            onNotiLangChange("", langCode)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(type: Boolean? = null) =
                ChangeLanguage().apply {
                    arguments = Bundle().apply {
                        putBoolean(ARG_PARAM1, type ?: false)
                    }
                }

    }

    override fun onNotiLangChange(message: String, langCode: String) {

        if (dataManager.getCurrentUserLoggedIn()) {
            mBinding?.root?.onSnackbar(message)
        }

        AppGlobal.localeManager?.setNewLocale(activity ?: requireContext(), langCode)

        when (langCode) {
            "arabic", "ar" -> {
                serviceList.stopData()
                GeneralFunctions.force_layout_to_RTL(activity as AppCompatActivity?)
            }
            "spanish", "español", "es" -> {
                serviceList.stopData()
                GeneralFunctions.force_layout_to_LTR(activity as AppCompatActivity?)
            }
            else -> {
                serviceList.stopData()
                GeneralFunctions.force_layout_to_LTR(activity as AppCompatActivity?)
            }
        }

        activity?.finishAffinity()
        if (serviceType) {
            prefHelper.setkeyValue(DataNames.FIRST_TIME, true)
            activity?.launchActivity<LocationActivity>()
        } else {
            activity?.launchActivity<EssentialHomeActivity>()
        }

    }

    override fun onErrorOccur(message: String) {
        mBinding?.root?.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }


}

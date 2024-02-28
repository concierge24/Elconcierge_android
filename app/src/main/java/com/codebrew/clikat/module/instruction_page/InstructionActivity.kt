package com.codebrew.clikat.module.instruction_page

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.codebrew.clikat.BR
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.extension.launchActivity
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.ActivityInstructionScreenBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.modal.other.SettingModel.DataBean.ScreenFlowBean
import com.codebrew.clikat.module.change_language.ChangeLanguage
import com.codebrew.clikat.module.instruction_page.adapter.InstructionAdapter
import com.codebrew.clikat.module.instruction_page.adapter.InstructionAdapter.InstructionCallback
import com.codebrew.clikat.module.location.LocationActivity
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.StaticFunction.setStatusBarColor
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.gson.Gson
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.activity_instruction_screen.*
import javax.inject.Inject

class InstructionActivity : BaseActivity<ActivityInstructionScreenBinding, InstructionViewModel>(), BaseInterface, InstructionCallback, HasAndroidInjector {

    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var appUtils: AppUtils

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    private var clientInform: SettingModel.DataBean.SettingData? = null
    private var tutorialScreens = ArrayList<SettingModel.DataBean.TutorialItem>()

    private var mViewModel: InstructionViewModel? = null

    private val appBackground = Color.parseColor(Configurations.colors.appBackground)
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruction_screen)
        clientInform = dataManager.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
        viewModel.navigator = this

        if (BuildConfig.CLIENT_CODE == "duka_0754") {
            setStatusBarColor(this, Color.parseColor("#2D8CFF"))
        } else {
            setStatusBarColor(this, appBackground)
        }
        /* requestWindowFeature(Window.FEATURE_NO_TITLE)
         setStatusBarColor(this@InstructionActivity, appBackground)*/
        settingLayout()
        pagerlistener()
    }

    private fun pagerlistener() {
        viewPager?.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (clientInform?.is_skip_theme == "1") {
                    tvPrev?.visibility = if (position == 1 || position == 2) View.VISIBLE else View.GONE
                    tvNext?.visibility = if (position == 0 || position == 1) View.VISIBLE else View.GONE
                }
                if (position == 2) {
                    scheduleScreen()
                }
            }

            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun scheduleScreen() {
        Handler().postDelayed(
                {
                    // After the screen duration, route to the right activities
                    onActivityFinish()
                },
                3000
        )
    }

    private fun settingLayout() {
        if (!clientInform?.tutorial_screens.isNullOrEmpty()) {
            val tutorialScreens = Gson().fromJson(clientInform?.tutorial_screens, Array<SettingModel.DataBean.TutorialItem>::class.java)
            this.tutorialScreens.addAll(tutorialScreens)
        }
        val screenFlowBean = prefHelper.getGsonValue(DataNames.SCREEN_FLOW, ScreenFlowBean::class.java)
        val adapter = InstructionAdapter(this, screenFlowBean?.app_type ?: 0,
                appUtils, clientInform, tutorialScreens)
        adapter.settingCallback(this)
        viewPager?.adapter = adapter
        indicator?.setViewPager(viewPager)

        tvSkipIntro?.setOnClickListener {
            onActivityFinish()
        }
        tvNext?.visibility = if (clientInform?.is_skip_theme == "1") View.VISIBLE else View.GONE
        tvSkipIntro?.visibility = if (clientInform?.is_pickup_primary == "1") {
            View.VISIBLE
        } else {
            View.GONE
        }


        tvNext?.setOnClickListener {
            viewPager?.currentItem = (viewPager?.currentItem ?: 0) + 1
        }
        tvPrev?.setOnClickListener {
            viewPager?.currentItem = (viewPager?.currentItem ?: 0) - 1
        }
    }

    override fun onChangeLang() {
        ChangeLanguage.newInstance(true).show(supportFragmentManager, "change_language")
    }

    override fun onNextButton(position: Int) {
        onActivityFinish()
    }

    private fun onActivityFinish() {
        prefHelper.setkeyValue(DataNames.FIRST_TIME, true)
        if (prefHelper.getGsonValue(PrefenceConstants.ADRS_DATA, AddressBean::class.java) != null) {
            onAdrressValidate()
        } else {
            launchActivity<LocationActivity>()
        }
        finish()
    }

    private fun onAdrressValidate() {
        val settingBean = prefHelper.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
        var isGuest = settingBean?.login_template.isNullOrEmpty() || settingBean?.login_template == "0"

        if (settingBean?.enable_login_on_launch == "1")
            isGuest = false

        if (isGuest) {
            appUtils.checkHomeActivity(this, intent.extras ?: Bundle.EMPTY)
        } else {
            appUtils.checkLoginFlow(this@InstructionActivity, -1)
        }
    }

    override fun getBindingVariable(): Int = BR.viewModel


    override fun getLayoutId(): Int = R.layout.activity_instruction_screen


    override fun getViewModel(): InstructionViewModel {
        mViewModel = ViewModelProviders.of(this, factory).get(InstructionViewModel::class.java)
        return mViewModel as InstructionViewModel
    }

    override fun onErrorOccur(message: String) = viewDataBinding.root.onSnackbar(message)


    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

    override fun androidInjector(): AndroidInjector<Any> {
      return androidInjector
    }
}
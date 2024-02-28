package com.codebrew.clikat.module.essentialHome

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.codebrew.clikat.BR
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.DialogsUtil
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.databinding.ActivityEssentialHomeBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.module.product.product_listing.DialogListener
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject


class EssentialHomeActivity : BaseActivity<ActivityEssentialHomeBinding, ServiceViewModel>(), BaseInterface, HasAndroidInjector,
    DialogListener {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var mDialogsUtil: DialogsUtil


    private var mViewModel: ServiceViewModel? = null

    private val appBackground = Color.parseColor(Configurations.colors.appBackground)

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_essential_home
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.CLIENT_CODE == "duka_0754") {
            StaticFunction.setStatusBarColor(this, Color.parseColor("#2D8CFF"))
        } else {
            StaticFunction.setStatusBarColor(this, appBackground)
        }

        val navController = findNavController(R.id.nav_container)
        navController.setGraph(navController.graph, intent.extras)

        locationPermission()
    }

    override fun getViewModel(): ServiceViewModel {
        mViewModel = ViewModelProviders.of(this, factory).get(ServiceViewModel::class.java)
        return mViewModel as ServiceViewModel
    }

    override fun onErrorOccur(message: String) {

    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base?.let { AppGlobal.localeManager?.setLocale(it) })
    }

    private fun locationPermission() {
        if (!(dataManager.getKeyValue(DataNames.LOCATION_PERM, PrefenceConstants.TYPE_BOOLEAN) as Boolean))
            mDialogsUtil.openAlertDialog(this, getString(R.string.location_permission_declaration), getString(R.string.ok), "", this)
    }

    override fun onSucessListner() {
        dataManager.setkeyValue(DataNames.LOCATION_PERM, true)
    }

    override fun onErrorListener() {

    }

    override fun onResume() {
        super.onResume()
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }
}

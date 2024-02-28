package com.trava.user.ui.menu.travelPackages

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.trava.user.R
import com.trava.user.ui.menu.travelPackages.package_details.TravelPackageDetailsActivity
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.travelPackages.PackagesItem
import com.trava.utilities.*
import kotlinx.android.synthetic.main.activity_travel_packages.*
import kotlinx.android.synthetic.main.activity_travel_packages.rootView
import java.util.*
import kotlin.collections.ArrayList

class TravelPackagesActivity : AppCompatActivity(), TravellingPackagesContract.View, PackageInterface {

    private val presenter = TravelPackagesPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_packages)

        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)
        rlIntro.setBackgroundColor(Color.parseColor(ConfigPOJO.primary_color))
        presenter.attachView(this)
        hitPackageListingApi()
        setListener()
    }

    private fun setListener() {
        ivBack.setOnClickListener {
            super.onBackPressed()
        }
    }

    private fun hitPackageListingApi() {
        if (CheckNetworkConnection.isOnline(this)) {
            presenter.requestGetPackagesApiCall()
        } else {
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }

    override fun onApiSuccess(list: List<PackagesItem>) {
        if (list.isEmpty()) {
            viewFlipperPkg?.displayedChild = 2
        } else {
            viewFlipperPkg?.displayedChild = 1
            rvPackages.layoutManager = LinearLayoutManager(this)
            rvPackages.adapter = TravelPackagesAdapter(list as ArrayList<PackagesItem>, this)
        }
    }

    override fun showLoader(isLoading: Boolean) {
    }

    override fun apiFailure() {
        rootView.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(this)
        } else {
            rootView.showSnack(error.toString())
        }
    }

    override fun getSelectedData(data: PackagesItem,position : Int) {
        startActivity(Intent(this, TravelPackageDetailsActivity::class.java)
                .putExtra(Constants.PACKAGE_DATA,data)
                .putExtra("pos",position))
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

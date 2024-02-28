package com.trava.user.ui.menu.travelPackages.package_details

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.trava.user.R
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.menu.travelPackages.TravelPkgCategoriesAdapter
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.travelPackages.PackagesItem
import com.trava.utilities.Constants
import com.trava.utilities.LocaleManager
import com.trava.utilities.hide
import com.trava.utilities.openUrl
import kotlinx.android.synthetic.main.activity_travel_package_details.*
import kotlinx.android.synthetic.main.activity_travel_packages.ivBack
import kotlinx.android.synthetic.main.item_packages.*
import kotlinx.android.synthetic.main.item_packages.view.*
import java.util.*

class TravelPackageDetailsActivity : AppCompatActivity() {

    private var pkgData : PackagesItem? = null
    private var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_package_details)

        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)

        tvPlaceRequest.background= StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        intialize()
        setListener()
    }

    private fun intialize() {
        pkgData = intent.getParcelableExtra<PackagesItem>(Constants.PACKAGE_DATA)
        position = intent.getIntExtra("pos",0)
        tvViewPkg.hide()
        setpkgData()
    }

    private fun setListener() {
        ivBack.setOnClickListener{
            onBackPressed()
        }

        tvTermsConditions.setOnClickListener {
            openUrl("https://www.uber.com/legal/terms/us/")
        }

        tvPlaceRequest.setOnClickListener {
            startActivity(Intent(this,HomeActivity::class.java)
                    .putExtra(Constants.PACKAGE_DATA,pkgData))
        }
    }

    private fun setpkgData(){
        tvPkgName.text = pkgData?.name
        tvPkgDesc.text = pkgData?.description
        tvPkgKm.text = String.format("%s","${"Package for"} ${pkgData?.packageData?.distanceKms}${"KM"}")

        if(position%2==0){
            Glide.with(this).load(R.drawable.green).into(ivImage)
        }else{
            Glide.with(this).load(R.drawable.red).into(ivImage)

        }

        when(pkgData?.packageData?.packageType){
            "2" -> {
                /* Hour Packages */
                tvPkgValid.text = String.format("%s","${getString(R.string.validFor)} ${pkgData?.packageData?.validity} ${getString(R.string.hours)}")

            }

            "1" -> {
                /* Day Packages */
                tvPkgValid.text = String.format("%s","${getString(R.string.validFor)} ${pkgData?.packageData?.validity} ${getString(R.string.days)}")


            }

            "3" -> {
                /* Month Packages */
                tvPkgValid.text = String.format("%s","${getString(R.string.validFor)} ${pkgData?.packageData?.validity} ${getString(R.string.months)}")


            }
        }
        val list = pkgData?.packageData?.packagePricings
        rvCategories.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        rvCategories.adapter = list?.let { TravelPkgCategoriesAdapter(it) }
        tvPackageDesc.text = pkgData?.description
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

}

package com.trava.user.ui.home.promocodes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.trava.user.R
import com.trava.user.databinding.ActivityPromocodesBinding
import com.trava.user.ui.home.HomeActivity
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.promocodes.CouponsItem
import com.trava.user.webservices.models.promocodes.PromoCodeResponse
import com.trava.utilities.*
import kotlinx.android.synthetic.main.activity_promocodes.*
import java.util.*
import kotlin.collections.ArrayList

class PromoCodesActivity : AppCompatActivity(), PromoCodeContract.View, PromoCodeInterface {
    private var presenter = PromoCodesPresenter()
    private var promoList = ArrayList<CouponsItem>()
    private var dialog: DialogIndeterminate? = null
    private var isPromoApplied = false
    lateinit var promocodesBinding: ActivityPromocodesBinding
    val REQUEST_CODE = 11
    var isCorsa: Boolean = false
    private var finalCharge: String = "0.0"
    private var minprice: String = "0.0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        promocodesBinding = DataBindingUtil.setContentView(this, R.layout.activity_promocodes)
        promocodesBinding.color = ConfigPOJO.Companion
        // setContentView(promocodesBinding.root)
        val statusColor = Color.parseColor(ConfigPOJO.primary_color)
        StaticFunction.setStatusBarColor(this, statusColor)

        if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            isCorsa = true
        }

        presenter.attachView(this)
        dialog = DialogIndeterminate(this)
        hitPromoCodeApi()
        setListener()

        if (intent.extras != null) {
            finalCharge = intent.getStringExtra("amount") ?: "0.0"
            minprice = intent.getStringExtra("actual_value") ?: "0.0"
        }
    }

    private fun hitPromoCodeApi() {
        if (CheckNetworkConnection.isOnline(this)) {
            presenter.requestPromoCodesapiCall()
        } else {
            CheckNetworkConnection.showNetworkError(rvPromos)
        }
    }

    private fun setListener() {
        ivCross.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        etPromoCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (etPromoCode.text.toString().trim().isNotEmpty()) {
                    tvApply.isEnabled = true
                    tvApply.setTextColor(ContextCompat.getColor(this@PromoCodesActivity, R.color.thin_light_purple))
                } else {
                    tvApply.isEnabled = false
                    tvApply.setTextColor(ContextCompat.getColor(this@PromoCodesActivity, R.color.purple_op_50))
                }
            }
        })

        tvApply.setOnClickListener {
            if (CheckNetworkConnection.isOnline(this)) {
                isPromoApplied = true
                presenter.checkPromoCodeApiCall(etPromoCode.text.toString().trim())
            } else {
                CheckNetworkConnection.showNetworkError(rootView)
            }
//            for(item in promoList){
//                if(item.code?.toUpperCase() == etPromoCode.text.toString().trim()){
//                    isPromoApplied = true
//                    setResult(Activity.RESULT_OK,Intent().putExtra(Constants.PROMO_DATA,item))
//                    finish()
//                    break
//                }
//            }
//            if(!isPromoApplied){
//                tvApply.showSnack(getString(R.string.invalidPromoCode))
//            }
        }
    }

    override fun onApiSuccess(data: PromoCodeResponse) {
        promoList.clear()
        viewFlipperPromos.displayedChild = 1
        promoList.addAll(data.coupons)
        rvPromos.adapter = PromoCodeAdapter(promoList, this)
    }

    override fun onApiCheckPromoSuccess(data: CouponsItem) {
        if (isCorsa) {
            val intent = Intent()
            intent.putExtra(Constants.PROMO_DATA, data)
            setResult(REQUEST_CODE, intent) // You can also send result without any data using setResult(int resultCode)
            finish()
        } else {
            if (!isCorsa) {
                Toast.makeText(this, getString(R.string.promoCodeApplied), Toast.LENGTH_SHORT).show()
            }
            setResult(Activity.RESULT_OK, Intent().putExtra(Constants.PROMO_DATA, data))
            finish()
        }

    }

    override fun showLoader(isLoading: Boolean) {
        if (isPromoApplied) {
            dialog?.show(isLoading)
        }
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

    override fun selectedPromoCode(data: CouponsItem) {
        if (data.purchaseCounts ?: 0 < data.ridesValue ?: 0)
        {
            if (finalCharge.toDouble() > (data.minimum_value ?: "0.0").toDouble())
            {
                if (!isCorsa) {
                    Toast.makeText(this, getString(R.string.promoCodeApplied), Toast.LENGTH_SHORT).show()
                }
                if (isCorsa) {
                    val intent = Intent()
                    intent.putExtra(Constants.PROMO_DATA, data)
                    setResult(REQUEST_CODE, intent) // You can also send result without any data using setResult(int resultCode)
                    finish()
                } else {
                    if (finalCharge.toDouble() > minprice.toDouble())
                    {
                        setResult(Activity.RESULT_OK, Intent().putExtra(Constants.PROMO_DATA, data))
                        finish()
                    }
                    else
                    {
                        Toast.makeText(this, getString(R.string.ride_amount_is_less_than_coupon_value)+" "+data.minimum_value, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this,  getString(R.string.ride_amount_is_less_than_coupon_value)+" "+data.minimum_value, Toast.LENGTH_SHORT).show()
            }
        } else {
            rootView.showSnack(getString(R.string.promocodeLimit))
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
